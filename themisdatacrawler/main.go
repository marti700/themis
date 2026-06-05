package main

// main.go is the entry point for the themisdatacrawler program.
// It reads every file from the "docs/" directory and runs the AI crawler
// agent on each one to extract customer information.

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"path/filepath" // filepath.Join builds OS-correct paths, e.g. "docs/file.docx"
	"strings"

	// Our own crawler package defined in agents/crawler/
	"github.com/marti700/themisdatacrawler/agents/crawler"
)

func main() {
	// docsDir is the folder where .docx files to be crawled are stored.
	// The path is relative to where you run the binary from (the module root).
	docsDir := "docs"

	// os.ReadDir lists all entries (files and subdirectories) in docsDir.
	// It returns an error if the directory does not exist or cannot be read.
	entries, err := os.ReadDir(docsDir)
	if err != nil {
		log.Fatalf("Failed to read docs directory %q: %v", docsDir, err)
	}

	cusCache := make(map[string]bool)
	for _, entry := range entries {
		// Skip subdirectories — we only want files.
		if entry.IsDir() {
			continue
		}

		// Build the full relative path to the file, e.g. "docs/contract.docx".
		docPath := filepath.Join(docsDir, entry.Name())
		fmt.Println(docPath)

		// RunCrawlerAgent sends the document to the local NIM (the Llama model),
		// lets the model read the file via the get_text_from_docx tool, and
		// returns the list of customers it found in the document.
		customers, err := crawler.RunCrawlerAgent(docPath)
		if err != nil {
			// Log the error and continue to the next file rather than aborting.
			log.Printf("Error crawling %s: %v", docPath, err)
			continue
		}

		// Print each extracted customer.  %+v includes field names in the output,
		// e.g. {FirstName:John LastName:Doe ...}
		fmt.Println("Customers number", len(customers))
		for _, c := range customers {
			_, ok := cusCache[strings.TrimSpace(c.IDNumber)]
			if ok || c.IDNumber == "" {
				fmt.Println("No va")
				continue
			}
			// fmt.Println("Tamo aqui")
			body, err := json.Marshal(c)
			// fmt.Println(c)
			if err != nil {
				log.Printf("Error marshalling customer: %v", err)
				continue
			}

			request, err := http.NewRequest("POST", "http://localhost:9094/users", bytes.NewBuffer(body))
			if err != nil {
				log.Printf("Error creating request: %v", err)
				continue
			}
			request.Header.Set("Content-type", "application/json")

			resp, err := http.DefaultClient.Do(request)
			if err != nil {
				log.Printf("Error sending customer: %v", err)
				continue
			}
			if resp.StatusCode != http.StatusCreated {
				var errBody bytes.Buffer
				errBody.ReadFrom(resp.Body)
				log.Printf("Backend rejected customer (HTTP %d): %s | customer: %+v", resp.StatusCode, errBody.String(), c)
			} else {
				cusCache[strings.TrimSpace(c.IDNumber)] = true
				log.Printf("Customer %s saved: %+v", c.FirstName, c)
			}
			resp.Body.Close()
		}
	}
}
