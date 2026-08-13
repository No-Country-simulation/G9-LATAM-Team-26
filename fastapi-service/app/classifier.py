"""
Motor de inferencia del sistema.

Carga los modelos entrenados para:
- Clasificar transacciones por descripción.
- Predecir el perfil financiero del usuario.

La función pública `analizar()` mantiene la misma interfaz utilizada por el resto
de la aplicación.

Modelos (en la carpeta models/):
- modelo_clasificador_transacciones.joblib + vectorizer_transacciones.joblib
    clasifican cada transacción por su descripción (TF-IDF + LogisticRegression)
- modelo_perfil_financiero.joblib
    predice el perfil (RandomForest) a partir de un vector de 37 features
"""
from pathlib import Path
import os
import urllib.request

import joblib # type: ignore
import pandas as pd
from dotenv import load_dotenv

from app.schemas import AnalisisFinancieroRequest, TransaccionClasificada
from app.schemas import Transaccion
from typing import List, Dict, Any

import logging

logger = logging.getLogger(__name__)

# Carga las variables definidas en el archivo .env (si existe) al entorno del proceso
load_dotenv()

# ---------------------------------------------------------------------------
# Descarga de modelos desde OCI Object Storage (URLs Preautenticadas)
# Las URLs viven en variables de entorno, NUNCA hardcodeadas en el código.
# ---------------------------------------------------------------------------
_MODELS_DIR = Path(__file__).resolve().parent.parent / "models"
_MODELS_DIR.mkdir(parents=True, exist_ok=True)

OCI_URLS = {
    "modelo_perfil_financiero.joblib": os.environ.get("OCI_URL_MODELO_PERFIL"),
    "modelo_clasificador_transacciones.joblib": os.environ.get("OCI_URL_MODELO_CLASIFICADOR"),
    "vectorizer_transacciones.joblib": os.environ.get("OCI_URL_VECTORIZER"),
}

_faltantes = [nombre for nombre, url in OCI_URLS.items() if not url]
if _faltantes:
    raise RuntimeError(
        f"Faltan variables de entorno para descargar: {_faltantes}. "
        "Revisa tu archivo .env (copia .env.example y llénalo con tus URLs de OCI)."
    )


def descargar_modelos_desde_oci() -> None:
    """Descarga automáticamente los modelos desde OCI Object Storage si no están presentes localmente."""
    logger.info("Verificando modelos en el entorno local / OCI...")
    for nombre_archivo, url_oci in OCI_URLS.items():
        ruta_destino = _MODELS_DIR / nombre_archivo
        if not ruta_destino.exists():
            logger.info("Descargando %s desde Oracle Cloud Storage...", nombre_archivo)
            try:
                urllib.request.urlretrieve(url_oci, ruta_destino)
                logger.info("¡%s descargado con éxito!", nombre_archivo)
            except Exception as e:
                logger.error("Error al descargar %s desde OCI: %s", nombre_archivo, e)


# Ejecutar la sincronización con OCI antes de cargar los modelos
descargar_modelos_desde_oci()

# ---------------------------------------------------------------------------
# Carga de modelos
# ---------------------------------------------------------------------------
modelo_perfil = joblib.load(_MODELS_DIR / "modelo_perfil_financiero.joblib")
modelo_transacciones= joblib.load(_MODELS_DIR / "modelo_clasificador_transacciones.joblib")
vectorizer_transacciones= joblib.load(_MODELS_DIR / "vectorizer_transacciones.joblib")

logger.info("Modelos cargados: perfil=%s, clasificador=%s", type(modelo_perfil).__name__, type(modelo_transacciones).__name__)

# Columnas esperadas por el modelo de perfil.
COLUMNAS_MODELO_PERFIL = list(modelo_perfil.feature_names_in_)

# ---------------------------------------------------------------------------
# Constantes y helpers (copiados fielmente del notebook de Data Science)
# ---------------------------------------------------------------------------
GASTOS_HORMIGA_KEYWORDS = {
    "starbucks", "oxxo", "7-eleven", "café punta del cielo", "coca-cola",
    "sabritas", "pan dulce", "chocolate", "galletas", "helado",
}

ORDEN_AHORRO = {"Nula": 0, "Baja": 1, "Media": 2, "Alta": 3}


# ---------------------------------------------------------------------------
# Paso 1: clasificar transacciones por descripción
# ---------------------------------------------------------------------------
def _procesar_transacciones(transacciones: List[Transaccion]) -> pd.DataFrame:
    """
    Clasifica las transacciones y marca los posibles gastos hormiga.
    """
    transacciones_df = pd.DataFrame( [{"descripcion": t.descripcion, "valor": t.valor} for t in transacciones])

    transacciones_df["categoria"] = modelo_transacciones.predict(
        vectorizer_transacciones.transform(transacciones_df["descripcion"])
    )
    transacciones_df["es_gasto_hormiga"] = (
        transacciones_df["descripcion"]
        .str.lower()
        .apply(lambda desc: any(k in desc for k in GASTOS_HORMIGA_KEYWORDS))
    )

    logger.info("Transacciones clasificadas: %s", transacciones_df[["descripcion", "categoria"]].to_dict("records"))
    return transacciones_df


# ---------------------------------------------------------------------------
# Paso 2: Construcción del vector de entrada para el modelo de perfil
# ---------------------------------------------------------------------------
def _construir_features_usuario(datos: AnalisisFinancieroRequest, trans_df: pd.DataFrame) -> pd.DataFrame:
    monto_total_gastado = trans_df["valor"].sum()
    porcentaje_gasto_por_categoria = (trans_df.groupby("categoria")["valor"].sum() / monto_total_gastado)

    metricas_usuario: Dict[str, Any] = {
        "ingreso_mensual": datos.ingreso_mensual,
        "nivel_endeudamiento": datos.nivel_endeudamiento,
        "ticket_promedio": trans_df["valor"].mean(),
        "std_monto": trans_df["valor"].std() if len(trans_df) > 1 else 0,
        "frecuencia_transacciones": len(trans_df),
        "num_categorias_distintas": trans_df["categoria"].nunique(),
        "pct_gasto_hormiga": trans_df["es_gasto_hormiga"].mean(),
        "frecuencia_ahorro_ord": ORDEN_AHORRO.get(datos.frecuencia_ahorro, 0),
    }

    categoria_riesgo = (trans_df["categoria"] == "Deudas") | trans_df["es_gasto_hormiga"]
    metricas_usuario["pct_gasto_riesgo"] = (trans_df.loc[categoria_riesgo, "valor"].sum() / monto_total_gastado)

    categoria_top = trans_df.loc[trans_df["valor"].idxmax(), "categoria"]

    # Inicializa todas las variables en cero.
    vector_features = {nombre_columna: 0 for nombre_columna in COLUMNAS_MODELO_PERFIL}

    for nombre_feature, valor_feature in metricas_usuario.items():
        if nombre_feature in vector_features:
            vector_features[nombre_feature] = valor_feature

    # Las columnas del modelo conservan los acentos (pct_gasto_alimentación),
    # por eso se usa .lower() pero no se quitan los acentos: deben coincidir
    # exactamente con los nombres definidos durante el entrenamiento.
    for categoria, porcentaje_gasto in porcentaje_gasto_por_categoria.items():
        col = f"pct_gasto_{categoria.lower()}"
        if col in vector_features:
            vector_features[col] = porcentaje_gasto

    # Marca la categoría con mayor gasto.
    columna_categoria_principal = f"top_{categoria_top}"
    if columna_categoria_principal in vector_features:
        vector_features[columna_categoria_principal] = 1

    # Reordena las columnas según el orden esperado por el modelo.
    return pd.DataFrame([vector_features])[COLUMNAS_MODELO_PERFIL]


# ---------------------------------------------------------------------------
# Punto de entrada del módulo.
# ---------------------------------------------------------------------------
def analizar(datos: AnalisisFinancieroRequest) -> Dict[str, Any]:
    transacciones_clasificadas_df = _procesar_transacciones(datos.transacciones)

    vector_usuario = _construir_features_usuario(datos, transacciones_clasificadas_df)
    perfil_predicho = modelo_perfil.predict(vector_usuario)[0]
    confianza_prediccion = float(max(modelo_perfil.predict_proba(vector_usuario)[0]))

    logger.info(
        "Análisis completo -> perfil=%s, confianza=%.2f, transacciones=%d",
        perfil_predicho, confianza_prediccion, len(transacciones_clasificadas_df)
    )

    transacciones_clasificadas = [
        TransaccionClasificada(
            descripcion=transaccion["descripcion"],
            valor=transaccion["valor"],
            categoria=transaccion["categoria"],
        )
        for _, transaccion in transacciones_clasificadas_df.iterrows()
    ]

    return {
        "perfil_financiero": perfil_predicho,
        "probabilidad": round(confianza_prediccion, 2),
        "transacciones_clasificadas": transacciones_clasificadas,
    }