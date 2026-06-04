package customer

import (
	"bytes"
	"encoding/json"
	"log"
	"net/http"
	"strconv"

	"github.com/gorilla/mux"
	"github.com/marti700/themis/internal/database"
)

type Handler struct {
	Queries database.Querier
}

func (h *Handler) Create(w http.ResponseWriter, r *http.Request) {
	var params database.CreateCustomerParams
	json.NewDecoder(r.Body).Decode(&params)

	cus, err := h.Queries.CreateCustomer(r.Context(), &params)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	var buf bytes.Buffer
	if err = json.NewEncoder(&buf).Encode(cus); err != nil {
		log.Printf("Error encoding created customer: %s", err.Error())
		http.Error(w, "Error parsing customer", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	w.Write(buf.Bytes())
}

func (h *Handler) Get(w http.ResponseWriter, r *http.Request) {
	cusID, err := strconv.Atoi(mux.Vars(r)["id"])
	if err != nil {
		http.Error(w, "Invalid id, it must be a valid number", http.StatusBadRequest)
		return
	}

	cus, err := h.Queries.GetCustomer(r.Context(), int32(cusID))
	if err != nil {
		log.Printf("Error getting customer from database: %s", err.Error())
		http.Error(w, "Customer not found", http.StatusNotFound)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	if err := json.NewEncoder(w).Encode(&cus); err != nil {
		log.Printf("JSON encode error: %v", err)
	}
}
