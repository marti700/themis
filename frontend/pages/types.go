package pages

import (
	"fmt"
	"strings"
)

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

// clauseOrdinals mirrors pythonapi's _ORDINALS so preview numbering matches the docx.
var clauseOrdinals = []string{
	"PRIMERO", "SEGUNDO", "TERCERO", "CUARTO", "QUINTO", "SEXTO", "SÉPTIMO", "OCTAVO",
	"NOVENO", "DÉCIMO", "DÉCIMO PRIMERO", "DÉCIMO SEGUNDO", "DÉCIMO TERCERO",
	"DÉCIMO CUARTO", "DÉCIMO QUINTO", "DÉCIMO SEXTO", "DÉCIMO SÉPTIMO", "DÉCIMO OCTAVO",
	"DÉCIMO NOVENO", "VIGÉSIMO",
}

func ordinalWord(i int) string {
	if i >= 0 && i < len(clauseOrdinals) {
		return clauseOrdinals[i]
	}
	return fmt.Sprintf("%dº", i+1)
}

// SignatureLine is one signer rendered in a notarial act's signature grid.
type SignatureLine struct {
	Name string
	Role string
}

// appendSigners adds each named party to lines under the given role label.
func appendSigners(lines []SignatureLine, parties []PartyPreview, role string) []SignatureLine {
	for _, p := range parties {
		if full := strings.TrimSpace(p.FirstName + " " + p.LastName); full != "" {
			lines = append(lines, SignatureLine{Name: strings.ToUpper(full), Role: role})
		}
	}
	return lines
}

// PoderEspecialPreviewData drives the Poder Especial draft preview.
type PoderEspecialPreviewData struct {
	Poderdantes            []PartyPreview
	Apoderados             []PartyPreview
	Witnesses              []PartyPreview
	PoderdanteDenomination string
	ApoderadoDenomination  string
}

func (d PoderEspecialPreviewData) SignerNames() string {
	principals := append(append([]PartyPreview{}, d.Poderdantes...), d.Apoderados...)
	return signerNames(principals, d.Witnesses)
}

func (d PoderEspecialPreviewData) Signatures() []SignatureLine {
	lines := appendSigners(nil, d.Poderdantes, "Poderdante")
	lines = appendSigners(lines, d.Apoderados, "Apoderado")
	return appendSigners(lines, d.Witnesses, "Testigo Instrumental")
}

// DeclaracionJuradaPreviewData drives the Declaración Jurada draft preview.
type DeclaracionJuradaPreviewData struct {
	Declarants            []PartyPreview
	Witnesses             []PartyPreview
	DeclarantDenomination string
	Clauses               []string
}

func (d DeclaracionJuradaPreviewData) SignerNames() string {
	return signerNames(d.Declarants, d.Witnesses)
}

func (d DeclaracionJuradaPreviewData) Signatures() []SignatureLine {
	lines := appendSigners(nil, d.Declarants, "Compareciente")
	return appendSigners(lines, d.Witnesses, "Testigo Instrumental")
}

// ActoNotoriedadPreviewData drives the Acto de Notoriedad draft preview — it is
// structurally identical to Declaración Jurada (N declarants + witnesses + a
// dynamic clause body), so the two share this type rather than duplicating it.
type ActoNotoriedadPreviewData = DeclaracionJuradaPreviewData

// HeirPreview is one heir row in a Determinación de Herederos preview — richer
// than PartyPreview because the act must state each heir's filiation (birth data).
type HeirPreview struct {
	FirstName     string
	LastName      string
	IDNumber      string
	Nationality   string
	MaritalStatus string
	Occupation    string
	Address       string
	BirthDate     string
	BirthRecord   string
}

// DeterminacionHerederosPreviewData drives the Determinación de Herederos draft
// preview.
type DeterminacionHerederosPreviewData struct {
	Declarants            []PartyPreview
	Witnesses             []PartyPreview
	DeclarantDenomination string
	Heirs                 []HeirPreview
	Clauses               []string
}

func (d DeterminacionHerederosPreviewData) SignerNames() string {
	return signerNames(d.Declarants, d.Witnesses)
}

func (d DeterminacionHerederosPreviewData) Signatures() []SignatureLine {
	lines := appendSigners(nil, d.Declarants, "Compareciente")
	return appendSigners(lines, d.Witnesses, "Testigo Instrumental")
}

// AutorizacionViajeMenorPreviewData drives the Autorización para Viaje de Menor
// draft preview.
type AutorizacionViajeMenorPreviewData struct {
	Authorizers            []PartyPreview
	Acceptors              []PartyPreview
	Witnesses              []PartyPreview
	AuthorizerDenomination string
	AcceptorDenomination   string
}

func (d AutorizacionViajeMenorPreviewData) SignerNames() string {
	principals := append(append([]PartyPreview{}, d.Authorizers...), d.Acceptors...)
	return signerNames(principals, d.Witnesses)
}

func (d AutorizacionViajeMenorPreviewData) Signatures() []SignatureLine {
	lines := appendSigners(nil, d.Authorizers, "Autorizador")
	lines = appendSigners(lines, d.Acceptors, "Aceptante")
	return appendSigners(lines, d.Witnesses, "Testigo Instrumental")
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
