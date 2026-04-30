package main

import (
	"bytes"
	"context"
	"encoding/json"
	"log"
	"net/http"
	"strconv"

	"github.com/gorilla/mux"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/marti700/themis/internal/database"
)

func main() {

	conPool, err := pgxpool.New(context.Background(), "postgres://themis:themis_dev@postgres:5432/themis?sslmode=disable")
	if err != nil {
		log.Fatal("Bobo!! Error during db conection", err.Error())
	}

	queries := database.New(conPool)

	createCustomer := func(query *database.Queries) func(http.ResponseWriter, *http.Request) {
		return func(w http.ResponseWriter, r *http.Request) {
			if r.Method != http.MethodPost {
				log.Println("1")
				w.WriteHeader(http.StatusMethodNotAllowed)
				return
			}
			var params database.CreateCustomerParams
			json.NewDecoder(r.Body).Decode(&params)
			cus, err := query.CreateCustomer(r.Context(), &params)
			log.Printf("%v", cus)
			if err != nil {
				http.Error(w, err.Error(), http.StatusInternalServerError)
				return
			}

			var buf bytes.Buffer
			err = json.NewEncoder(&buf).Encode(cus)

			if err != nil {
				log.Printf("Error during parsing of database returned customer, %s", err.Error())
				http.Error(w, "Error parsing customer", http.StatusInternalServerError)
			}

			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusCreated)
			w.Write(buf.Bytes())
		}
	}

	getCustomer := func(queries *database.Queries) func(http.ResponseWriter, *http.Request) {
		return func(w http.ResponseWriter, r *http.Request) {
			if r.Method != http.MethodGet {
				w.WriteHeader(http.StatusMethodNotAllowed)
				return
			}

			cusId, err := strconv.Atoi(mux.Vars(r)["id"])
			if err != nil {
				http.Error(w, "Invalid id, it must be a valid number", http.StatusInternalServerError)
				return
			}

			cus, err := queries.GetCustomer(r.Context(), int32(cusId))
			if err != nil {
				log.Printf("Error getting user from database %s", err.Error())
			}

			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			if err := json.NewEncoder(w).Encode(&cus); err != nil {
				log.Printf("Bulto!! JSON decode error: %v", err)
				log.Printf("Bulto!! Here is what we are trying to parse: %v", cus)
				http.Error(w, "Check your JSON format: "+err.Error(), http.StatusInternalServerError)
				return
			}
		}
	}

	r := mux.NewRouter()
	r.HandleFunc("/users", createCustomer(queries))
	r.HandleFunc("/users/{id}", getCustomer(queries))

	err = http.ListenAndServe(":9094", r)
	if err != nil {
		log.Fatal(err.Error())
	}
}
