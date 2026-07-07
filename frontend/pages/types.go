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
	return signerNames(d.Sellers, d.Buyers)
}

type RentContractPreviewData struct {
	Owners             []PartyPreview
	Tenants            []PartyPreview
	OwnerDenomination  string
	TenantDenomination string
}

// SignerNames returns the upper-cased full names of every party in the document
// (owners followed by tenants), joined as "A, B y C". Empty if there are none.
func (d RentContractPreviewData) SignerNames() string {
	return signerNames(d.Owners, d.Tenants)
}

func signerNames(a, b []PartyPreview) string {
	names := make([]string, 0, len(a)+len(b))
	for _, p := range a {
		if full := strings.TrimSpace(p.FirstName + " " + p.LastName); full != "" {
			names = append(names, strings.ToUpper(full))
		}
	}
	for _, p := range b {
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
