"""
Test de integración del endpoint HTTP, usando TestClient de FastAPI
(no requiere levantar uvicorn por separado).
"""
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health_check():
    response = client.get("/")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


def test_analisis_financiero_responde_200_con_shape_correcto():
    payload = {
        "ingreso_mensual": 15000,
        "nivel_endeudamiento": 45,
        "frecuencia_ahorro": "Baja",
        "transacciones": [
            {"descripcion": "Compra en Walmart", "valor": 850},
        ],
    }
    response = client.post("/analisis-financiero", json=payload)
    assert response.status_code == 200

    data = response.json()
    assert "perfil_financiero" in data
    assert "probabilidad" in data
    assert "transacciones_clasificadas" in data
    assert isinstance(data["transacciones_clasificadas"], list)