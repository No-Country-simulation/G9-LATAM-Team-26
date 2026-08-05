from pydantic import BaseModel
from typing import List

# -------------- Entrada ---------------


class Transaccion(BaseModel):
    descripcion: str
    valor: float


class AnalisisFinancieroRequest(BaseModel):
    ingreso_mensual: float
    nivel_endeudamiento: float
    frecuencia_ahorro: str
    transacciones: List[Transaccion]


# -------------- Salida ---------------

class TransaccionClasificada(BaseModel):
    descripcion: str
    valor: float
    categoria: str

class AnalisisFinancieroResponse(BaseModel):
    perfil_financiero: str
    probabilidad: float
    transacciones_clasificadas: List[TransaccionClasificada]
