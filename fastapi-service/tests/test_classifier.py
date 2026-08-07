"""
Tests del motor de inferencia (classifier.py).
Usa los mismos 3 casos que se prueban manualmente en Insomnia,
para no duplicar el diseño de casos de prueba.
"""
from app.classifier import analizar
from app.schemas import AnalisisFinancieroRequest, Transaccion


def test_perfil_saludable():
    datos = AnalisisFinancieroRequest(
        ingreso_mensual=20000,
        nivel_endeudamiento=10,
        frecuencia_ahorro="Alta",
        transacciones=[
            Transaccion(descripcion="Compra en Walmart", valor=600),
            Transaccion(descripcion="CFE", valor=300),
            Transaccion(descripcion="Gasolina Shell", valor=400),
        ],
    )
    resultado = analizar(datos)
    assert resultado["perfil_financiero"] == "Saludable"


def test_perfil_en_observacion():
    datos = AnalisisFinancieroRequest(
        ingreso_mensual=15000,
        nivel_endeudamiento=45,
        frecuencia_ahorro="Baja",
        transacciones=[
            Transaccion(descripcion="Compra en Walmart", valor=850),
            Transaccion(descripcion="Gasolina Pemex", valor=500),
            Transaccion(descripcion="Netflix mensual", valor=199),
            Transaccion(descripcion="Pago TDC BBVA", valor=3000),
        ],
    )
    resultado = analizar(datos)
    assert resultado["perfil_financiero"] == "En observación"


def test_perfil_en_riesgo():
    datos = AnalisisFinancieroRequest(
        ingreso_mensual=8000,
        nivel_endeudamiento=80,
        frecuencia_ahorro="Nula",
        transacciones=[
            Transaccion(descripcion="Pago TDC Santander", valor=3000),
            Transaccion(descripcion="Pago Préstamo Personal", valor=2500),
            Transaccion(descripcion="Compra en Walmart", valor=900),
        ],
    )
    resultado = analizar(datos)
    assert resultado["perfil_financiero"] == "En riesgo"


def test_clasificacion_de_transacciones():
    """Verifica que el modelo asigne las categorías esperadas por comercio conocido."""
    datos = AnalisisFinancieroRequest(
        ingreso_mensual=10000,
        nivel_endeudamiento=20,
        frecuencia_ahorro="Media",
        transacciones=[
            Transaccion(descripcion="Compra en Walmart", valor=500),
            Transaccion(descripcion="Netflix mensual", valor=199),
            Transaccion(descripcion="Pago TDC BBVA", valor=1000),
        ],
    )
    resultado = analizar(datos)
    categorias = {t.categoria for t in resultado["transacciones_clasificadas"]}
    assert "Alimentación" in categorias
    assert "Suscripciones" in categorias
    assert "Deudas" in categorias