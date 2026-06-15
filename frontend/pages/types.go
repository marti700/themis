package pages

type PartyPreview struct {
	FirstName     string
	LastName      string
	IDNumber      string
	Address       string
	MaritalStatus string
	Occupation    string
}

type SellContractPreviewData struct {
	Sellers            []PartyPreview
	Buyers             []PartyPreview
	SellerDenomination string
	BuyerDenomination  string
}
