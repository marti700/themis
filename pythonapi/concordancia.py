"""Spanish gender/number agreement (concordancia) helpers for party
descriptions in notarial acts and contracts.

Every party role (sellers, buyers, declarants, witnesses, ...) is a list of
people, never a fixed count of 1 or 2 — every function here operates on the
full list, however long.
"""

from docxtpl import RichText

GENDER_M = "masculin"
GENDER_F = "femenin"


def agree(masc: str, fem: str, gender: str | None) -> str:
    """Pick the form matching `gender`; falls back to the ambiguous "(a)"
    hack (matching the templates' pre-existing convention) when unknown."""
    if gender == GENDER_F:
        return fem
    if gender == GENDER_M:
        return masc
    return f"{masc}(a)"


MARITAL_STATUS_ES: dict[str, tuple[str, str]] = {
    "single": ("soltero", "soltera"),
    "married": ("casado", "casada"),
    "divorced": ("divorciado", "divorciada"),
    "widowed": ("viudo", "viuda"),
    "legal_union": ("en unión libre", "en unión libre"),
}


def marital_status_es(raw: str | None, gender: str | None) -> str:
    """Translate the DB's English marital-status code to gendered Spanish.
    Text that isn't one of the known codes (e.g. already hand-typed Spanish
    via the placeholder editor) passes through unchanged."""
    if not raw:
        return ""
    pair = MARITAL_STATUS_ES.get(raw.strip().lower())
    if pair is None:
        return raw
    masc, fem = pair
    if gender == GENDER_F:
        return fem
    if gender == GENDER_M:
        return masc
    return masc if masc == fem else f"{masc}(a)"


def describe_party(p) -> str:
    """One person's descriptive clause, gender-agreeing wherever their
    gender is known. `p` is a CustomerInfo/HeirInfo-like object exposing
    nationality, marital_status, occupation, id_number, address, gender."""
    parts: list[str] = []
    if p.nationality:
        # Nationality agrees with the feminine noun "nacionalidad", not with
        # the person — stays feminine regardless of gender (see
        # themisdatacrawler ResolveNationality for the same convention).
        parts.append(f"de nacionalidad {p.nationality}")
    parts.append("mayor de edad")
    ms = marital_status_es(getattr(p, "marital_status", None), p.gender)
    if ms:
        parts.append(ms)
    if p.occupation:
        parts.append(p.occupation)
    if p.id_number:
        portador = agree("portador", "portadora", p.gender)
        parts.append(f"{portador} de la cédula de identidad y electoral No. {p.id_number}")
    if p.address:
        domiciliado = agree("domiciliado", "domiciliada", p.gender)
        parts.append(f"{domiciliado} y residente en {p.address}")
    return ", ".join(parts)


def party_block(people) -> RichText:
    """Bold-upper name + gendered description per person, separated by
    '; ' — the direct replacement for the old shared-adjective sentence."""
    rt = RichText()
    for i, p in enumerate(people):
        if i:
            rt.add("; ")
        rt.add(f"{p.first_name} {p.last_name}".upper(), bold=True)
        rt.add(f", {describe_party(p)}")
    return rt


def quien(n: int) -> str:
    return "quien" if n == 1 else "quienes"


def sigue(n: int) -> str:
    return "sigue" if n == 1 else "siguen"


def denominar(n: int) -> str:
    return "se denominará" if n == 1 else "se denominarán"


def ha(n: int) -> str:
    return "ha" if n == 1 else "han"


def pick(n: int, singular: str, plural: str) -> str:
    """Generic singular/plural picker for one-off verbs tied to a party
    count (e.g. "acepta"/"aceptan", "da"/"dan")."""
    return singular if n == 1 else plural


def _composition(people) -> str | None:
    """None if no one's gender is known; GENDER_F only if every person with
    a known gender is a woman; GENDER_M otherwise (mixed, all men, or an
    unknown gender alongside at least one man)."""
    known = {p.gender for p in people if p.gender in (GENDER_M, GENDER_F)}
    if not known:
        return None
    return GENDER_F if known == {GENDER_F} else GENDER_M


# (masculine singular, feminine singular, masculine plural, feminine plural)
# — the 4 forms of each role denomination, matching today's fixed defaults
# in builder.templ (e.g. denomDefault: 'LOS VENDEDORES').
DENOMINATIONS: dict[str, tuple[str, str, str, str]] = {
    "VENDEDOR": ("EL VENDEDOR", "LA VENDEDORA", "LOS VENDEDORES", "LAS VENDEDORAS"),
    "COMPRADOR": ("EL COMPRADOR", "LA COMPRADORA", "LOS COMPRADORES", "LAS COMPRADORAS"),
    "PROPIETARIO": ("EL PROPIETARIO", "LA PROPIETARIA", "LOS PROPIETARIOS", "LAS PROPIETARIAS"),
    "INQUILINO": ("EL INQUILINO", "LA INQUILINA", "LOS INQUILINOS", "LAS INQUILINAS"),
    "PODERDANTE": ("EL PODERDANTE", "LA PODERDANTE", "LOS PODERDANTES", "LAS PODERDANTES"),
    "APODERADO": ("EL APODERADO", "LA APODERADA", "LOS APODERADOS", "LAS APODERADAS"),
    "COMPARECIENTE": ("EL COMPARECIENTE", "LA COMPARECIENTE", "LOS COMPARECIENTES", "LAS COMPARECIENTES"),
    "DECLARANTE": ("EL DECLARANTE", "LA DECLARANTE", "LOS DECLARANTES", "LAS DECLARANTES"),
    "AUTORIZANTE": ("EL AUTORIZANTE", "LA AUTORIZANTE", "LOS AUTORIZANTES", "LAS AUTORIZANTES"),
    "ACEPTANTE": ("EL ACEPTANTE", "LA ACEPTANTE", "LOS ACEPTANTES", "LAS ACEPTANTES"),
}


def resolve_denomination(raw: str, role: str, people) -> str:
    """If `raw` is still one of the role's known defaults, recompute it in
    the correct gender/number for `people`; if the caller customized it to
    other text, leave it untouched."""
    forms = DENOMINATIONS.get(role)
    if not people or forms is None or raw.strip().upper() not in forms:
        return raw
    masc_s, fem_s, masc_p, fem_p = forms
    comp = _composition(people)
    if len(people) == 1:
        if comp == GENDER_F:
            return fem_s
        if comp == GENDER_M:
            return masc_s
        return raw  # unknown gender, single person: don't guess
    return fem_p if comp == GENDER_F else masc_p
