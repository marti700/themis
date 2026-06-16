package crawler

import "strings"

// maritalStatusNorm maps common Spanish/variant forms to the expected English
// enum values so a misbehaving model can't cause a database error.
var maritalStatusNorm = map[string]string{
	// single
	"soltero":      "single",
	"soltera":      "single",
	"soltero/a":    "single",
	// married
	"casado":       "married",
	"casada":       "married",
	"casado/a":     "married",
	// divorced
	"divorciado":   "divorced",
	"divorciada":   "divorced",
	"divorciado/a": "divorced",
	// widowed
	"viudo":        "widowed",
	"viuda":        "widowed",
	"viudo/a":      "widowed",
	// legal_union
	"union libre":        "legal_union",
	"unión libre":        "legal_union",
	"union de hecho":     "legal_union",
	"unión de hecho":     "legal_union",
	"concubinato":        "legal_union",
	"conviviente":        "legal_union",
	"legal union":        "legal_union",
}

// genderNorm maps common variant/Spanish forms to the expected enum values.
var genderNorm = map[string]string{
	"masculino":  "masculin",
	"masculin":   "masculin",
	"mascul":     "masculin",
	"male":       "masculin",
	"hombre":     "masculin",
	"m":          "masculin",
	"femenino":  "femenin",
	"feminino":  "femenin",
	"feminin":   "femenin",
	"femin":     "femenin",
	"female":    "femenin",
	"mujer":     "femenin",
	"f":         "femenin",
}

// normalizeMaritalStatus returns the canonical English enum value for a marital
// status string. Returns the original value if it already matches or is unknown
// (the database will reject truly invalid values with a clear error).
func normalizeMaritalStatus(s *string) *string {
	if s == nil {
		return nil
	}
	key := strings.ToLower(strings.TrimSpace(*s))
	if canonical, ok := maritalStatusNorm[key]; ok {
		return &canonical
	}
	return s
}

// normalizeGender returns the canonical enum value for a gender string.
func normalizeGender(s *string) *string {
	if s == nil {
		return nil
	}
	key := strings.ToLower(strings.TrimSpace(*s))
	if canonical, ok := genderNorm[key]; ok {
		return &canonical
	}
	return s
}
