from fastapi import FastAPI
from app.schemas import AnalisisFinancieroRequest, AnalisisFinancieroResponse
from app.classifier import analizar

app = FastAPI(
    title="Finance AI - Microservicio de Clasificación",
    description="Servicio de análisis financiero para el hackatón G9 LATAM Team 26",
    version="1.0.0",
)


@app.get("/")
def health_check():
    """Endpoint simple para verificar que el servicio está vivo."""
    return {"status": "ok", "servicio": "finance-ai-ml-service"}


@app.post("/analisis-financiero", response_model=AnalisisFinancieroResponse)
def analisis_financiero(datos: AnalisisFinancieroRequest):
    """
    Recibe los datos financieros del usuario y sus transacciones,
    devuelve el perfil financiero y las transacciones clasificadas.

    Hoy: lógica por reglas (dummy classifier).
    Mañana: modelo.pkl real cargado en classifier.py.
    La forma de este endpoint no cambia en ninguno de los dos casos.
    """
    resultado = analizar(datos)
    return AnalisisFinancieroResponse(**resultado)