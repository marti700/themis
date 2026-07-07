package frontend

import (
	"encoding/json"
	"log"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/gorilla/mux"
	"github.com/jackc/pgx/v5/pgtype"
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

func (h *Handler) CustomerRegisterForm(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := pages.CustomerRegister("").Render(r.Context(), w); err != nil {
		log.Printf("Error rendering customer register form: %v", err)
	}
}

// optText converts a form value into a nullable text column: empty stays NULL.
func optText(v string) pgtype.Text {
	v = strings.TrimSpace(v)
	return pgtype.Text{String: v, Valid: v != ""}
}

func (h *Handler) CustomerCreate(w http.ResponseWriter, r *http.Request) {
	if err := r.ParseForm(); err != nil {
		http.Error(w, "Invalid form data", http.StatusBadRequest)
		return
	}

	renderErr := func(msg string) {
		w.Header().Set("Content-Type", "text/html; charset=utf-8")
		if err := pages.CustomerRegister(msg).Render(r.Context(), w); err != nil {
			log.Printf("Error rendering customer register form: %v", err)
		}
	}

	firstName := strings.TrimSpace(r.FormValue("first_name"))
	lastName := strings.TrimSpace(r.FormValue("last_name"))
	idNumber := strings.TrimSpace(r.FormValue("id_number"))
	if firstName == "" || lastName == "" || idNumber == "" {
		renderErr("First name, last name, and document ID are required.")
		return
	}

	params := database.CreateCustomerParams{
		FirstName:   firstName,
		LastName:    lastName,
		IDNumber:    idNumber,
		Nationality: optText(r.FormValue("nationality")),
		Occupation:  optText(r.FormValue("occupation")),
		Address:     optText(r.FormValue("address")),
	}

	if b := strings.TrimSpace(r.FormValue("birthday")); b != "" {
		t, err := time.Parse("2006-01-02", b)
		if err != nil {
			renderErr("Invalid date of birth.")
			return
		}
		params.Birthday = pgtype.Date{Time: t, Valid: true}
	}

	if g := strings.TrimSpace(r.FormValue("gender")); g != "" {
		params.Gender = database.NullGender{Gender: database.Gender(g), Valid: true}
	}

	if ms := strings.TrimSpace(r.FormValue("marital_status")); ms != "" {
		params.MaritalStatus = database.NullMaritalStatus{MaritalStatus: database.MaritalStatus(ms), Valid: true}
	}

	if _, err := h.Queries.CreateCustomer(r.Context(), &params); err != nil {
		log.Printf("Error creating customer: %v", err)
		renderErr("Could not save customer. The document ID may already be registered.")
		return
	}

	http.Redirect(w, r, "/customers", http.StatusSeeOther)
}

func (h *Handler) DocumentBuilder(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := pages.Builder().Render(r.Context(), w); err != nil {
		log.Printf("Error rendering builder: %v", err)
	}
}

type previewParty struct {
	FirstName     string `json:"first_name"`
	LastName      string `json:"last_name"`
	IDNumber      string `json:"id_number"`
	Address       string `json:"address"`
	Nationality   string `json:"nationality"`
	MaritalStatus string `json:"marital_status"`
	Occupation    string `json:"occupation"`
}

type previewRequest struct {
	Sellers            []previewParty `json:"sellers"`
	Buyers             []previewParty `json:"buyers"`
	SellerDenomination string         `json:"seller_denomination"`
	BuyerDenomination  string         `json:"buyer_denomination"`
}

func (h *Handler) SellContractPreview(w http.ResponseWriter, r *http.Request) {
	var req previewRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request", http.StatusBadRequest)
		return
	}

	toParty := func(p previewParty) pages.PartyPreview {
		return pages.PartyPreview{
			FirstName:     p.FirstName,
			LastName:      p.LastName,
			IDNumber:      p.IDNumber,
			Address:       p.Address,
			Nationality:   p.Nationality,
			MaritalStatus: p.MaritalStatus,
			Occupation:    p.Occupation,
		}
	}

	sellers := make([]pages.PartyPreview, len(req.Sellers))
	for i, s := range req.Sellers {
		sellers[i] = toParty(s)
	}
	buyers := make([]pages.PartyPreview, len(req.Buyers))
	for i, b := range req.Buyers {
		buyers[i] = toParty(b)
	}

	data := pages.SellContractPreviewData{
		Sellers:            sellers,
		Buyers:             buyers,
		SellerDenomination: req.SellerDenomination,
		BuyerDenomination:  req.BuyerDenomination,
	}

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := pages.SellContractPreview(data).Render(r.Context(), w); err != nil {
		log.Printf("Error rendering sell contract preview: %v", err)
	}
}

type rentPreviewRequest struct {
	Owners             []previewParty `json:"owners"`
	Tenants            []previewParty `json:"tenants"`
	OwnerDenomination  string         `json:"owner_denomination"`
	TenantDenomination string         `json:"tenant_denomination"`
}

func (h *Handler) RentContractPreview(w http.ResponseWriter, r *http.Request) {
	var req rentPreviewRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request", http.StatusBadRequest)
		return
	}

	toParty := func(p previewParty) pages.PartyPreview {
		return pages.PartyPreview{
			FirstName:     p.FirstName,
			LastName:      p.LastName,
			IDNumber:      p.IDNumber,
			Address:       p.Address,
			Nationality:   p.Nationality,
			MaritalStatus: p.MaritalStatus,
			Occupation:    p.Occupation,
		}
	}

	owners := make([]pages.PartyPreview, len(req.Owners))
	for i, o := range req.Owners {
		owners[i] = toParty(o)
	}
	tenants := make([]pages.PartyPreview, len(req.Tenants))
	for i, t := range req.Tenants {
		tenants[i] = toParty(t)
	}

	data := pages.RentContractPreviewData{
		Owners:             owners,
		Tenants:            tenants,
		OwnerDenomination:  req.OwnerDenomination,
		TenantDenomination: req.TenantDenomination,
	}

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := pages.RentContractPreview(data).Render(r.Context(), w); err != nil {
		log.Printf("Error rendering rent contract preview: %v", err)
	}
}

func (h *Handler) CustomerListJSON(w http.ResponseWriter, r *http.Request) {
	customers, err := h.Queries.ListCustomers(r.Context())
	if err != nil {
		log.Printf("Error listing customers: %v", err)
		http.Error(w, `{"error":"Failed to load customers"}`, http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(customers); err != nil {
		log.Printf("Error encoding customers: %v", err)
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
