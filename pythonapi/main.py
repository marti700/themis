from io import BytesIO
from pathlib import Path

from docx import Document
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from docx.enum.text import WD_PARAGRAPH_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docxtpl import DocxTemplate
from fastapi import FastAPI, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

app = FastAPI(title="Themis Document API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

TEMPLATES_DIR = Path(__file__).parent / "docxtemplates"
STATIC_DIR = Path(__file__).parent / "static"


@app.get("/templates")
def list_templates():
    return [f.stem for f in sorted(TEMPLATES_DIR.glob("*.docx"))]
_SIG_SENTINEL = "__SIGNATURES__"


class CustomerInfo(BaseModel):
    first_name: str
    last_name: str
    id_number: str
    address: str | None = None
    marital_status: str | None = None
    occupation: str | None = None
    nationality: str | None = None  # not yet in customer DB model


class SellContractRequest(BaseModel):
    sellers: list[CustomerInfo]
    buyers: list[CustomerInfo]
    seller_denomination: str = "EL VENDEDOR"
    buyer_denomination: str = "EL COMPRADOR"
    property_description: str
    property_justification: str
    price_words: str
    price_rd: str
    originals_count_words: str
    originals_count: str
    city: str
    province: str
    day_words: str
    day_number: str
    month: str
    year_words: str
    year_number: str
    signers_list: str = ""
    firm_name: str
    firm_service: str
    notary_title_name: str
    notary_phone: str
    notary_municipality: str
    notary_registration: str


class RentContractRequest(BaseModel):
    owners: list[CustomerInfo]
    tenants: list[CustomerInfo]
    owner_denomination: str = "EL PROPIETARIO"
    tenant_denomination: str = "EL INQUILINO"
    property_description: str
    property_use: str = "vivienda"
    price_words: str
    price_rd: str
    deposit_words: str
    deposit_rd: str
    duration_words: str
    duration_number: str
    start_date: str
    end_date: str
    originals_count_words: str
    originals_count: str
    city: str
    province: str
    day_words: str
    day_number: str
    month: str
    year_words: str
    year_number: str
    signers_list: str = ""
    firm_name: str
    firm_service: str
    notary_title_name: str
    notary_phone: str
    notary_municipality: str
    notary_registration: str


def _set_table_borders_invisible(table):
    tbl = table._tbl
    tblPr = tbl.tblPr
    tblBorders = tblPr.find(qn("w:tblBorders"))
    if tblBorders is None:
        tblBorders = OxmlElement("w:tblBorders")
        tblPr.append(tblBorders)
    for border_name in ["top", "left", "bottom", "right", "insideH", "insideV"]:
        border_el = OxmlElement(f"w:{border_name}")
        border_el.set(qn("w:val"), "nil")
        tblBorders.append(border_el)


def inject_signatures(doc: Document, parties: list[tuple[str, str]]):
    """Replace the sentinel paragraph with two-column signature tables (name bold+underlined, role below)."""
    target_paragraph = None
    for paragraph in doc.paragraphs:
        if _SIG_SENTINEL in paragraph.text:
            target_paragraph = paragraph
            paragraph.text = ""
            break

    if target_paragraph is None:
        return

    # Build pairs; insert in reverse so addnext() preserves top-to-bottom order.
    pairs = [parties[i : i + 2] for i in range(0, len(parties), 2)]
    for chunk in reversed(pairs):
        table = doc.add_table(rows=1, cols=2)
        table.autofit = False
        _set_table_borders_invisible(table)
        target_paragraph._p.addnext(table._tbl)

        for col_index, (name, role) in enumerate(chunk):
            cell = table.cell(0, col_index)

            name_para = cell.paragraphs[0]
            name_para.alignment = WD_PARAGRAPH_ALIGNMENT.CENTER
            name_run = name_para.add_run(name)
            name_run.bold = True
            name_run.underline = True

            role_para = cell.add_paragraph()
            role_para.alignment = WD_PARAGRAPH_ALIGNMENT.CENTER
            role_para.add_run(role)

        spacer = doc.add_paragraph()
        table._tbl.addnext(spacer._p)


@app.post("/documents/sell_contract")
def generate_sell_contract(req: SellContractRequest):
    template_path = TEMPLATES_DIR / "sell_contract.docx"
    if not template_path.exists():
        raise HTTPException(status_code=404, detail="Template not found")

    # Pass 1 — docxtpl renders all text placeholders.
    # SIGNATURES is set to a sentinel so we can locate the paragraph in pass 2.
    tpl = DocxTemplate(template_path)
    context = {
        "BUFETE_NOMBRE": req.firm_name,
        "BUFETE_SERVICIO": req.firm_service,
        "NOTARIO_TITULO_NOMBRE": req.notary_title_name,
        "NOTARIO_TITULO_NOMBRE_MAYUS": req.notary_title_name.upper(),
        "NOTARIO_TELEFONO": req.notary_phone,
        "NOTARIO_MUNICIPIO": req.notary_municipality,
        "NOTARIO_MATRICULA": req.notary_registration,
        "VENDEDOR_NOMBRE": " y ".join(f"{s.first_name} {s.last_name}" for s in req.sellers),
        "VENDEDOR_NACIONALIDAD": ", ".join(s.nationality or "" for s in req.sellers),
        "VENDEDOR_ESTADO_CIVIL": ", ".join(s.marital_status or "" for s in req.sellers),
        "VENDEDOR_OCUPACION": ", ".join(s.occupation or "" for s in req.sellers),
        "VENDEDOR_CEDULA": ", ".join(s.id_number for s in req.sellers),
        "VENDEDOR_DOMICILIO": ", ".join(s.address or "" for s in req.sellers),
        "COMPRADOR_NOMBRE": " y ".join(f"{b.first_name} {b.last_name}" for b in req.buyers),
        "COMPRADOR_NACIONALIDAD": ", ".join(b.nationality or "" for b in req.buyers),
        "COMPRADOR_ESTADO_CIVIL": ", ".join(b.marital_status or "" for b in req.buyers),
        "COMPRADOR_OCUPACION": ", ".join(b.occupation or "" for b in req.buyers),
        "COMPRADOR_CEDULA": ", ".join(b.id_number for b in req.buyers),
        "COMPRADOR_DOMICILIO": ", ".join(b.address or "" for b in req.buyers),
        "DENOMINACION_VENDEDORES": req.seller_denomination,
        "DENOMINACION_COMPRADOR": req.buyer_denomination,
        "DESCRIPCION_INMUEBLE": req.property_description,
        "JUSTIFICACION_PROPIEDAD": req.property_justification,
        "PRECIO_LETRAS": req.price_words,
        "PRECIO_RD": req.price_rd,
        "CANTIDAD_ORIGINALES_LETRAS": req.originals_count_words,
        "CANTIDAD_ORIGINALES": req.originals_count,
        "CIUDAD_CONTRATO": req.city,
        "PROVINCIA_CONTRATO": req.province,
        "DIA_LETRAS": req.day_words,
        "DIA_NUM": req.day_number,
        "MES": req.month,
        "ANIO_LETRAS": req.year_words,
        "ANIO_NUM": req.year_number,
        "SIGNATURES": _SIG_SENTINEL,
        "FIRMANTES_LISTA": req.signers_list,
    }
    tpl.render(context)
    intermediate = BytesIO()
    tpl.save(intermediate)
    intermediate.seek(0)

    # Pass 2 — python-docx finds the sentinel and injects the signature table.
    doc = Document(intermediate)
    parties = [
        (f"{s.first_name} {s.last_name}", req.seller_denomination) for s in req.sellers
    ] + [
        (f"{b.first_name} {b.last_name}", req.buyer_denomination) for b in req.buyers
    ]
    inject_signatures(doc, parties)

    buf = BytesIO()
    doc.save(buf)
    buf.seek(0)

    headers = {"Content-Disposition": "attachment; filename=sell_contract.docx"}
    return StreamingResponse(
        buf,
        media_type="application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        headers=headers,
    )


@app.post("/documents/rent_contract")
def generate_rent_contract(req: RentContractRequest):
    template_path = TEMPLATES_DIR / "rent_contract.docx"
    if not template_path.exists():
        raise HTTPException(status_code=404, detail="Template not found")

    # Pass 1 — docxtpl renders all text placeholders.
    # SIGNATURES is set to a sentinel so we can locate the paragraph in pass 2.
    tpl = DocxTemplate(template_path)
    context = {
        "BUFETE_NOMBRE": req.firm_name,
        "BUFETE_SERVICIO": req.firm_service,
        "NOTARIO_TITULO_NOMBRE": req.notary_title_name,
        "NOTARIO_TITULO_NOMBRE_MAYUS": req.notary_title_name.upper(),
        "NOTARIO_TELEFONO": req.notary_phone,
        "NOTARIO_MUNICIPIO": req.notary_municipality,
        "NOTARIO_MATRICULA": req.notary_registration,
        "PROPIETARIO_NOMBRE": " y ".join(f"{o.first_name} {o.last_name}" for o in req.owners),
        "PROPIETARIO_NACIONALIDAD": ", ".join(o.nationality or "" for o in req.owners),
        "PROPIETARIO_ESTADO_CIVIL": ", ".join(o.marital_status or "" for o in req.owners),
        "PROPIETARIO_OCUPACION": ", ".join(o.occupation or "" for o in req.owners),
        "PROPIETARIO_CEDULA": ", ".join(o.id_number for o in req.owners),
        "PROPIETARIO_DOMICILIO": ", ".join(o.address or "" for o in req.owners),
        "INQUILINO_NOMBRE": " y ".join(f"{t.first_name} {t.last_name}" for t in req.tenants),
        "INQUILINO_NACIONALIDAD": ", ".join(t.nationality or "" for t in req.tenants),
        "INQUILINO_ESTADO_CIVIL": ", ".join(t.marital_status or "" for t in req.tenants),
        "INQUILINO_OCUPACION": ", ".join(t.occupation or "" for t in req.tenants),
        "INQUILINO_CEDULA": ", ".join(t.id_number for t in req.tenants),
        "INQUILINO_DOMICILIO": ", ".join(t.address or "" for t in req.tenants),
        "DENOMINACION_PROPIETARIO": req.owner_denomination,
        "DENOMINACION_INQUILINO": req.tenant_denomination,
        "DESCRIPCION_INMUEBLE": req.property_description,
        "USO_INMUEBLE": req.property_use,
        "PRECIO_LETRAS": req.price_words,
        "PRECIO_RD": req.price_rd,
        "DEPOSITO_LETRAS": req.deposit_words,
        "DEPOSITO_RD": req.deposit_rd,
        "DURACION_LETRAS": req.duration_words,
        "DURACION_NUM": req.duration_number,
        "FECHA_INICIO": req.start_date,
        "FECHA_FIN": req.end_date,
        "CANTIDAD_ORIGINALES_LETRAS": req.originals_count_words,
        "CANTIDAD_ORIGINALES": req.originals_count,
        "CIUDAD_CONTRATO": req.city,
        "PROVINCIA_CONTRATO": req.province,
        "DIA_LETRAS": req.day_words,
        "DIA_NUM": req.day_number,
        "MES": req.month,
        "ANIO_LETRAS": req.year_words,
        "ANIO_NUM": req.year_number,
        "SIGNATURES": _SIG_SENTINEL,
        "FIRMANTES_LISTA": req.signers_list,
    }
    tpl.render(context)
    intermediate = BytesIO()
    tpl.save(intermediate)
    intermediate.seek(0)

    # Pass 2 — python-docx finds the sentinel and injects the signature table.
    doc = Document(intermediate)
    parties = [
        (f"{o.first_name} {o.last_name}", req.owner_denomination) for o in req.owners
    ] + [
        (f"{t.first_name} {t.last_name}", req.tenant_denomination) for t in req.tenants
    ]
    inject_signatures(doc, parties)

    buf = BytesIO()
    doc.save(buf)
    buf.seek(0)

    headers = {"Content-Disposition": "attachment; filename=rent_contract.docx"}
    return StreamingResponse(
        buf,
        media_type="application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        headers=headers,
    )


# Static files last so API routes take precedence
if STATIC_DIR.exists():
    app.mount("/ui", StaticFiles(directory=str(STATIC_DIR), html=True), name="static")
