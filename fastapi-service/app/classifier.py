from app.schemas import AnalisisFinancieroRequest, TransaccionClasificada

# ---------------------------------------------------------------
# ESTO ES LO "TONTO". El día que el equipo de datos entregue el
# modelo.pkl, se reemplaza el CONTENIDO de estas dos funciones.
# Las firmas quedan igual, así que main.py no se entera de nada.
# ---------------------------------------------------------------

CATALOGO = {
    "Alimentación": [
        "Walmart", "Soriana", "Chedraui", "Bodega Aurrera", "Costco", "Sam's Club",
        "La Comer", "City Market", "Subway", "Domino's Pizza", "Little Caesars",
        "McDonald's", "Burger King", "Toks", "Vips", "El Globo", "Starbucks",
        "Italianni's", "Sushi Roll", "Chili's", "Mercado", "Despensa"
    ],
    "Transporte": [
        "Uber", "DiDi", "Cabify", "Gasolina Pemex", "Gasolina Shell", "Gasolina BP",
        "Caseta CAPUFE", "ADO", "ETN", "Viva Aerobus", "Aeroméxico",
        "Metro CDMX", "Metrobús"
    ],
    "Salud": [
        "Farmacias Guadalajara", "Farmacias del Ahorro", "Farmacia San Pablo",
        "Hospital Ángeles", "Hospital ABC", "Laboratorio Chopo", "Salud Digna",
        "Dentista", "Ópticas Devlyn", "Ginecólogo"
    ],
    "Vivienda": [
        "Renta", "Hipoteca", "Home Depot", "IKEA", "Mantenimiento",
        "Ferretería", "Pinturas Comex", "Construrama"
    ],
    "Educación": [
        "Coursera", "Platzi", "UNAM", "IPN", "Tec de Monterrey",
        "Compra Libros", "Amazon Libros", "Útiles", "Colegiatura"
    ],
    "Servicios": [
        "CFE", "Telmex", "Totalplay", "Izzi", "Megacable", "Gas Natural",
        "Servicio de Agua", "Telcel", "AT&T"
    ],
    "Entretenimiento": [
        "Cinépolis", "Cinemex", "Steam", "PlayStation Store", "Xbox Store",
        "Nintendo eShop", "Concierto", "Six Flags", "Museo"
    ],
    "Suscripciones": [
        "Netflix", "Spotify", "Disney+", "Amazon Prime", "Max",
        "YouTube Premium", "Google One", "Dropbox", "Apple Music", "Microsoft 365"
    ],
    "Inversión": [
        "CETES", "GBM", "Nu Ahorro", "AFORE", "Fondo Indexado",
        "ETF Vanguard", "Compra Acciones"
    ],
    "Deudas": [
        "Pago TDC BBVA", "Pago TDC Banamex", "Pago TDC Santander",
        "Pago Préstamo Personal", "Liverpool Crédito", "Pago Nómina Kueski"
    ],
    "Seguros": [
        "GNP Seguros", "AXA Seguros", "Seguros Monterrey", "Seguro Auto Qualitas"
    ],
    "Ropa": [
        "Liverpool", "Zara", "H&M", "C&A", "Palacio de Hierro", "Shein"
    ],
    "Mascotas": [
        "Petco", "Veterinario", "PatasPet"
    ],
    "Otros": [
        "Transferencia SPEI", "Compra Desconocida", "Cargo Varios", "OXXO", "7-Eleven"
    ],
}

# Aplanamos el catálogo a un mapa comercio -> categoría, todo en minúsculas
# para comparar. Se calcula una sola vez al importar el módulo.
_MAPA_COMERCIOS = {
    comercio.lower(): categoria
    for categoria, comercios in CATALOGO.items()
    for comercio in comercios
}

CATEGORIAS = list(CATALOGO.keys())


def clasificar_transaccion(descripcion: str) -> str:
    """
    Clasifica UNA transacción buscando coincidencia de comercio
    dentro de la descripción. Mañana esto se reemplaza por:
    modelo.predict([descripcion])
    """
    texto = descripcion.lower()

    for comercio, categoria in _MAPA_COMERCIOS.items():
        if comercio in texto:
            return categoria

    return "Otros"  # fallback oficial del catálogo


def predecir_perfil(datos: AnalisisFinancieroRequest) -> tuple[str, float]:
    """
    Replica la regla de negocio real (calcular_perfil_financiero) del
    notebook de Data Science, para que el clasificador dummy razone
    igual que el modelo que va a llegar.
    Mañana esto se reemplaza por: modelo.predict_proba(features)
    """
    gasto_total = sum(t.valor for t in datos.transacciones)
    ratio_gasto = gasto_total / datos.ingreso_mensual if datos.ingreso_mensual else 0

    puntos_riesgo = 0

    if datos.nivel_endeudamiento > 35:
        puntos_riesgo += 2
    elif datos.nivel_endeudamiento > 18:
        puntos_riesgo += 1

    if ratio_gasto > 0.65:
        puntos_riesgo += 2
    elif ratio_gasto > 0.45:
        puntos_riesgo += 1

    ahorro = datos.frecuencia_ahorro
    if ahorro == "Nula":
        puntos_riesgo += 2
    elif ahorro == "Baja":
        puntos_riesgo += 1
    elif ahorro == "Alta":
        puntos_riesgo -= 1

    if puntos_riesgo >= 4:
        return "En riesgo", 0.85
    elif puntos_riesgo >= 2:
        return "En observación", 0.75
    else:
        return "Saludable", 0.90


def analizar(datos: AnalisisFinancieroRequest) -> dict:
    """
    Función pública que usa main.py. Esta firma NO cambia nunca.
    """
    transacciones_clasificadas = [
        TransaccionClasificada(
            descripcion=t.descripcion,
            valor=t.valor,
            categoria=clasificar_transaccion(t.descripcion),
        )
        for t in datos.transacciones
    ]

    perfil, probabilidad = predecir_perfil(datos)

    return {
        "perfil_financiero": perfil,
        "probabilidad": probabilidad,
        "transacciones_clasificadas": transacciones_clasificadas,
    }