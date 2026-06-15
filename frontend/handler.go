package frontend

import (
	"log"
	"net/http"
	"strconv"

	"github.com/gorilla/mux"
	"github.com/marti700/themis/backend/database"
	"github.com/marti700/themis/frontend/pages"
)

type Handler struct {
	Queries database.Querier
}

func (h *Handler) CustomerDirectory(w http.ResponseWriter, r *http.Request) {
	customers, err := h.Queries.ListCustomers(r.Context())
	if err != nil {
		log.Printf("Error listing customers: %v", err)
		http.Error(w, "Failed to load customers", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := pages.Directory(customers).Render(r.Context(), w); err != nil {
		log.Printf("Error rendering directory: %v", err)
	}
}

func (h *Handler) CustomerProfile(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.Atoi(mux.Vars(r)["id"])
	if err != nil {
		http.Error(w, "Invalid customer ID", http.StatusBadRequest)
		return
	}

	customer, err := h.Queries.GetCustomer(r.Context(), int32(id))
	if err != nil {
		log.Printf("Error getting customer %d: %v", id, err)
		http.Error(w, "Customer not found", http.StatusNotFound)
		return
	}

	docs, err := h.Queries.GetDocumentsByCustomer(r.Context(), int32(id))
	if err != nil {
		log.Printf("Error getting documents for customer %d: %v", id, err)
		docs = []database.Document{}
	}

	companies, err := h.Queries.GetCompaniesByCustomer(r.Context(), int32(id))
	if err != nil {
		log.Printf("Error getting companies for customer %d: %v", id, err)
		companies = []database.Company{}
	}

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := pages.Profile(pages.ProfileData{
		Customer:  customer,
		Documents: docs,
		Companies: companies,
	}).Render(r.Context(), w); err != nil {
		log.Printf("Error rendering profile: %v", err)
	}
}
