import logging

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")

from fastapi import FastAPI
from typing import List
from app.schemas import (
    AnalisisFinancieroRequest,
    AnalisisFinancieroResponse,
    ClasificarTransaccionesRequest,
    TransaccionClasificada,
)
from app.classifier import analizar, clasificar_transacciones

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


@app.post("/clasificar-transaccion", response_model=List[TransaccionClasificada])
def clasificar_transaccion(datos: ClasificarTransaccionesRequest):
    """
    Clasifica una o varias transacciones por su descripción (Alimentación,
    Transporte, Salud, etc.), sin calcular el perfil financiero completo.
    """
    return clasificar_transacciones(datos.transacciones)