package crawler

import (
	"log"

	"github.com/lu4p/cat"
	"google.golang.org/adk/tool"
	"google.golang.org/adk/tool/functiontool"
)

// Export fields (Capitalize) so the ADK reflection can read them for the LLM schema
type GetTextFromDocxInput struct {
	Path string `json:"path" jsonschema:description=The path were the documet is located`
}

type GetTextFromDocxOutput struct {
	Data string `json:"data" jsonschema:description=the document text as a string`
}

func Test(path string) string {
	data, err := cat.File(path)
	log.Printf("%s", data)
	if err != nil {
		return ""
	}
	return data
}

func GetTextFromDocxTool(path string) (tool.Tool, error) {
	getTextFromDoc := func(ctx tool.Context, input GetTextFromDocxInput) (GetTextFromDocxOutput, error) {
		data, err := cat.File(input.Path)
		log.Printf("%s", data)
		if err != nil {
			return GetTextFromDocxOutput{}, err
		}
		return GetTextFromDocxOutput{Data: data}, nil
	}

	return functiontool.New(
		functiontool.Config{
			Name:        "get_text_from_docx",
			Description: "Extracts all text from a .docx file located at the provided path.",
		},
		getTextFromDoc,
	)
}
