package crawler

// This file implements RunCrawlerAgent, the main entry point for the crawler.
// It drives an "agentic loop" against a local NVIDIA NIM that exposes an
// OpenAI-compatible REST API.
//
// --- What is an agentic loop? ---
//
// A single call to an LLM is not enough when the model needs external data
// (like reading a file).  An agentic loop works like this:
//
//   1. Send the model a conversation (system prompt + user message) along with
//      a list of tools it can call.
//   2. If the model responds with a tool_call (instead of plain text) it means
//      the model wants us to run a function on its behalf.
//   3. We execute the function, add the result to the conversation as a "tool"
//      message, and send the whole conversation back to the model.
//   4. Repeat until the model responds with plain text (no more tool calls) —
//      that final text is the answer we parse and return.
//
// --- What is the OpenAI chat completions API? ---
//
// It is a simple HTTP POST endpoint.  You send a JSON body with:
//   - model:    which model to use
//   - messages: the conversation so far (system, user, assistant, tool turns)
//   - tools:    optional list of functions the model is allowed to call
//
// The server responds with JSON containing the model's next message.
// The NIM (NVIDIA Inference Microservice) speaks this same protocol, so
// code written for the OpenAI API works with NIM without any changes.

import (
	"bytes"         // bytes.NewReader wraps a []byte so http.NewRequest can read it
	"encoding/json" // serialise Go structs to JSON and back
	"fmt"
	"io"
	"log"
	"net/http" // standard Go HTTP client
	"strings"  // used to strip markdown fences from the model's JSON output
	"time"     // used to set a timeout on the HTTP client
)

// SYSTEM_PROMPT is the persistent instruction given to the model at the start of
// every conversation.  It tells the model its role and the exact output format
// expected.  It is the first message in every request (role: "system").
const SYSTEM_PROMPT = `You are a precision data extraction engine that works in two steps:

STEP 1 — Read the document.
Call the get_text_from_docx tool with the file path provided by the user.
You MUST invoke this tool using the tool-calling mechanism. Never write a tool call as text or inside a code block.

STEP 2 — Extract customers.
Once you have the document text, identify every person who qualifies as a customer and return a JSON array. No commentary, no markdown fences — raw JSON only.

Schema (each object in the array):
  first_name      string  — given name
  last_name       string  — family name
  address         string  — full physical address; "" if missing
  marital_status  string  — one of: single, married, divorced, widowed, legal_union; null if not mentioned
  occupation      string  — job title or profession; "" if missing
  id_number       string  — government ID / passport / SSN; "" if missing

Example output:
[{"first_name":"John","last_name":"Doe","address":"123 Maple St","marital_status":"married","occupation":"Engineer","id_number":"A1234567"}]
`

const (
	// nimURL is the full URL of the OpenAI-compatible chat completions endpoint
	// served by the local NIM container.  The NIM listens on port 8000 and exposes
	// the same path as the real OpenAI API (/v1/chat/completions).
	nimURL = "http://localhost:11434/v1/chat/completions"

	// nimModel is the model identifier sent in every request.
	// NIM uses the model name from the container image without the registry prefix.
	nimModel = "lukaspetrik/gemma3-tools:12b"

	// maxLoopIters is a safety cap.  If the model keeps calling tools and never
	// produces a final text response after this many rounds, we give up and return
	// an error rather than looping forever.
	maxLoopIters = 10
)

// CustomerInfo is the Go struct that represents one extracted customer.
// It matches the JSON schema described in SYSTEM_PROMPT.
// The json tags control how fields are named when marshalled to/from JSON.
// MaritalStatus is a pointer to string (*string) so it can be nil (JSON null)
// when the document does not mention marital status.
type CustomerInfo struct {
	FirstName     string  `json:"first_name"`
	LastName      string  `json:"last_name"`
	Address       string  `json:"address"`
	MaritalStatus *string `json:"marital_status"` // nil when not mentioned in the document
	Occupation    string  `json:"occupation"`
	IDNumber      string  `json:"id_number"`
}

func (c CustomerInfo) String() string {
	ms := "<nil>"
	if c.MaritalStatus != nil {
		ms = *c.MaritalStatus
	}
	return fmt.Sprintf("{FirstName:%s LastName:%s Address:%s MaritalStatus:%s Occupation:%s IDNumber:%s}",
		c.FirstName, c.LastName, c.Address, ms, c.Occupation, c.IDNumber)
}

// --- OpenAI-compatible request / response types ---
//
// These mirror the JSON format the API expects and returns.
// Each struct field has a `json:"..."` tag that sets the exact key name in JSON.
// `omitempty` means the field is omitted from the JSON output when it is empty
// (zero value for its type: "" for string, nil for slice, etc.).

// ChatMessage represents one turn in the conversation.
// The Role field controls who "said" this message:
//   - "system"    — the persistent instruction at the top of every conversation
//   - "user"      — the human's request
//   - "assistant" — the model's response (may contain ToolCalls instead of Content)
//   - "tool"      — the result of a tool call, sent back to the model
type ChatMessage struct {
	Role    string `json:"role"`
	Content string `json:"content,omitempty"` // plain text content (absent for tool-call turns)

	// ToolCallID links a "tool" role message back to the specific tool call that
	// triggered it.  The model uses the ID to match results to requests.
	ToolCallID string `json:"tool_call_id,omitempty"`

	// ToolCalls is populated when the model wants to invoke one or more tools.
	// When this slice is non-empty, Content will be empty — the model is asking
	// us to run these functions instead of providing a text answer.
	ToolCalls []ToolCall `json:"tool_calls,omitempty"`

	// Name is used on "tool" role messages to identify which function produced
	// this result.  Some models require it; including it is always safe.
	Name string `json:"name,omitempty"`
}

// ToolCall is one tool invocation requested by the model inside an assistant message.
type ToolCall struct {
	// ID is a unique string the model generates for this call.  We must echo it
	// back in the tool result message so the model can correlate request/response.
	ID string `json:"id"`

	// Type is always "function" — the only supported tool type.
	Type string `json:"type"`

	// Function contains the name and arguments the model wants to call.
	Function FunctionCall `json:"function"`
}

// FunctionCall holds the name and JSON-encoded arguments for one tool call.
type FunctionCall struct {
	Name string `json:"name"`

	// Arguments is a JSON *string* — it is JSON encoded inside a JSON string.
	// For example: "{\"path\": \"/docs/file.docx\"}"
	// We pass it to DispatchToolCall which unmarshals it again.
	Arguments string `json:"arguments"`
}

// ChatRequest is the full JSON body we POST to /v1/chat/completions.
type ChatRequest struct {
	Model       string        `json:"model"`
	Messages    []ChatMessage `json:"messages"`
	Tools       []ToolDef     `json:"tools,omitempty"`
	MaxTokens   int           `json:"max_tokens,omitempty"`
	Temperature float64       `json:"temperature,omitempty"`
}

// ChatResponse is the JSON body returned by the API.
// We only use Choices and Error; other fields (id, created, usage, etc.) are ignored.
type ChatResponse struct {
	Choices []Choice  `json:"choices"`
	Error   *APIError `json:"error,omitempty"` // non-nil when the API returns an error object
}

// Choice is one candidate response.  The API can return multiple choices but we
// always use only the first one (Choices[0]).
type Choice struct {
	Message      ChatMessage `json:"message"`
	FinishReason string      `json:"finish_reason"` // e.g. "stop", "tool_calls", "length"
}

// APIError is the error object the API returns inside the response body when
// something goes wrong (e.g. invalid model name, malformed request).
type APIError struct {
	Message string `json:"message"`
	Type    string `json:"type"`
}

// postChat sends one chat-completion request to the NIM and returns the parsed
// response.  It is a thin wrapper around the standard Go HTTP client.
func postChat(client *http.Client, req ChatRequest) (*ChatResponse, error) {
	// Serialise the request struct to a JSON byte slice.
	body, err := json.Marshal(req)
	if err != nil {
		return nil, fmt.Errorf("marshal request: %w", err)
	}

	// http.NewRequest needs an io.Reader, so we wrap the byte slice.
	httpReq, err := http.NewRequest(http.MethodPost, nimURL, bytes.NewReader(body))
	if err != nil {
		return nil, err
	}

	// Tell the server we are sending JSON.  Without this header most APIs reject
	// the request with a 415 Unsupported Media Type error.
	httpReq.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(httpReq)
	if err != nil {
		return nil, fmt.Errorf("http post: %w", err)
	}
	defer resp.Body.Close()

	// Buffer the full body so we can both decode it and include it in errors.
	rawBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("read response body: %w", err)
	}

	var chatResp ChatResponse
	if err := json.Unmarshal(rawBody, &chatResp); err != nil {
		return nil, fmt.Errorf("decode response (status %d): %w\nraw body: %s", resp.StatusCode, err, rawBody)
	}

	if chatResp.Error != nil {
		return nil, fmt.Errorf("API error [%s]: %s", chatResp.Error.Type, chatResp.Error.Message)
	}

	// Surface the raw body alongside empty-choices so we can see what the server sent.
	if len(chatResp.Choices) == 0 {
		return nil, fmt.Errorf("no choices in response (status %d)\nraw body: %s", resp.StatusCode, rawBody)
	}

	return &chatResp, nil
}

// RunCrawlerAgent is the public entry point for the crawler.
// It drives the full agentic loop: sends the document path to the model, lets
// the model call the get_text_from_docx tool to read the file, and returns the
// extracted customers once the model produces its final JSON answer.
func RunCrawlerAgent(docPath string) ([]CustomerInfo, error) {
	// Use a single HTTP client for all requests in this call.
	// The 120-second timeout prevents hanging indefinitely if the NIM is slow.
	client := &http.Client{Timeout: 120 * time.Second}

	// Build the initial conversation.  Every conversation starts with:
	//   1. A "system" message — the standing instructions for the model.
	//   2. A "user" message  — the specific task for this invocation.
	messages := []ChatMessage{
		{Role: "system", Content: SYSTEM_PROMPT},
		{
			Role:    "user",
			Content: fmt.Sprintf("Call get_text_from_docx with path=%q, then extract all customers as JSON.", docPath),
		},
	}

	// Tell the model about the one tool it can use.
	tools := []ToolDef{GetDocxToolDef()}

	// Agentic loop — keeps running until the model gives a final text response
	// or we hit the iteration cap.
	for i := 0; i < maxLoopIters; i++ {
		resp, err := postChat(client, ChatRequest{
			Model:       nimModel,
			Messages:    messages,
			Tools:       tools,
			MaxTokens:   1024,
			Temperature: 0,
		})
		if err != nil {
			return nil, fmt.Errorf("iteration %d: %w", i, err)
		}

		// The model's reply for this round.
		assistantMsg := resp.Choices[0].Message
		log.Printf("[iter %d] finish_reason=%q tool_calls=%d content=%q",
			i, resp.Choices[0].FinishReason, len(assistantMsg.ToolCalls), assistantMsg.Content)

		// Always append the assistant's turn to the conversation so that the
		// next request includes the full history.  The model needs this context
		// to continue reasoning correctly.
		messages = append(messages, assistantMsg)

		// If the model did NOT request any tool calls, the conversation is done.
		// assistantMsg.Content now holds the final answer (the JSON customer list).
		if len(assistantMsg.ToolCalls) == 0 {
			return parseCustomers(assistantMsg.Content)
		}

		// The model wants to call one or more tools.  Execute each one and add
		// the result as a "tool" role message.  The model will see all results
		// together in the next request.
		for _, tc := range assistantMsg.ToolCalls {
			result := DispatchToolCall(tc.Function.Name, tc.Function.Arguments)

			// A "tool" message must include the ToolCallID that the model issued.
			// Without it the API will reject the request.
			messages = append(messages, ChatMessage{
				Role:       "tool",
				ToolCallID: tc.ID,          // echo the ID back so the model can match request/result
				Name:       tc.Function.Name,
				Content:    result,
			})
		}
		// Loop back: send the enriched conversation (now including tool results)
		// to the model so it can continue.
	}

	return nil, fmt.Errorf("agentic loop exceeded %d iterations without a final answer", maxLoopIters)
}

// parseCustomers extracts a []CustomerInfo from the model's final text response.
//
// Models are instructed to reply with raw JSON, but they sometimes wrap it in
// a markdown code fence like:
//
//	```json
//	[ ... ]
//	```
//
// This function strips that fence (if present) before calling json.Unmarshal.
func parseCustomers(text string) ([]CustomerInfo, error) {
	text = strings.TrimSpace(text)

	// Detect and strip markdown code fences.
	if idx := strings.Index(text, "```"); idx != -1 {
		text = text[idx:]
		text = strings.TrimPrefix(text, "```json") // strip ```json opener
		text = strings.TrimPrefix(text, "```")     // strip plain ``` opener
		if end := strings.LastIndex(text, "```"); end != -1 {
			text = text[:end] // strip closing ```
		}
		text = strings.TrimSpace(text)
	}

	var customers []CustomerInfo
	if err := json.Unmarshal([]byte(text), &customers); err != nil {
		// Include the raw model output in the error message so you can debug
		// what the model actually returned when parsing fails.
		return nil, fmt.Errorf("parse customers JSON: %w\nraw model output: %s", err, text)
	}
	return customers, nil
}
