import logging

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")

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
    devuelve el perfil financiero (RandomForest) y las transacciones
    clasificadas (LogisticRegression + TF-IDF)..
    """
    resultado = analizar(datos)
    return AnalisisFinancieroResponse(**resultado)