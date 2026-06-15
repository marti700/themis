from io import BytesIO
from pathlib import Path

from docxtpl import DocxTemplate
from fastapi import FastAPI, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

app = FastAPI(title="Themis Document API")

TEMPLATES_DIR = Path(__file__).parent / "docxtemplates"


class CustomerInfo(BaseModel):
    first_name: str
    last_name: str
    id_number: str
    address: str | None = None
    marital_status: str | None = None
    occupation: str | None = None
    nationality: str | None = None  # not yet in customer DB model


class SellContractRequest(BaseModel):
    seller: CustomerInfo
    buyer: CustomerInfo
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
    signatures: str = ""
    signers_list: str = ""
    firm_name: str
    firm_service: str
    notary_title_name: str
    notary_phone: str
    notary_municipality: str
    notary_registration: str


@app.post("/documents/sell_contract")
def generate_sell_contract(req: SellContractRequest):
    template_path = TEMPLATES_DIR / "sell_contract.docx"
    if not template_path.exists():
        raise HTTPException(status_code=404, detail="Template not found")

    doc = DocxTemplate(template_path)
    context = {
        "BUFETE_NOMBRE": req.firm_name,
        "BUFETE_SERVICIO": req.firm_service,
        "NOTARIO_TITULO_NOMBRE": req.notary_title_name,
        "NOTARIO_TITULO_NOMBRE_MAYUS": req.notary_title_name.upper(),
        "NOTARIO_TELEFONO": req.notary_phone,
        "NOTARIO_MUNICIPIO": req.notary_municipality,
        "NOTARIO_MATRICULA": req.notary_registration,
        "VENDEDOR_NOMBRE": f"{req.seller.first_name} {req.seller.last_name}",
        "VENDEDOR_NACIONALIDAD": req.seller.nationality or "",
        "VENDEDOR_ESTADO_CIVIL": req.seller.marital_status or "",
        "VENDEDOR_OCUPACION": req.seller.occupation or "",
        "VENDEDOR_CEDULA": req.seller.id_number,
        "VENDEDOR_DOMICILIO": req.seller.address or "",
        "COMPRADOR_NOMBRE": f"{req.buyer.first_name} {req.buyer.last_name}",
        "COMPRADOR_NACIONALIDAD": req.buyer.nationality or "",
        "COMPRADOR_ESTADO_CIVIL": req.buyer.marital_status or "",
        "COMPRADOR_OCUPACION": req.buyer.occupation or "",
        "COMPRADOR_CEDULA": req.buyer.id_number,
        "COMPRADOR_DOMICILIO": req.buyer.address or "",
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
        "SIGNATURES": req.signatures,
        "FIRMANTES_LISTA": req.signers_list,
    }

    doc.render(context)

    buf = BytesIO()
    doc.save(buf)
    buf.seek(0)

    headers = {"Content-Disposition": "attachment; filename=sell_contract.docx"}
    return StreamingResponse(
        buf,
        media_type="application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        headers=headers,
    )
