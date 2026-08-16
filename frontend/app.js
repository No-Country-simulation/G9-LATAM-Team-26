/* =========================================================
   FlowFi — Lógica de la aplicación (app.js)
   ========================================================= 

   Índice de este archivo (buscar estos encabezados con Ctrl+F):
     1. CONFIGURACIÓN            -> aquí se conecta con el backend real
     2. NAVEGACIÓN               -> cambio entre las 3 pantallas (tabs)
     3. SWITCH DE TEMA           -> oscuro / claro, manual
     4. SLIDER DE ENDEUDAMIENTO  -> del formulario dentro del modal
     5. TABLA DE TRANSACCIONES   -> filas dinámicas del formulario
     6. MODAL                    -> abrir/cerrar "Nuevo análisis"
     7. MODO DEMO                -> cálculo local mientras no hay backend
     8. LLAMADA A LA API         -> real o mock, según CONFIG.USE_MOCK
     9. RENDER: Diagnóstico      -> perfil, mascota, barras de gasto
    10. RENDER: Inicio           -> dashboard (score, stats, transacciones)
    11. RENDER: Recomendaciones  -> tarjetas de recomendación
    12. EDICIÓN INLINE           -> stats editables desde el dashboard
    13. BOTÓN "ANALIZAR"         -> dispara el análisis desde el modal

   ---------------------------------------------------------
   CÓMO CONECTAR CON EL BACKEND REAL (cuando esté desplegado):
   ---------------------------------------------------------
   1. Ir a la sección "1. CONFIGURACIÓN" (unas líneas abajo).
   2. Cambiar USE_MOCK a false.
   3. Poner la URL real del endpoint en API_URL.
   Nada más del código necesita tocarse — toda la app llama a
   través de la función llamarAPI(), que decide internamente si
   usa el mock local o el fetch() real según CONFIG.USE_MOCK.
   ========================================================= */

/* =========================================================
   CONFIGURACIÓN — AJUSTAR CUANDO BACKEND ESTÉ LISTO
   ========================================================= */
let analisisActualId = null;

// Detecta automáticamente si la app corre en tu máquina (localhost) o ya
// desplegada en Render — así nunca se te olvida cambiar la URL a mano y
// se rompe para el resto de las personas que abren el sitio.
const IS_LOCAL = ["localhost", "127.0.0.1"].includes(window.location.hostname);

const CONFIG = {
  USE_MOCK: false,
  // Cuando tengas la URL final del backend en Render, ponla aquí:
  PROD_API_URL: "https://flowfi-backend-java.onrender.com/analisis-financiero",
  LOCAL_API_URL: "http://localhost:8080/analisis-financiero",
  get API_URL() {
    return IS_LOCAL ? this.LOCAL_API_URL : this.PROD_API_URL;
  },
};
/* =========================================================
   NOTIFICACIONES (toast) — reemplaza a los alert() del navegador
   ========================================================= */
function mostrarToast(mensaje, tipo = "error") {
  let contenedor = document.getElementById("toast-container");
  if (!contenedor) {
    contenedor = document.createElement("div");
    contenedor.id = "toast-container";
    document.body.appendChild(contenedor);
  }
  const toast = document.createElement("div");
  toast.className = `toast toast--${tipo}`;
  toast.textContent = mensaje;
  contenedor.appendChild(toast);

  // Forzar reflow para que la animación de entrada corra
  requestAnimationFrame(() => toast.classList.add("toast--visible"));

  setTimeout(() => {
    toast.classList.remove("toast--visible");
    toast.addEventListener("transitionend", () => toast.remove(), { once: true });
  }, 3000);
}

/* =========================================================
   NAVEGACIÓN ENTRE PANTALLAS
   ========================================================= */
const tabButtons = document.querySelectorAll(".tab-btn");
const screens = document.querySelectorAll(".screen");

function goToScreen(name){
  tabButtons.forEach(b => b.classList.toggle("active", b.dataset.screen === name));
  screens.forEach(s => s.classList.toggle("active", s.id === `screen-${name}`));
}
tabButtons.forEach(btn => btn.addEventListener("click", () => goToScreen(btn.dataset.screen)));
goToScreen("inicio");

/* =========================================================
   SWITCH DE TEMA (oscuro / claro) — controlado por el usuario,
   se mantiene igual sin importar en qué pestaña esté.
   ========================================================= */
const themeToggleBtn = document.getElementById("theme-toggle");

function setTheme(dark){
  document.body.classList.toggle("theme-dark", dark);
  themeToggleBtn.textContent = dark ? "☀️" : "🌙";
  themeToggleBtn.setAttribute("aria-label", dark ? "Cambiar a tema claro" : "Cambiar a tema oscuro");
}
themeToggleBtn.addEventListener("click", () => {
  setTheme(!document.body.classList.contains("theme-dark"));
});
setTheme(true); // estado inicial: oscuro, igual que antes

/* =========================================================
   CATÁLOGO DE COMERCIOS POR CATEGORÍA
   Mismo catálogo que usa el equipo de Data Science para generar
   y clasificar transacciones — se usa aquí para armar los menús
   desplegables (categoría → comercio) del formulario, y también
   para categorizar con precisión en el modo demo (ver más abajo).
   ========================================================= */
const CATALOGO = {
  "Alimentación": [
    "Walmart", "Soriana", "Chedraui", "Bodega Aurrera", "Costco", "Sam's Club",
    "La Comer", "City Market", "Subway", "Domino's Pizza", "Little Caesars",
    "McDonald's", "Burger King", "Toks", "Vips", "El Globo", "Starbucks",
    "Italianni's", "Sushi Roll", "Chili's", "Mercado", "Despensa", "Supermercado",
    "Comida", "Restaurante",
  ],
  "Transporte": [
    "Uber", "DiDi", "Cabify", "Gasolina Pemex", "Gasolina Shell", "Gasolina BP",
    "Caseta CAPUFE", "ADO", "ETN", "Viva Aerobus", "Aeroméxico",
    "Metro CDMX", "Metrobús", "Combustible", "Gasolina", "Transporte", "Taxi",
  ],
  "Salud": [
    "Farmacias Guadalajara", "Farmacias del Ahorro", "Farmacia San Pablo",
    "Hospital Ángeles", "Hospital ABC", "Laboratorio Chopo", "Salud Digna",
    "Dentista", "Ópticas Devlyn", "Ginecólogo", "Farmacia", "Medicina", "Consulta Médica",
  ],
  "Vivienda": [
    "Renta", "Hipoteca", "Home Depot", "IKEA", "Mantenimiento",
    "Ferretería", "Pinturas Comex", "Construrama", "Alquiler",
  ],
  "Educación": [
    "Coursera", "Platzi", "UNAM", "IPN", "Tec de Monterrey",
    "Compra Libros", "Amazon Libros", "Útiles", "Colegiatura", "Escuela", "Curso",
  ],
  "Servicios": [
    "CFE", "Telmex", "Totalplay", "Izzi", "Megacable", "Gas Natural",
    "Servicio de Agua", "Telcel", "AT&T", "Luz", "Agua",
    "Internet", "Teléfono", "Recibo",
  ],
  "Entretenimiento": [
    "Cinépolis", "Cinemex", "Steam", "PlayStation Store", "Xbox Store",
    "Nintendo eShop", "Concierto", "Six Flags", "Museo", "Cine", "Ocio", "Boletos",
  ],
  "Suscripciones": [
    "Netflix", "Spotify", "Disney+", "Amazon Prime", "Max",
    "YouTube Premium", "Google One", "Dropbox", "Apple Music", "Microsoft 365",
    "Streaming", "Suscripción",
  ],
  "Inversión": [
    "CETES", "GBM", "Nu Ahorro", "AFORE", "Fondo Indexado",
    "ETF Vanguard", "Compra Acciones", "Inversión", "Ahorro",
  ],
  "Deudas": [
    "Pago TDC BBVA", "Pago TDC Banamex", "Pago TDC Santander",
    "Pago Préstamo Personal", "Liverpool Crédito", "Pago Nómina Kueski",
    "Tarjeta de Crédito", "Prestamo", "Deuda",
  ],
  "Seguros": [
    "GNP Seguros", "AXA Seguros", "Seguros Monterrey", "Seguro Auto Qualitas", "Seguro",
  ],
  "Ropa": [
    "Liverpool", "Zara", "H&M", "C&A", "Palacio de Hierro", "Shein", "Ropa", "Vestimenta",
  ],
  "Mascotas": [
    "Petco", "Veterinario", "PatasPet", "Mascota",
  ],
  "Otros": [
    "Transferencia SPEI", "Compra Desconocida", "Cargo Varios", "OXXO", "7-Eleven",
  ],
};

// Convierte "Alimentación" -> "alimentacion" (mismo criterio que usa el
// backend de Python para las llaves de resumen_gastos: sin acentos, en
// minúsculas, espacios como guión bajo).
function slugCategoria(nombre) {
  return nombre
    .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/\s+/g, "_");
}

// Lookup directo comercio -> categoría (slug), construido a partir del
// catálogo. Como el usuario ahora ELIGE el comercio de una lista (no
// escribe texto libre), esta categorización es exacta, no una adivinanza.
const COMERCIO_A_CATEGORIA = {};
Object.entries(CATALOGO).forEach(([categoria, comercios]) => {
  const slug = slugCategoria(categoria);
  comercios.forEach(c => { COMERCIO_A_CATEGORIA[c.toLowerCase()] = slug; });
});

/* =========================================================
   TABLA DINÁMICA DE TRANSACCIONES
   ========================================================= */
const txList = document.getElementById("tx-list");

function poblarComercios(selectComercio, categoria) {
  const comercios = CATALOGO[categoria] || [];
  selectComercio.innerHTML = comercios
    .map(c => `<option value="${c}">${c}</option>`)
    .join("");
  selectComercio.disabled = comercios.length === 0;
}

function addTxRow(categoria = "", comercio = "", valor = "") {
  const row = document.createElement("div");
  row.className = "tx-row";

  const opcionesCategoria = Object.keys(CATALOGO)
    .map(cat => `<option value="${cat}" ${cat === categoria ? "selected" : ""}>${cat}</option>`)
    .join("");

  row.innerHTML = `
    <div class="tx-row-selects">
      <select class="tx-categoria">
        <option value="" disabled ${!categoria ? "selected" : ""}>Categoría</option>
        ${opcionesCategoria}
      </select>
      <select class="tx-comercio" ${!categoria ? "disabled" : ""}>
        <option value="" disabled ${!comercio ? "selected" : ""}>Comercio</option>
      </select>
    </div>
    <div class="tx-row-bottom">
      <input type="number" class="monto" placeholder="Monto" value="${valor}" min="0" step="1">
      <button type="button" class="tx-remove" title="Eliminar">✕</button>
    </div>
  `;

  const selectCategoria = row.querySelector(".tx-categoria");
  const selectComercio = row.querySelector(".tx-comercio");

  if (categoria) {
    poblarComercios(selectComercio, categoria);
    if (comercio) selectComercio.value = comercio;
  }

  selectCategoria.addEventListener("change", () => {
    poblarComercios(selectComercio, selectCategoria.value);
  });

  row.querySelector(".tx-remove").addEventListener("click", () => row.remove());
  txList.appendChild(row);
}

document.getElementById("add-tx").addEventListener("click", () => addTxRow());

// Arranca con una fila en blanco (antes había 5 transacciones de ejemplo precargadas)
addTxRow();

/* =========================================================
   MODAL: ABRIR / CERRAR
   ========================================================= */
const modalOverlay = document.getElementById("modal-overlay");

function openModal(){
  modalOverlay.classList.add("open");
  document.body.style.overflow = "hidden";
}
function closeModal(){
  modalOverlay.classList.remove("open");
  document.body.style.overflow = "";
}

// Deja el formulario del modal como recién abierto: quita todas las
// transacciones capturadas y agrega una fila en blanco, y regresa
// ingreso/ahorro a sus valores por defecto.
function reiniciarFormulario(){
  txList.innerHTML = "";
  addTxRow();
  document.getElementById("ingreso").value = 15000;
  document.getElementById("ahorro").value = "Media";
}
document.getElementById("fab-add").addEventListener("click", openModal);
document.getElementById("modal-close").addEventListener("click", closeModal);
modalOverlay.addEventListener("click", (e) => {
  if (e.target === modalOverlay) closeModal(); // cerrar solo si se hace click en el fondo
});

/* =========================================================
   MODO DEMO — aproximación local mientras Backend no está listo.
   Reemplazar por la llamada real (ver llamarAPI) cuando exista el
   endpoint desplegado.
   ========================================================= */
// Palabras clave de respaldo, por si algún día se vuelve a permitir texto
// libre. Con el catálogo completo (COMERCIO_A_CATEGORIA, arriba) casi nunca
// se llega a usar esto, porque el usuario ahora elige el comercio de una
// lista — pero se deja como red de seguridad.
const CATEGORIAS_DEMO = {
  alimentacion: ["supermercado", "restaurante", "comida", "walmart", "soriana"],
  transporte: ["combustible", "gasolina", "uber", "didi", "taxi"],
  vivienda: ["renta", "hipoteca", "alquiler"],
  suscripciones: ["netflix", "spotify", "streaming", "disney"],
  servicios: ["luz", "agua", "internet", "telefono", "cfe"],
  entretenimiento: ["cine", "concierto", "ocio"],
  deudas: ["tdc", "tarjeta de credito", "tarjeta de crédito", "prestamo", "préstamo",
           "credito", "crédito", "deuda", "banamex", "bbva", "santander", "kueski",
           "financiera", "abono"],
};

function categorizarDemo(descripcion) {
  const texto = descripcion.toLowerCase().trim();

  // 1) Coincidencia exacta contra el catálogo (comercio elegido por el
  //    usuario) — es el caso normal y es 100% preciso.
  if (COMERCIO_A_CATEGORIA[texto]) return COMERCIO_A_CATEGORIA[texto];

  // 2) Respaldo por palabras clave, para texto que no venga del catálogo.
  for (const [categoria, palabras] of Object.entries(CATEGORIAS_DEMO)) {
    if (palabras.some(p => texto.includes(p))) return categoria;
  }
  return "otros";
}

/* =========================================================
   NIVEL DE ENDEUDAMIENTO — YA NO SE CAPTURA MANUALMENTE.
   Se calcula como: (gasto en transacciones de "deudas" / ingreso
   mensual) × 100. Así, si el usuario registra pagos de tarjeta,
   préstamos, etc., el sistema mismo estima qué tan comprometido
   está su ingreso con deudas — sin pedírselo como dato aparte.
   ========================================================= */
function calcularEndeudamiento(ingresoMensual, transacciones) {
  if (!ingresoMensual || ingresoMensual <= 0) return 0;
  const gastoDeudas = transacciones
    .filter(t => categorizarDemo(t.descripcion) === "deudas")
    .reduce((sum, t) => sum + Number(t.valor), 0);
  const porcentaje = (gastoDeudas / ingresoMensual) * 100;
  return Math.min(100, Math.round(porcentaje));
}

function mockAnalizarFinanzas(payload) {
  const gastoTotal = payload.transacciones.reduce((s, t) => s + Number(t.valor), 0);
  const ratioGasto = gastoTotal / payload.ingreso_mensual;

  let puntos = 0;
  if (payload.nivel_endeudamiento > 35) puntos += 2;
  else if (payload.nivel_endeudamiento > 18) puntos += 1;
  if (ratioGasto > 0.65) puntos += 2;
  else if (ratioGasto > 0.45) puntos += 1;
  if (payload.frecuencia_ahorro === "Nula") puntos += 2;
  else if (payload.frecuencia_ahorro === "Baja") puntos += 1;
  else if (payload.frecuencia_ahorro === "Alta") puntos -= 1;

  const perfil = puntos >= 4 ? "En riesgo" : puntos >= 2 ? "En observación" : "Saludable";

  const resumen = {};
  payload.transacciones.forEach(t => {
    const cat = categorizarDemo(t.descripcion);
    resumen[cat] = (resumen[cat] || 0) + Number(t.valor);
  });

  const recomendaciones = [];
  if (payload.nivel_endeudamiento > 35) {
    recomendaciones.push(`Tu nivel de endeudamiento (${payload.nivel_endeudamiento}%) está por encima del recomendado; prioriza las deudas con mayor tasa de interés.`);
  }
  if (payload.frecuencia_ahorro === "Nula" || payload.frecuencia_ahorro === "Baja") {
    recomendaciones.push("Aumentar tu frecuencia de ahorro, aunque sea con montos pequeños, mejoraría tu perfil financiero.");
  }
  if (ratioGasto > 0.7) {
    recomendaciones.push(`Estás gastando ${Math.round(ratioGasto*100)}% de tu ingreso mensual; busca reducir el gasto en al menos una categoría.`);
  }
  const catTop = Object.entries(resumen).sort((a,b) => b[1]-a[1])[0];
  if (catTop && catTop[1]/gastoTotal > 0.4) {
    recomendaciones.push(`Monitorea tus gastos de ${catTop[0]}, concentran ${Math.round((catTop[1]/gastoTotal)*100)}% de tu gasto total.`);
  }
  if (recomendaciones.length === 0) {
    recomendaciones.push("Tu comportamiento financiero luce saludable; mantén este ritmo de ahorro y control de gastos.");
  }

  return {
    perfil_financiero: perfil,
    probabilidad: Math.min(0.95, 0.55 + puntos * 0.08),
    resumen_gastos: resumen,
    recomendaciones,
  };
}

/* =========================================================
   LLAMADA A LA API (real o mock según CONFIG.USE_MOCK)
   ========================================================= */
async function llamarAPI(payload) {
  console.log("Entre a llamarAPI");
  console.log("analisisActualId: ", analisisActualId);
  if (CONFIG.USE_MOCK) {
    await new Promise(r => setTimeout(r, 500)); // simula latencia de red
    return mockAnalizarFinanzas(payload);
  }
  const esActualizacion = analisisActualId !== null;
  const url = esActualizacion 
    ? `${CONFIG.API_URL}/${analisisActualId}`
    : CONFIG.API_URL;

    const metodo = esActualizacion ? "PUT" : "POST";
    
    const resp = await fetch(url, {
      method: metodo,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
  });
  if (!resp.ok) throw new Error(`Error del servidor: ${resp.status}`);
  return resp.json();
}

document.getElementById("mode-hint").textContent = CONFIG.USE_MOCK
  ? "⚙ Modo demo local — aún no conectado al backend real."
  : "";

/* =========================================================
   ACTUALIZAR ANALISIS DEL USUARIO
   ========================================================= */
async function actualizarAnalisis(id, datos){
  const resp = await fetch(`${CONFIG.API_URL}/${id}`,{
    method: "PUT",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify(datos),
  });
  if (!resp.ok) throw new Error(`Error del servidor: ${resp.status}`);
  return resp.json();
}


/* =========================================================
   RENDER DE RESULTADOS
   ========================================================= */
const COLOR_PERFIL = {
  "Saludable": "var(--good)",
  "En observación": "var(--warn)",
  "En riesgo": "var(--risk)",
  "Crítico": "var(--risk)",
};

/* =========================================================
   MASCOTA DE FLOWFI — la expresión y el mensaje cambian según el
   perfil financiero real (no según un cálculo local aparte).
   ========================================================= */
const MASCOTA_IMG = "img/flowfi-mascota.png";

function rutaMascota(estado) {
  return `img/mascota-${estado}.png`;
}

const MENSAJE_MASCOTA = {
  triste:     "Tu situación financiera necesita atención urgente.",
  preocupado: "Tu situación requiere atención — vamos con calma.",
  serio:      "Vas empezando, mantente al pendiente de tus gastos.",
  confiado:   "Tu perfil financiero se ve saludable.",
  feliz:      "¡Ya estás invirtiendo! Eso es construir a futuro.",
};

// La expresión de la mascota (y del logo) se basa en el PERFIL REAL que
// calculó el backend — no en un cálculo local aparte — para que nunca
// contradiga lo que dice la tarjeta de perfil (ej. no puede decir "sin
// deudas, todo sólido" si el perfil real es "En riesgo").
function determinarEstadoMascota(resultado) {
  const perfil = resultado.perfil_financiero;

  if (perfil === "Crítico" || perfil === "En riesgo") return "triste";
  if (perfil === "En observación") return "preocupado";

  if (perfil === "Saludable") {
    const tieneInversion = Object.entries(resultado.resumen_gastos)
      .some(([cat, monto]) => cat === "inversion" && monto > 0);
    return tieneInversion ? "feliz" : "confiado";
  }

  return "serio"; // perfil no reconocido, expresión neutral
}

function renderDiagnostico(resultado, payload) {
  const color = COLOR_PERFIL[resultado.perfil_financiero] || "var(--brand)";
  const entries = Object.entries(resultado.resumen_gastos).sort((a,b) => b[1]-a[1]);
  const max = Math.max(...entries.map(e => e[1]));

  const estadoMascota = determinarEstadoMascota(resultado);
  actualizarExpresionLogo(estadoMascota);

  const barsHtml = entries.map(([cat, monto]) => `
    <div class="bar-row">
      <div class="bar-row-top">
        <span class="cat">${cat.replace(/_/g," ")}</span>
        <span class="amt">$${monto.toLocaleString("es-MX", {maximumFractionDigits:0})}</span>
      </div>
      <div class="bar-track"><div class="bar-fill" style="width:${(monto/max*100).toFixed(0)}%; background:${color};"></div></div>
    </div>
  `).join("");

  document.getElementById("diagnostico-content").innerHTML = `
    <div class="mascota-wrap">
      <img src="${rutaMascota(estadoMascota)}" alt="FlowFi ${estadoMascota}" class="mascota-img">
        <div class="mascota-eyes">
          <span class="mascota-eye"></span>
          <span class="mascota-eye"></span>
        </div>
        <span class="mascota-boca"></span>
        <span class="mascota-lagrima"></span>
        
        <!-- Mensaje único -->
        <p class="mascota-msg">${MENSAJE_MASCOTA[estadoMascota]}</p>
    </div>
    
    <div class="profile-card" style="border-color:${color}55; background:${color}0D;">
      <p class="profile-label">Tu perfil financiero</p>
      <p class="profile-value" style="color:${color};">${resultado.perfil_financiero}</p>
      <p class="profile-conf">Confianza del modelo: ${Math.round(resultado.probabilidad*100)}%</p>
    </div>
    
    <p class="bars-title">Gasto por categoría</p>
    ${barsHtml}
  `;
}

// Aplica el mismo estado de ánimo de la mascota al logo (header y dashboard),
// para que ambas caras reflejen la situación real del perfil.
function actualizarExpresionLogo(estado) {
  document.querySelectorAll(".brand-logo-wrap").forEach(wrap => {
    wrap.className = wrap.className.replace(/\blogo-mood--\w+\b/g, "").trim();
    wrap.classList.add(`logo-mood--${estado}`);
  });
}

function renderRecomendaciones(resultado) {
  const html = resultado.recomendaciones.map(rec => `
    <div class="rec-card">
      <div class="rec-icon">💡</div>
      <div class="rec-text">${rec}</div>
    </div>
  `).join("");
  document.getElementById("recomendaciones-content").innerHTML = html;
}

/* =========================================================
   RENDER DEL DASHBOARD "INICIO"
   ========================================================= */
let ultimasTransacciones = null;
let mostrarTodasTx = false;

function renderInicio(resultado, payload) {
  const color = COLOR_PERFIL[resultado.perfil_financiero] || "var(--brand)";
  const score = Math.round(resultado.probabilidad * 100);

  // --- Tarjeta de perfil + score ring ---
  document.getElementById("dash-profile-card").innerHTML = `
    <div>
      <p class="profile-label">Perfil financiero</p>
      <p class="profile-value" style="color:${color}; font-size:20px;">${resultado.perfil_financiero}</p>
      <p class="profile-conf">Score: ${score}/100</p>
    </div>
    <div class="score-ring" style="--pct:${score}; --ring-color:${color};">
      <div class="score-ring-inner"><span>${score}%</span></div>
    </div>
  `;

  // --- Stats: ingreso y ahorro son editables; endeudamiento se calcula solo ---
  const opcionesAhorro = ["Nula", "Baja", "Media", "Alta"]
    .map(op => `<option value="${op}" ${op === payload.frecuencia_ahorro ? "selected" : ""}>${op}</option>`)
    .join("");

  document.getElementById("dash-stats").innerHTML = `
    <div class="dash-stat">
      <span>Ingreso mensual</span>
      <input type="number" id="dash-input-ingreso" class="dash-input" value="${payload.ingreso_mensual}" min="0" step="500">
    </div>
    <div class="dash-stat">
      <span>Endeudamiento</span>
      <strong class="dash-stat-computed">${payload.nivel_endeudamiento}%</strong>
      <span class="dash-stat-note">calculado</span>
    </div>
    <div class="dash-stat">
      <span>Ahorro</span>
      <select id="dash-input-ahorro" class="dash-input">${opcionesAhorro}</select>
    </div>
  `;

  ["dash-input-ingreso", "dash-input-ahorro"].forEach(id => {
    document.getElementById(id).addEventListener("change", actualizarDesdeStats);
  });

  // --- Barras de gasto por categoría ---
  const entries = Object.entries(resultado.resumen_gastos).sort((a,b) => b[1]-a[1]);
  const max = Math.max(...entries.map(e => e[1]));
  document.getElementById("dash-bars").innerHTML = entries.map(([cat, monto]) => `
    <div class="bar-row">
      <div class="bar-row-top">
        <span class="cat">${cat.replace(/_/g," ")}</span>
        <span class="amt">$${monto.toLocaleString("es-MX", {maximumFractionDigits:0})}</span>
      </div>
      <div class="bar-track"><div class="bar-fill" style="width:${(monto/max*100).toFixed(0)}%; background:${color};"></div></div>
    </div>
  `).join("");

  // --- Últimas transacciones ---
  ultimasTransacciones = payload.transacciones.map(t => ({
    ...t, categoria: categorizarDemo(t.descripcion),
  }));
  mostrarTodasTx = false;
  renderTxList();
}

function renderTxList() {
  const visibles = mostrarTodasTx ? ultimasTransacciones : ultimasTransacciones.slice(0, 3);
  document.getElementById("dash-tx-list").innerHTML = visibles.map(t => `
    <div class="dash-tx-row">
      <div>
        <span class="tx-desc">${t.descripcion}</span>
        <span class="tx-cat">${t.categoria.replace(/_/g," ")}</span>
      </div>
      <span class="tx-amt">-$${Number(t.valor).toLocaleString("es-MX", {maximumFractionDigits:0})}</span>
    </div>
  `).join("");

  const verTodasBtn = document.getElementById("dash-ver-todas");
  verTodasBtn.textContent = mostrarTodasTx ? "Ver menos" : "Ver todas";
  verTodasBtn.style.display = ultimasTransacciones.length > 3 ? "block" : "none";
}

document.getElementById("dash-ver-todas").addEventListener("click", () => {
  mostrarTodasTx = !mostrarTodasTx;
  renderTxList();
});

/* =========================================================
   EDICIÓN INLINE DE INGRESO / ENDEUDAMIENTO / AHORRO
   desde las tarjetas del dashboard — reutiliza las transacciones
   ya cargadas y vuelve a analizar con los valores nuevos.
   ========================================================= */
function actualizarDesdeStats() {
  const ingresoMensual = Number(document.getElementById("dash-input-ingreso").value);

  if (!ingresoMensual || Number.isNaN(ingresoMensual) || ingresoMensual <= 0) {
    mostrarToast("El ingreso mensual debe ser mayor a 0.");
    return;
  }

  const transacciones = ultimasTransacciones.map(({ descripcion, valor }) => ({ descripcion, valor }));

  const payload = {
    ingreso_mensual: ingresoMensual,
    nivel_endeudamiento: calcularEndeudamiento(ingresoMensual, transacciones),
    frecuencia_ahorro: document.getElementById("dash-input-ahorro").value,
    transacciones,
  };
  ejecutarAnalisis(payload);
}

/* =========================================================
   BOTÓN "ANALIZAR"
   ========================================================= */
async function ejecutarAnalisis(payload, { esModal } = {}) {
  const boton = document.getElementById("analizar-btn");
  if (esModal) {
    boton.disabled = true;
    boton.textContent = "Analizando...";
  }

  try {
    const resultado = await llamarAPI(payload);
    analisisActualId = resultado.id ?? analisisActualId;
    renderDiagnostico(resultado, payload);
    renderRecomendaciones(resultado);
    renderInicio(resultado, payload);

    if (esModal) {
      closeModal();
      goToScreen("inicio");
      reiniciarFormulario();
    }
  } catch (err) {
    mostrarToast("Ocurrió un error al analizar tus finanzas: " + err.message);
  } finally {
    if (esModal) {
      boton.disabled = false;
      boton.textContent = "Analizar mi salud financiera";
    }
  }
}

function leerPayloadDelFormulario() {
  const filasCrudas = [...document.querySelectorAll(".tx-row")].map(row => ({
    descripcion: row.querySelector(".tx-comercio").value,
    valor: Number(row.querySelector(".monto").value),
  })).filter(t => t.descripcion); // solo filas donde ya se eligió un comercio

  if (filasCrudas.length === 0) return null; // formulario vacío, sin mensaje (pasa en la carga inicial)

  // Antes esto se descartaba en silencio; ahora avisamos en vez de que el
  // usuario no entienda por qué su transacción "desapareció".
  const montoInvalido = filasCrudas.some(t => Number.isNaN(t.valor) || t.valor <= 0);
  if (montoInvalido) {
    mostrarToast("El monto de cada transacción debe ser mayor a 0.");
    return null;
  }

  const ingresoMensual = Number(document.getElementById("ingreso").value);
  if (!ingresoMensual || Number.isNaN(ingresoMensual) || ingresoMensual <= 0) {
    mostrarToast("El ingreso mensual debe ser mayor a 0.");
    return null;
  }

  // Aviso inmediato si los gastos superan el ingreso (no bloquea el envío:
  // el backend igual va a marcar el perfil como "Crítico" y a explicarlo
  // en Recomendaciones, esto es solo para que se note al instante).
  const gastoTotal = filasCrudas.reduce((s, t) => s + t.valor, 0);
  if (gastoTotal > ingresoMensual) {
    mostrarToast("Tus gastos registrados superan tu ingreso mensual — tu perfil se marcará como Crítico.");
  }

  return {
    ingreso_mensual: ingresoMensual,
    nivel_endeudamiento: calcularEndeudamiento(ingresoMensual, filasCrudas),
    frecuencia_ahorro: document.getElementById("ahorro").value,
    transacciones: filasCrudas,
  };
}

document.getElementById("analizar-btn").addEventListener("click", () => {
  const hayAlgoEscrito = [...document.querySelectorAll(".tx-row")]
    .some(row => row.querySelector(".tx-comercio").value);

  const payload = leerPayloadDelFormulario();
  if (!payload) {
    // Si ya se mostró un aviso específico (monto/ingreso inválido) no lo
    // repetimos; el genérico solo aplica cuando de plano no hay nada capturado.
    if (!hayAlgoEscrito) mostrarToast("Agrega al menos una transacción válida.");
    return;
  }
  ejecutarAnalisis(payload, { esModal: true });
});

// Al cargar la página ya no hay transacciones de ejemplo precargadas, así
// que solo disparamos el análisis automático si el formulario ya trae datos
// válidos (evita el 400 de mandar un payload vacío al backend).
const payloadInicial = leerPayloadDelFormulario();
if (payloadInicial) {
  ejecutarAnalisis(payloadInicial);
}
