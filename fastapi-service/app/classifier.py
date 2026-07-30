"""
classifier.py — Motor de inferencia real (reemplaza al clasificador dummy).

IMPORTANTE PARA EL EQUIPO:
- La firma pública `analizar(datos) -> dict` NO cambió respecto al dummy.
  main.py y schemas.py siguen intactos, y Java no se entera del cambio.
- Todo lo que cambió vive acá adentro: ahora carga 3 modelos entrenados
  por el equipo de Data Science en vez de usar reglas.

Modelos (en la carpeta models/):
- modelo_clasificador_transacciones.joblib + vectorizer_transacciones.joblib
    -> clasifican cada transacción por su descripción (TF-IDF + LogisticRegression)
- modelo_perfil_financiero.joblib
    -> predice el perfil (RandomForest) a partir de un vector de 37 features
"""

import unicodedata
from pathlib import Path

import joblib
import pandas as pd

from app.schemas import AnalisisFinancieroRequest, TransaccionClasificada

# ---------------------------------------------------------------------------
# Carga de modelos (una sola vez, al importar el módulo)
# ---------------------------------------------------------------------------
_MODELS_DIR = Path(__file__).resolve().parent.parent / "models"

modelo_perfil = joblib.load(_MODELS_DIR / "modelo_perfil_financiero.joblib")
modelo_transacciones = joblib.load(
    _MODELS_DIR / "modelo_clasificador_transacciones.joblib"
)
vectorizer_transacciones = joblib.load(_MODELS_DIR / "vectorizer_transacciones.joblib")

# El propio modelo recuerda el orden EXACTO de sus 37 features. Lo usamos como
# fuente de verdad para reindexar: si falta o sobra una columna, sklearn tira
# error explícito en vez de predecir en silencio con datos mal alineados.
COLUMNAS_MODELO_PERFIL = list(modelo_perfil.feature_names_in_)

# ---------------------------------------------------------------------------
# Constantes y helpers (copiados fielmente del notebook de Data Science)
# ---------------------------------------------------------------------------
GASTOS_HORMIGA_KEYWORDS = {
    "starbucks", "oxxo", "7-eleven", "café punta del cielo", "coca-cola",
    "sabritas", "pan dulce", "chocolate", "galletas", "helado",
}

ORDEN_AHORRO = {"Nula": 0, "Baja": 1, "Media": 2, "Alta": 3}


def _quitar_acentos(texto: str) -> str:
    return "".join(
        c for c in unicodedata.normalize("NFKD", texto) if not unicodedata.combining(c)
    )


def _slug(texto: str) -> str:
    """'Alimentación' -> 'alimentacion'. SOLO para llaves del JSON de salida."""
    return _quitar_acentos(texto).lower().replace(" ", "_")


# ---------------------------------------------------------------------------
# Paso 1: clasificar transacciones por descripción
# ---------------------------------------------------------------------------
def _procesar_transacciones(transacciones) -> pd.DataFrame:
    """
    Recibe la lista de transacciones del request y devuelve un DataFrame
    con la categoría predicha y la marca de gasto hormiga.
    """
    df = pd.DataFrame(
        [{"descripcion": t.descripcion, "valor": t.valor} for t in transacciones]
    )

    df["categoria"] = modelo_transacciones.predict(
        vectorizer_transacciones.transform(df["descripcion"])
    )
    df["es_gasto_hormiga"] = (
        df["descripcion"]
        .str.lower()
        .apply(lambda desc: any(k in desc for k in GASTOS_HORMIGA_KEYWORDS))
    )
    return df


# ---------------------------------------------------------------------------
# Paso 2: construir el vector de 37 features que espera el RandomForest
# ---------------------------------------------------------------------------
def _construir_features_usuario(
    datos: AnalisisFinancieroRequest, df_tx: pd.DataFrame) -> pd.DataFrame:
    gasto_total = df_tx["valor"].sum()
    pct_por_categoria = df_tx.groupby("categoria")["valor"].sum() / gasto_total

    features = {
        "ingreso_mensual": datos.ingreso_mensual,
        "nivel_endeudamiento": datos.nivel_endeudamiento,
        "ticket_promedio": df_tx["valor"].mean(),
        "std_monto": df_tx["valor"].std() if len(df_tx) > 1 else 0,
        "frecuencia_transacciones": len(df_tx),
        "num_categorias_distintas": df_tx["categoria"].nunique(),
        "pct_gasto_hormiga": df_tx["es_gasto_hormiga"].mean(),
        "frecuencia_ahorro_ord": ORDEN_AHORRO.get(datos.frecuencia_ahorro, 0),
    }

    categoria_riesgo = (df_tx["categoria"] == "Deudas") | df_tx["es_gasto_hormiga"]
    features["pct_gasto_riesgo"] = (
        df_tx.loc[categoria_riesgo, "valor"].sum() / gasto_total
    )

    categoria_top = df_tx.loc[df_tx["valor"].idxmax(), "categoria"]

    # Arrancamos con todas las columnas en 0 y rellenamos las que aplican.
    fila = {col: 0 for col in COLUMNAS_MODELO_PERFIL}

    for clave, valor in features.items():
        if clave in fila:
            fila[clave] = valor

    # OJO: las columnas pct_gasto_<categoria> del modelo conservan el acento
    # (pct_gasto_alimentación). Por eso acá va .lower() pero NO _slug():
    # _slug quitaría el acento y la columna no matchearía -> feature en 0.
    for categoria, pct in pct_por_categoria.items():
        col = f"pct_gasto_{categoria.lower()}"
        if col in fila:
            fila[col] = pct

    # top_<Categoria>: nombre literal, capitalizado y con acento tal cual.
    col_top = f"top_{categoria_top}"
    if col_top in fila:
        fila[col_top] = 1

    # Reindexamos por el orden exacto del modelo (blindaje de orden).
    return pd.DataFrame([fila])[COLUMNAS_MODELO_PERFIL]


# ---------------------------------------------------------------------------
# Función pública — la firma NO cambia. main.py la llama igual que al dummy.
# ---------------------------------------------------------------------------
def analizar(datos: AnalisisFinancieroRequest) -> dict:
    df_tx = _procesar_transacciones(datos.transacciones)

    X_usuario = _construir_features_usuario(datos, df_tx)
    perfil = modelo_perfil.predict(X_usuario)[0]
    probabilidad = float(max(modelo_perfil.predict_proba(X_usuario)[0]))

    transacciones_clasificadas = [
        TransaccionClasificada(
            descripcion=row["descripcion"],
            valor=row["valor"],
            categoria=row["categoria"],
        )
        for _, row in df_tx.iterrows()
    ]

    return {
        "perfil_financiero": perfil,
        "probabilidad": round(probabilidad, 2),
        "transacciones_clasificadas": transacciones_clasificadas,
    }
