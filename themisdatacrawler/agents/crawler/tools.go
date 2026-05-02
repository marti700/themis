package crawler

// This file defines:
//   1. The Go types that describe a "tool" in OpenAI-compatible API format.
//   2. A function that returns the tool definition for get_text_from_docx so the
//      model knows the tool exists and how to call it.
//   3. A dispatcher function that actually *runs* the tool when the model requests it.
//
// --- What is a "tool" in the OpenAI API? ---
//
// The OpenAI-compatible chat API lets you tell the model: "here are some functions
// you can call if you need to look something up or take an action".  The model does
// NOT call the function itself — it just responds with a structured JSON message
// that says "please call function X with these arguments".  Your code must then
// actually run the function and send the result back to the model so it can continue.
//
// A tool definition has three parts:
//   - type:        always "function" (the only supported type right now)
//   - name:        the identifier the model uses to request the tool
//   - description: plain English explaining what the tool does (the model reads this
//                  to decide when to use it)
//   - parameters:  a JSON Schema object that describes the arguments the model must
//                  provide when calling the tool (name, type, description of each arg)

import (
	"encoding/json"
	"fmt"

	// docconv converts Office documents (Word .docx, PDF, etc.) to plain text.
	// ConvertPath(path) opens the file at `path`, extracts all text, and returns
	// a struct whose .Body field is that text as a plain string.
	"code.sajari.com/docconv/v2"
)

// ToolDef represents one entry in the "tools" array you send to the chat API.
// The JSON tags (e.g. `json:"type"`) control how this struct is serialised into
// the JSON body of the HTTP request.
type ToolDef struct {
	// Type is always "function" — it tells the API this is a callable function.
	Type string `json:"type"`

	// Function holds the actual description of the function: its name, what it
	// does, and what arguments it expects.
	Function FunctionDef `json:"function"`
}

// FunctionDef describes the function itself inside a ToolDef.
type FunctionDef struct {
	// Name is the identifier the model uses when it wants to call this tool.
	// It must match exactly what you check for in DispatchToolCall below.
	Name string `json:"name"`

	// Description is read by the model to decide when to use this tool.
	// Write it like a docstring: clear, concise, one sentence.
	Description string `json:"description"`

	// Parameters is the JSON Schema that defines the arguments for this function.
	// json.RawMessage lets us embed a raw JSON literal directly without needing
	// a separate Go struct for the schema — it is passed through unchanged.
	Parameters json.RawMessage `json:"parameters"`
}

// GetDocxToolDef builds and returns the ToolDef for the get_text_from_docx tool.
// Call this once when setting up the chat request so the model knows the tool exists.
func GetDocxToolDef() ToolDef {
	// This is a JSON Schema object. It tells the model:
	//   - the arguments object has one property called "path"
	//   - "path" is a string
	//   - "path" is required (the model must always provide it)
	// The model uses this schema to know what JSON to put in its tool-call arguments.
	schema := json.RawMessage(`{
		"type": "object",
		"properties": {
			"path": {
				"type": "string",
				"description": "The filesystem path to the .docx file to read"
			}
		},
		"required": ["path"]
	}`)

	return ToolDef{
		Type: "function",
		Function: FunctionDef{
			Name:        "get_text_from_docx",
			Description: "Extracts all text content from a .docx file at the given path.",
			Parameters:  schema,
		},
	}
}

// DispatchToolCall executes a tool by name using the JSON-encoded arguments the
// model provided, and returns the result as a plain string.
//
// When the model decides to call a tool it sends back a message like:
//
//	{
//	  "tool_calls": [{
//	    "id": "call_abc123",
//	    "type": "function",
//	    "function": {
//	      "name": "get_text_from_docx",
//	      "arguments": "{\"path\": \"/docs/file.docx\"}"
//	    }
//	  }]
//	}
//
// The `arguments` field is a JSON string (a string that itself contains JSON).
// This function receives `name` and `argsJSON` and runs the corresponding Go code.
// The string it returns is sent back to the model as the tool result.
func DispatchToolCall(name string, argsJSON string) string {
	switch name {
	case "get_text_from_docx":
		// The model sends arguments as a JSON string, so we unmarshal it into
		// an anonymous struct to pull out the "path" field.
		var args struct {
			Path string `json:"path"`
		}
		if err := json.Unmarshal([]byte(argsJSON), &args); err != nil {
			// Return the error as a string so the model receives it as the tool
			// result and can report that something went wrong.
			return fmt.Sprintf("error parsing arguments: %v", err)
		}

		// docconv.ConvertPath opens the file, detects its type (Word, PDF, etc.),
		// and extracts all human-readable text from it.
		// result.Body contains the plain-text content of the document.
		result, err := docconv.ConvertPath(args.Path)
		if err != nil {
			return fmt.Sprintf("error reading file: %v", err)
		}
		return result.Body

	default:
		// If the model hallucinates a tool name that does not exist, return a
		// clear error string so the model can recover gracefully.
		return fmt.Sprintf("unknown tool: %s", name)
	}
}
