package pages

import "strings"

type PartyPreview struct {
	FirstName     string
	LastName      string
	IDNumber      string
	Address       string
	Nationality   string
	MaritalStatus string
	Occupation    string
}

type SellContractPreviewData struct {
	Sellers            []PartyPreview
	Buyers             []PartyPreview
	SellerDenomination string
	BuyerDenomination  string
}

// SignerNames returns the upper-cased full names of every party in the document
// (sellers followed by buyers), joined as "A, B y C". Empty if there are none.
func (d SellContractPreviewData) SignerNames() string {
	names := make([]string, 0, len(d.Sellers)+len(d.Buyers))
	for _, p := range d.Sellers {
		if full := strings.TrimSpace(p.FirstName + " " + p.LastName); full != "" {
			names = append(names, strings.ToUpper(full))
		}
	}
	for _, p := range d.Buyers {
		if full := strings.TrimSpace(p.FirstName + " " + p.LastName); full != "" {
			names = append(names, strings.ToUpper(full))
		}
	}
	switch len(names) {
	case 0:
		return ""
	case 1:
		return names[0]
	default:
		return strings.Join(names[:len(names)-1], ", ") + " y " + names[len(names)-1]
	}
}
