# 📊 Finance AI — Asistente Inteligente de Salud Financiera FlowFi
### Módulo de Ciencia de Datos — Hackathon Oracle & Alura

Este repositorio contiene el trabajo del equipo de **Data Science** para el asistente de salud financiera: generación del dataset sintético, análisis exploratorio, ingeniería de variables, entrenamiento de modelos, clasificación de transacciones y motor de recomendaciones.

---

## 📁 Contenido del notebook

| # | Sección | Descripción |
|---|---|---|
| 1 | Generación del Dataset | Simulación de usuarios y transacciones financieras a partir de un catálogo de comercios |
| 2 | EDA (Exploración y Limpieza) | Validaciones de calidad, distribuciones, outliers y correlaciones |
| 3 | Feature Engineering | Agregación de métricas por usuario y construcción de `X`/`y` |
| 4 | Entrenamiento de Modelos | Clasificador del perfil financiero (Regresión Logística vs. Random Forest) |
| 5 | Clasificador de Transacciones | Modelo TF-IDF para categorizar transacciones a partir de su descripción |
| 6 | Motor de Recomendaciones | Integra ambos modelos y genera la respuesta final del endpoint |

---

## 1. Generación del Dataset

Se construyó un catálogo de +150 comercios mexicanos organizados en 14 categorías (Alimentación, Transporte, Salud, Vivienda, Educación, Servicios, Entretenimiento, Suscripciones, Inversión, Deudas, Seguros, Ropa, Mascotas, Otros), combinando marcas reconocibles (Walmart, Uber, Netflix...) y términos genéricos (Combustible, Streaming, Renta...) para que el modelo generalice a ambos estilos de descripción.

Cada transacción simulada incluye **ruido realista**: prefijos bancarios ("PAGO ", "REF. "), sucursal o ciudad, número de referencia, variación de mayúsculas/minúsculas y typos leves — para que el dataset se parezca a un estado de cuenta real y no a una lista limpia de nombres de comercio.

El `perfil_financiero` (Saludable / En observación / En riesgo) se etiqueta con una regla de negocio que combina nivel de endeudamiento, ratio gasto/ingreso y frecuencia de ahorro.

**Dataset final:** 4,000 usuarios · dataset de transacciones. Distribución de perfiles:

| Perfil | % |
|---|---|
| En observación | 43.6% |
| Saludable | 32.5% |
| En riesgo | 23.9% |

## 2. EDA (Exploración y Limpieza)

- Sin valores nulos ni duplicados en usuarios ni transacciones
- Validaciones de rango (ingresos > 0, endeudamiento entre 0-100%, montos > 0)
- Vivienda es la categoría de mayor gasto total; Alimentación la de mayor frecuencia de transacciones
- La frecuencia de ahorro separa claramente los perfiles: usuarios con ahorro "Alta" caen mayoritariamente en "Saludable"; los de ahorro "Nula" concentran el perfil "En riesgo"
- Matriz de correlación sin anomalías: el ratio gasto/ingreso correlaciona negativamente con el ingreso mensual, como se esperaría

## 3. Feature Engineering

A nivel usuario se agregan: % de gasto por categoría (normalizado), ticket promedio, desviación estándar del gasto, frecuencia de transacciones, número de categorías distintas, % de gasto hormiga, % de gasto en categorías de riesgo, categoría de mayor gasto (one-hot), y frecuencia de ahorro codificada como variable **ordinal** (Nula=0 … Alta=3).

> **Nota sobre data leakage:** `gasto_total` y el ratio gasto/ingreso se excluyen de las features porque se usaron directamente en la regla que generó la etiqueta. Sí se incluyen `ingreso_mensual`, `nivel_endeudamiento` y la frecuencia de ahorro codificada, porque son variables de entrada legítimas que un producto real tendría disponibles antes de calcular el perfil.

División train/test: 80/20, **estratificada** por `perfil_financiero` para preservar la proporción de clases en ambos conjuntos.

## 4. Entrenamiento de Modelos — Perfil Financiero

| Modelo | Accuracy | F1-Score (Macro) |
|---|---|---|
| Regresión Logística (Baseline) | 0.816 | 0.821 |
| **Random Forest (Principal)** | **0.915** | **0.917** |

**Validación adicional:**
- Validación cruzada (5 folds): F1 macro promedio 0.918 (±0.01) — el modelo es estable, no depende de un split particular
- Sobreajuste leve controlado: F1 en train 0.998 vs. test 0.931 (con `max_depth=10` limitando la complejidad del árbol)
- Variable más influyente: `frecuencia_ahorro_ord`, seguida de `nivel_endeudamiento` — coherente con la regla de negocio que generó las etiquetas

**Artefacto exportado:** `modelo_perfil_financiero.joblib`

## 5. Clasificador de Transacciones (TF-IDF)

Vectorización con TF-IDF sobre n-gramas de caracteres (`analyzer='char_wb'`, 2-4), para reconocer comercios con sucursal, mayúsculas variables o pequeños errores de tipeo sin haberlos visto exactamente en entrenamiento.

| Modelo | F1-Score (Macro) |
|---|---|
| Naive Bayes | 0.998 |
| **Logistic Regression** | **0.999** |

> El desempeño casi perfecto no indica fuga de datos: refleja que el vocabulario financiero (nombres de comercio) es muy estructurado y poco ambiguo entre categorías, y que el vectorizador de n-gramas de caracteres es robusto al ruido introducido. En producción con datos reales de usuarios el desempeño bajaría, pero la arquitectura del pipeline seguiría siendo válida.

**Artefactos exportados:** `modelo_clasificador_transacciones.joblib`, `vectorizer_transacciones.joblib`

## 6. Motor de Recomendaciones

Integra ambos modelos en una sola función, `analizar_finanzas()`, equivalente al endpoint `POST /analisis-financiero` del brief. Es un sistema de **reglas de negocio** (no un modelo de ML adicional) que combina el perfil predicho, el % de gasto hormiga, el nivel de endeudamiento, la frecuencia de ahorro y la concentración de gasto por categoría para generar recomendaciones específicas.

**Ejemplo de entrada:**
```json
{
  "ingreso_mensual": 4500,
  "nivel_endeudamiento": 25,
  "frecuencia_ahorro": "Media",
  "transacciones": [
    {"descripcion": "Supermercado", "valor": 420},
    {"descripcion": "Combustible", "valor": 300},
    {"descripcion": "Streaming", "valor": 40}
  ]
}
```

**Salida:**
```json
{
  "perfil_financiero": "Saludable",
  "probabilidad": 0.58,
  "resumen_gastos": {
    "alimentacion": 420.0,
    "transporte": 300.0,
    "suscripciones": 40.0
  },
  "recomendaciones": [
    "Monitorear los gastos recurrentes de alimentación, concentran 55% de tu gasto total."
  ]
}
```

---

## 🔗 Entrega a Backend

Backend solo necesita estos 4 archivos para integrar el modelo (no requiere ningún CSV de entrenamiento):

| Archivo | Uso |
|---|---|
| `modelo_perfil_financiero.joblib` | Predicción del perfil financiero |
| `modelo_clasificador_transacciones.joblib` | Clasificación de transacciones por categoría |
| `vectorizer_transacciones.joblib` | Transforma texto nuevo al formato que espera el clasificador |
| `motor_recomendaciones.py` | Código que integra ambos modelos y genera el JSON final |

> Como los modelos están serializados con `scikit-learn` (Python) y Backend está en Java/Spring Boot, la integración se realiza mediante un microservicio Python (FastAPI) que expone el mismo contrato de `POST /analisis-financiero`, desplegable como OCI Function.

## ▶️ Cómo reproducir el pipeline completo

Ejecutar en este orden (cada script lee los archivos que genera el anterior):

1. `generar_dataset_financiero.py` → `dataset_usuarios.csv`, `dataset_transacciones.csv`
2. `eda_financiero.py` → gráficas de exploración (PNG)
3. `feature_engineering.py` → `X_train.csv`, `X_test.csv`, `y_train.csv`, `y_test.csv`
4. `entrenamiento_modelos_final.py` → `modelo_perfil_financiero.joblib`
5. `clasificador_transacciones.py` → `modelo_clasificador_transacciones.joblib`, `vectorizer_transacciones.joblib`
6. `motor_recomendaciones.py` → función `analizar_finanzas()` lista para consumir

**Requisitos:** `pandas`, `numpy`, `scikit-learn`, `matplotlib`, `seaborn`, `joblib`
