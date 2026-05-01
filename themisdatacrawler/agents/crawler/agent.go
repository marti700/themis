package crawler

import (
	"context"
	"fmt"
	"log"
	"path/filepath"

	"google.golang.org/adk/agent"
	"google.golang.org/adk/agent/llmagent"
	"google.golang.org/adk/model/gemini"
	"google.golang.org/adk/runner"
	"google.golang.org/adk/session"
	"google.golang.org/adk/tool"
	"google.golang.org/genai"
)

const SYSTEM_PROMPT = `
Role: You are a precision data extraction engine. Your task is to parse the provided text and identify all individual customers, converting their information into a valid JSON array based on a specific Go struct schema.

Instructions:

Extract: Locate every person mentioned in the text who qualifies as a customer.

Format: Output the results as a JSON array of objects. Each object must strictly follow the field names and data requirements below.

Null Handling: If a piece of information is missing from the text, use null for fields typed as pgtype or NullMaritalStatus. For string fields, use an empty string "".

No Commentary: Provide only the JSON. Do not include any introductory or explanatory text.

Schema Definitions:

first_name: (string)

last_name: (string)

address: (string) Full physical address.

marital_status: (string) Must be one of the following valid enum values: single, married, divorced, widowed, legal_union . If not mentioned, use null.

occupation: (string) Current job title or profession.

id_number: (string) Government ID, Passport, or SSN if mentioned.

Output Structure Example:

JSON
[
  {
    "first_name": "John",
    "last_name": "Doe",
    "address": "123 Maple St, Springfield",
    "marital_status": "married",
    "occupation": "Software Engineer",
    "id_number": "A1234567",
  }
]
`

func Crawl(documentPath string) {
	ctx := context.Background()
	fileName := filepath.Base(documentPath)
	log.Printf("Crawling document: %s", documentPath)
	fmt.Println(fileName)

	model, err := gemini.NewModel(ctx, "gemini-2.5-flash", &genai.ClientConfig{
		APIKey: "AIzaSyATF5qby8otFJZQkAc6MHE-FPyG2jM9YSQ",
	})
	if err != nil {
		log.Fatalf("Failed to create model: %v", err)
	}

	getTextFromDocument, _ := GetTextFromDocxTool(documentPath)

	timeAgent, err := llmagent.New(llmagent.Config{
		Name:        "hello_time_agent",
		Model:       model,
		Description: "Extract customer information from documents",
		Instruction: SYSTEM_PROMPT,
		Tools: []tool.Tool{
			getTextFromDocument,
		},
	})
	if err != nil {
		log.Fatalf("Failed to create agent: %v", err)
	}

	// Use in-memory session service — no CLI, no server needed
	sessionService := session.InMemoryService()

	sess, err := sessionService.Create(ctx, &session.CreateRequest{
		AppName: "themisdatacrawler",
		UserID:  "system",
	})
	if err != nil {
		log.Fatalf("Failed to create session: %v", err)
	}

	r, err := runner.New(runner.Config{
		AppName:           "themisdatacrawler",
		Agent:             timeAgent,
		SessionService:    sessionService,
		AutoCreateSession: true,
	})
	if err != nil {
		log.Fatalf("Failed to create runner: %v", err)
	}

	// This is the message that kicks off the agent — tell it what to do
	input := genai.NewContentFromText(
		// fmt.Sprintf("Extract all customers from the document: %s", fileName),
		fmt.Sprintf("Extract all customers from the document located at: /workspace/themisdatacrawler/docs/Acto.de.Venta.Alfredo.Mateoaa.docx"),
		genai.RoleUser,
	)

	events := r.Run(ctx, "system", sess.Session.ID(), input, agent.RunConfig{
		StreamingMode: agent.StreamingModeNone,
	})

	for event, err := range events {
		if err != nil {
			log.Printf("Error during run: %v", err)
			break
		}

		// Guard: LLMResponse must exist and have content
		if event.LLMResponse.Content == nil {
			continue
		}

		content := event.LLMResponse.Content

		for _, part := range content.Parts {
			switch {
			case part.Text != "":
				if event.IsFinalResponse() {
					fmt.Printf("[FINAL][%s]: %s\n", event.Author, part.Text)
				} else {
					fmt.Printf("[THINKING][%s]: %s\n", event.Author, part.Text)
				}
			case part.FunctionCall != nil:
				fmt.Printf("[TOOL CALL][%s]: %s → args: %v\n",
					event.Author, part.FunctionCall.Name, part.FunctionCall.Args)
			case part.FunctionResponse != nil:
				fmt.Printf("[TOOL RESULT]: %s → %v\n",
					part.FunctionResponse.Name, part.FunctionResponse.Response)
			}
		}
	}
}
