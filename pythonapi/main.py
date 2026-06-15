from io import BytesIO
from pathlib import Path

from docx import Document
from docx.enum.text import WD_PARAGRAPH_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docxtpl import DocxTemplate
from fastapi import FastAPI, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

app = FastAPI(title="Themis Document API")

TEMPLATES_DIR = Path(__file__).parent / "docxtemplates"
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
