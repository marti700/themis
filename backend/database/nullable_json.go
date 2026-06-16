package database

import "encoding/json"

// UnmarshalJSON lets NullGender decode a plain JSON string (e.g. "masculin")
// in addition to the default struct form. A JSON null sets Valid=false.
func (ns *NullGender) UnmarshalJSON(data []byte) error {
	if string(data) == "null" {
		ns.Valid = false
		return nil
	}
	var s string
	if err := json.Unmarshal(data, &s); err != nil {
		return err
	}
	ns.Gender = Gender(s)
	ns.Valid = s != ""
	return nil
}

// UnmarshalJSON lets NullMaritalStatus decode a plain JSON string (e.g. "married")
// in addition to the default struct form. A JSON null sets Valid=false.
func (ns *NullMaritalStatus) UnmarshalJSON(data []byte) error {
	if string(data) == "null" {
		ns.Valid = false
		return nil
	}
	var s string
	if err := json.Unmarshal(data, &s); err != nil {
		return err
	}
	ns.MaritalStatus = MaritalStatus(s)
	ns.Valid = s != ""
	return nil
}
