package pages

import "strings"

// Spanish gender/number agreement (concordancia) helpers for the HTML draft
// previews — a Go port of pythonapi/concordancia.py so the on-screen preview
// matches the generated .docx. Every function operates on the full party
// list, however long (never assumes 1 or 2 people).

const (
	GenderM = "masculin"
	GenderF = "femenin"
)

// agree picks the form matching gender; falls back to the ambiguous "(a)"
// hack (matching the templates' pre-existing convention) when unknown.
func agree(masc, fem, gender string) string {
	switch gender {
	case GenderF:
		return fem
	case GenderM:
		return masc
	default:
		return masc + "(a)"
	}
}

var maritalStatusES = map[string][2]string{
	"single":      {"soltero", "soltera"},
	"married":     {"casado", "casada"},
	"divorced":    {"divorciado", "divorciada"},
	"widowed":     {"viudo", "viuda"},
	"legal_union": {"en unión libre", "en unión libre"},
}

// maritalStatusEs translates the DB's English marital-status code to
// gendered Spanish. Text that isn't one of the known codes (e.g. already
// hand-typed Spanish via the placeholder editor) passes through unchanged.
func maritalStatusEs(raw, gender string) string {
	if raw == "" {
		return ""
	}
	pair, ok := maritalStatusES[strings.ToLower(strings.TrimSpace(raw))]
	if !ok {
		return raw
	}
	switch gender {
	case GenderF:
		return pair[1]
	case GenderM:
		return pair[0]
	default:
		if pair[0] == pair[1] {
			return pair[0]
		}
		return pair[0] + "(a)"
	}
}

func quienWord(n int) string {
	if n == 1 {
		return "quien"
	}
	return "quienes"
}

func sigueWord(n int) string {
	if n == 1 {
		return "sigue"
	}
	return "siguen"
}

func denominarWord(n int) string {
	if n == 1 {
		return "se denominará"
	}
	return "se denominarán"
}

func haWord(n int) string {
	if n == 1 {
		return "ha"
	}
	return "han"
}

// pickWord is a generic singular/plural picker for one-off verbs tied to a
// party count (e.g. "acepta"/"aceptan", "da"/"dan").
func pickWord(n int, singular, plural string) string {
	if n == 1 {
		return singular
	}
	return plural
}

// composition returns "" if no one's gender is known, GenderF only if every
// person with a known gender is a woman, GenderM otherwise (mixed, all men,
// or an unknown gender alongside at least one man).
func composition(parties []PartyPreview) string {
	sawF, sawM := false, false
	for _, p := range parties {
		switch p.Gender {
		case GenderF:
			sawF = true
		case GenderM:
			sawM = true
		}
	}
	if !sawF && !sawM {
		return ""
	}
	if sawF && !sawM {
		return GenderF
	}
	return GenderM
}

// (masculine singular, feminine singular, masculine plural, feminine plural)
// — the 4 forms of each role denomination, matching today's fixed defaults
// in builder.templ (e.g. denomDefault: 'LOS VENDEDORES').
var denominations = map[string][4]string{
	"VENDEDOR":      {"EL VENDEDOR", "LA VENDEDORA", "LOS VENDEDORES", "LAS VENDEDORAS"},
	"COMPRADOR":     {"EL COMPRADOR", "LA COMPRADORA", "LOS COMPRADORES", "LAS COMPRADORAS"},
	"PROPIETARIO":   {"EL PROPIETARIO", "LA PROPIETARIA", "LOS PROPIETARIOS", "LAS PROPIETARIAS"},
	"INQUILINO":     {"EL INQUILINO", "LA INQUILINA", "LOS INQUILINOS", "LAS INQUILINAS"},
	"PODERDANTE":    {"EL PODERDANTE", "LA PODERDANTE", "LOS PODERDANTES", "LAS PODERDANTES"},
	"APODERADO":     {"EL APODERADO", "LA APODERADA", "LOS APODERADOS", "LAS APODERADAS"},
	"COMPARECIENTE": {"EL COMPARECIENTE", "LA COMPARECIENTE", "LOS COMPARECIENTES", "LAS COMPARECIENTES"},
	"DECLARANTE":    {"EL DECLARANTE", "LA DECLARANTE", "LOS DECLARANTES", "LAS DECLARANTES"},
	"AUTORIZANTE":   {"EL AUTORIZANTE", "LA AUTORIZANTE", "LOS AUTORIZANTES", "LAS AUTORIZANTES"},
	"ACEPTANTE":     {"EL ACEPTANTE", "LA ACEPTANTE", "LOS ACEPTANTES", "LAS ACEPTANTES"},
}

// ResolveDenomination recomputes `raw` in the correct gender/number for
// `parties` if it is still one of the role's known defaults; if the caller
// customized it to other text, it is left untouched.
func ResolveDenomination(raw, role string, parties []PartyPreview) string {
	forms, ok := denominations[role]
	if !ok || len(parties) == 0 {
		return raw
	}
	upper := strings.ToUpper(strings.TrimSpace(raw))
	matches := false
	for _, f := range forms {
		if f == upper {
			matches = true
			break
		}
	}
	if !matches {
		return raw
	}
	comp := composition(parties)
	if len(parties) == 1 {
		switch comp {
		case GenderF:
			return forms[1]
		case GenderM:
			return forms[0]
		default:
			return raw // unknown gender, single person: don't guess
		}
	}
	if comp == GenderF {
		return forms[3]
	}
	return forms[2]
}
