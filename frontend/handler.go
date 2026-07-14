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

func toParties(in []previewParty) []pages.PartyPreview {
	out := make([]pages.PartyPreview, len(in))
	for i, p := range in {
		out[i] = pages.PartyPreview{
			FirstName:     p.FirstName,
			LastName:      p.LastName,
			IDNumber:      p.IDNumber,
			Address:       p.Address,
			Nationality:   p.Nationality,
			MaritalStatus: p.MaritalStatus,
			Occupation:    p.Occupation,
		}
	}
	return out
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

type poderPreviewRequest struct {
	Poderdantes            []previewParty `json:"poderdantes"`
	Apoderados             []previewParty `json:"apoderados"`
	Witnesses              []previewParty `json:"witnesses"`
	PoderdanteDenomination string         `json:"poderdante_denomination"`
	ApoderadoDenomination  string         `json:"apoderado_denomination"`
}

func (h *Handler) PoderEspecialPreview(w http.ResponseWriter, r *http.Request) {
	var req poderPreviewRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request", http.StatusBadRequest)
		return
	}

	data := pages.PoderEspecialPreviewData{
		Poderdantes:            toParties(req.Poderdantes),
		Apoderados:             toParties(req.Apoderados),
		Witnesses:              toParties(req.Witnesses),
		PoderdanteDenomination: req.PoderdanteDenomination,
		ApoderadoDenomination:  req.ApoderadoDenomination,
	}

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := pages.PoderEspecialPreview(data).Render(r.Context(), w); err != nil {
		log.Printf("Error rendering poder especial preview: %v", err)
	}
}

type declaracionPreviewRequest struct {
	Declarants            []previewParty `json:"declarants"`
	Witnesses             []previewParty `json:"witnesses"`
	DeclarantDenomination string         `json:"declarant_denomination"`
	Clauses               []string       `json:"clauses"`
}

func (h *Handler) DeclaracionJuradaPreview(w http.ResponseWriter, r *http.Request) {
	var req declaracionPreviewRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request", http.StatusBadRequest)
		return
	}

	data := pages.DeclaracionJuradaPreviewData{
		Declarants:            toParties(req.Declarants),
		Witnesses:             toParties(req.Witnesses),
		DeclarantDenomination: req.DeclarantDenomination,
		Clauses:               req.Clauses,
	}

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := pages.DeclaracionJuradaPreview(data).Render(r.Context(), w); err != nil {
		log.Printf("Error rendering declaracion jurada preview: %v", err)
	}
}

func (h *Handler) ActoNotoriedadPreview(w http.ResponseWriter, r *http.Request) {
	var req declaracionPreviewRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request", http.StatusBadRequest)
		return
	}

	data := pages.ActoNotoriedadPreviewData{
		Declarants:            toParties(req.Declarants),
		Witnesses:             toParties(req.Witnesses),
		DeclarantDenomination: req.DeclarantDenomination,
		Clauses:               req.Clauses,
	}

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := pages.ActoNotoriedadPreview(data).Render(r.Context(), w); err != nil {
		log.Printf("Error rendering acto notoriedad preview: %v", err)
	}
}

type heirPreviewInput struct {
	FirstName     string `json:"first_name"`
	LastName      string `json:"last_name"`
	IDNumber      string `json:"id_number"`
	Nationality   string `json:"nationality"`
	MaritalStatus string `json:"marital_status"`
	Occupation    string `json:"occupation"`
	Address       string `json:"address"`
	BirthDate     string `json:"birth_date"`
	BirthRecord   string `json:"birth_record"`
}

func toHeirs(in []heirPreviewInput) []pages.HeirPreview {
	out := make([]pages.HeirPreview, len(in))
	for i, h := range in {
		out[i] = pages.HeirPreview{
			FirstName:     h.FirstName,
			LastName:      h.LastName,
			IDNumber:      h.IDNumber,
			Nationality:   h.Nationality,
			MaritalStatus: h.MaritalStatus,
			Occupation:    h.Occupation,
			Address:       h.Address,
			BirthDate:     h.BirthDate,
			BirthRecord:   h.BirthRecord,
		}
	}
	return out
}

type herederosPreviewRequest struct {
	Declarants            []previewParty     `json:"declarants"`
	Witnesses             []previewParty     `json:"witnesses"`
	DeclarantDenomination string             `json:"declarant_denomination"`
	Heirs                 []heirPreviewInput `json:"heirs"`
	Clauses               []string           `json:"clauses"`
}

func (h *Handler) DeterminacionHerederosPreview(w http.ResponseWriter, r *http.Request) {
	var req herederosPreviewRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request", http.StatusBadRequest)
		return
	}

	data := pages.DeterminacionHerederosPreviewData{
		Declarants:            toParties(req.Declarants),
		Witnesses:             toParties(req.Witnesses),
		DeclarantDenomination: req.DeclarantDenomination,
		Heirs:                 toHeirs(req.Heirs),
		Clauses:               req.Clauses,
	}

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := pages.DeterminacionHerederosPreview(data).Render(r.Context(), w); err != nil {
		log.Printf("Error rendering determinacion herederos preview: %v", err)
	}
}

type viajeMenorPreviewRequest struct {
	Authorizers            []previewParty `json:"authorizers"`
	Acceptors              []previewParty `json:"acceptors"`
	Witnesses              []previewParty `json:"witnesses"`
	AuthorizerDenomination string         `json:"authorizer_denomination"`
	AcceptorDenomination   string         `json:"acceptor_denomination"`
}

func (h *Handler) AutorizacionViajeMenorPreview(w http.ResponseWriter, r *http.Request) {
	var req viajeMenorPreviewRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request", http.StatusBadRequest)
		return
	}

	data := pages.AutorizacionViajeMenorPreviewData{
		Authorizers:            toParties(req.Authorizers),
		Acceptors:              toParties(req.Acceptors),
		Witnesses:              toParties(req.Witnesses),
		AuthorizerDenomination: req.AuthorizerDenomination,
		AcceptorDenomination:   req.AcceptorDenomination,
	}

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := pages.AutorizacionViajeMenorPreview(data).Render(r.Context(), w); err != nil {
		log.Printf("Error rendering autorizacion viaje menor preview: %v", err)
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
