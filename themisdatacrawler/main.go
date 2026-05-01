package main

import (
	"log"
	"os"

	"github.com/marti700/themisdatacrawler/agents/crawler"
)

func main() {
	docsDir := "docs"

	entries, err := os.ReadDir(docsDir)
	if err != nil {
		log.Fatalf("Failed to read docs directory %q: %v", docsDir, err)
	}

	for _, entry := range entries {
		if entry.IsDir() {
			continue
		}

		// docPath := filepath.Join(docsDir, entry.Name())
		// crawler.Crawl(docPath)
		crawler.Test("/workspace/themisdatacrawler/docs/Acto.de.Venta.Alfredo.Mateoaa.docx")
	}
}
