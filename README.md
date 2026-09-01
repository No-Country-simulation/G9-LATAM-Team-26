# 💲FlowFi 🤖
<img src="https://i.imgur.com/kn09wK6.png" alt="Banner FlowFi"/>

<p align="center">
  <!-- Backend -->
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk" height="22" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot" height="22" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven" height="22" alt="Maven">
  <img src="https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" height="22" alt="Swagger">
  <!-- Data / API -->
  <img src="https://img.shields.io/badge/Python-3.x-3776AB?style=for-the-badge&logo=python&logoColor=white" height="22" alt="Python">
  <img src="https://img.shields.io/badge/API-FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white" height="22" alt="FastAPI">
  <img src="https://img.shields.io/badge/Oracle_Database-F80000?style=for-the-badge&logo=oracle&logoColor=white" height="22" alt="Oracle Database">
  <!-- Frontend / Cloud -->
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white" height="22" alt="HTML5">
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white" height="22" alt="CSS3">
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black" height="22" alt="JavaScript">
  <img src="https://img.shields.io/badge/Oracle_Cloud-OCI-F80000?style=for-the-badge&logo=oracle" height="22" alt="OCI">
  <img src="https://img.shields.io/badge/Render-Deploy-46E3D3?style=for-the-badge&logo=render&logoColor=black" height="22" alt="Render">

FlowFi es un asistente inteligente de salud financiera desarrollado durante el Hackathon Oracle & Alura por el G9 LATAM Team 26.

La aplicación analiza las transacciones y la información financiera de una persona para convertir datos financieros en información clara y accionable. A partir de los datos proporcionados por el usuario, FlowFi clasifica automáticamente sus gastos, evalúa su perfil financiero y genera recomendaciones personalizadas que ayudan a comprender y mejorar sus hábitos financieros.

> [!NOTE]
> FlowFi no reemplaza a un asesor financiero. Su propósito es proporcionar al usuario un primer diagnóstico que le permita comprender mejor su situación financiera y tomar decisiones más conscientes.

Flowfi combina Ciencia de Datos, Machine Learning, una API REST, un microservicio de inferencia, una interfaz web e infraestructura en Oracle Cloud Infrastructure (OCI).


## 📖 Descripción del proyecto

Muchas personas tienen acceso a la información de sus transacciones mediante aplicaciones bancarias o estados de cuenta, pero no cuentan con una forma sencilla de convertir esos datos en información útil para tomar decisiones financieras.

**FlowFi** busca solucionar este problema transformando las transacciones y hábitos financieros del usuario en un diagnóstico financiero claro, inmediato y accionable.

Para realizar el análisis, la aplicación utiliza información como:

- 💵 Ingreso mensual.
- 💰 Frecuencia de ahorro.
- 💳 Nivel de endeudamiento.
- 🧾 Descripción de las transacciones.
- 💸 Monto de cada transacción.
- 🏷️ Categorias de gastos

A partir de estos datos, **FlowFi** es capaz de:

- 📊 Clasificar automáticamente las transacciones por categoría.
- 📈 Analizar el comportamiento financiero.
- 👤 Determinar el perfil financiero del usuario mediante Machine Learning.
- 📉 Generar un diagnóstico sobre su situación financiera.
- 💡 Proporcionar recomendaciones personalizadas.
- 🔎 Mostrar los factores que tuvieron mayor influencia en el diagnóstico.

---

# 🖥️ Uso de la aplicación

**FlowFi** es una aplicación web que permite realizar un análisis financiero sin necesidad de crear una cuenta o instalar una aplicación.

Para crear un análisis:

1. Selecciona el botón +.
2. Ingresa tu **ingreso mensual**.
3. Selecciona tu **frecuencia de ahorro**.
4. Registra tus transacciones.
5. Indica el comercio o concepto.
6. Ingresa el monto correspondiente.
7. Selecciona **"Analizar mi salud financiera"**.
8. Consulta el diagnóstico generado.

Si se registran pagos de tarjetas o préstamos dentro de la categoría Deudas, la aplicación puede calcular automáticamente el nivel de endeudamiento.

> 💡 La interfaz también cuenta con una mascota cuya expresión cambia dependiendo de la situación financiera detectada.


<table>
  <tr>
    <td align="center">
      <img src="https://i.imgur.com/UJHjarP.png" alt="Inicio Flowfi" width="250">
      <br>
      <strong>Pantalla principal</strong>
    </td>
    <td align="center">
      <img src="https://i.imgur.com/AiZxneP.png" alt="Diagnostico" width="250">
      <br>
      <strong>Diagnóstico</strong>
    </td>
    <td align="center">
      <img src="https://i.imgur.com/nq6shL5.png" alt="Recomendaciones" width="250">
      <br>
      <strong>Recomendaciones</strong>
    </td>
    <td align="center">
      <img src="https://i.imgur.com/yr0cDYB.png" alt="Nuevo Análisis" width="250">
      <br>
      <strong>Nuevo Análisis</strong>
    </td>
  </tr>
</table>

---


# 🏗 Arquitectura del proyecto

**FlowFi** sigue una arquitectura de 3 capas desacopladas, donde cada componente tiene una responsabilidad especifica:

```text
                        ┌─────────────────────────┐
                        │        FRONTEND         │
                        │                         │
                        │    HTML5 / CSS3 / JS    │
                        │ Interfaz web responsiva │
                        └────────────┬────────────┘
                                     │
                                     │ HTTP / JSON
                                     ▼
                     ┌─────────────────────────────────┐
                     │            BACKEND              │
                     │        Java / Spring Boot       │
                     │                                 │
                     │ • API REST                      │
                     │ • Validación                    │
                     │ • Reglas de negocio             │
                     │ • Persistencia                  │
                     └───────────────┬─────────────────┘
                                     │
                                     │ HTTP / JSON
                                     ▼
                     ┌─────────────────────────────────┐
                     │       MICROSERVICIO ML          │
                     │         Python / FastAPI        │
                     │                                 │
                     │ • Clasificación de transacciones│
                     │ • Predicción del perfil         │
                     │ • Inferencia de modelos         │
                     └───────────────┬─────────────────┘
                                     │
                                     │ Modelos .joblib
                                     ▼
                     ┌─────────────────────────────────┐
                     │      ORACLE CLOUD OCI           │
                     │        Object Storage           │
                     │                                 │
                     │ • modelo_perfil_financiero      │
                     │ • modelo_clasificador_...       │
                     │ • vectorizer_transacciones      │
                     └─────────────────────────────────┘

```

La separación entre Java y Python permite que el equipo de Ciencia de Datos pueda actualizar los modelos sin modificar la lógica de negocio del backend. El backend Java se mantiene enfocado en la validación, las reglas de negocio y la persistencia, mientras que FastAPI se encarga exclusivamente de servir los modelos de Machine Learning.

---

# 🛠️ Tecnologías utilizadas

## 🧠 Ciencia de Datos

|Tecnología	|Uso                                                   |
|-----------------|------------------------------------------------|
|Python	          |Pipeline de datos y microservicio de inferencia |
|Pandas	          |Limpieza, exploración e ingeniería de atributos |
|Scikit-learn	    |Entrenamiento y evaluación de modelos           |
|RandomForestClassifier	|Predicción del perfil financiero          |
|LogisticRegression	    |Clasificación de transacciones            |
|TF-IDF	          |Transformación de descripciones de transacciones|
|Joblib	          |Serialización de modelos                        |
|Jupyter Notebook	|Experimentación y desarrollo del pipeline       |

## ⚙️ Backend

|Tecnología	|Uso                                                      |
|-----------------|---------------------------------------------------|
|Java 17	        |Lenguaje principal                                 |
|Spring Boot 4.1	|Framework del backend                              |
|Spring Data JPA	|Persistencia                                       |
|H2	              |Base de datos utilizada para persistencia          |
|Spring Validation|Validación de datos de entrada                     |
|springdoc-openapi|	Documentación interactiva de la API               |
|Swagger	        |Documentación y pruebas de endpoints               |
|Maven	          |Gestión de dependencias y construcción             |

## 🤖 MicroServicio de Machine Learning
|Tecnología	|Uso                                                      |
|-----------------|---------------------------------------------------|
|Python	          |Lenguaje del microservicio                         |
|FastAPI	        |Exposición de los modelos mediante API REST        |
|Uvicorn	        |Servidor ASGI                                      |
|Pydantic	        |Validación y serialización de datos                |


## 🌐 Frontend
- HTML
- CSS3
- JavaScript
- Diseño responsivo/mobile-first
- Modo claro y obscuro.

## 🔧 Infraestructura

- Oracle Cloud Infrastructure (OCI)
- OCI Object Storage
- Docker
- Render
---
# ☁️ Integración con Oracle Cloud Infrastructure

FlowFi utiliza Oracle Cloud Infrastructure (OCI) mediante Object Storage.

Los siguientes artefactos de Machine Learning se almacenan en OCI:
- modelo_perfil_financiero.joblib
- modelo_clasificador_transacciones.joblib
- vectorizer_transacciones.joblib

El microservicio FastAPI descarga estos archivos automáticamente cuando inicia utilizando Pre-Authenticated Requests (PAR).

Las PAR permiten proporcionar acceso temporal y de solo lectura a objetos específicos sin exponer las credenciales de OCI dentro del código fuente o del servidor. Las URLs se proporcionan mediante variables de entorno durante el despliegue.

---

# ⚙️ Instalación del proyecto

## 1. Clonar el repositorio

```bash
git clone https://github.com/No-Country-simulation/G9-LATAM-Team-26.git

cd G9-LATAM-Team-26
```

## 2. Backend Java
Ingresa al directorio correspondiente al backend y ejecuta:

Linux / macOS

```bash
./mvnw clean install
```

Windows
```bash
mvnw.cmd clean install
```
O utilizando Maven instalado:

```bash
mvn clean install
```

Para iniciar el servicio:
```bash
./mvnw spring-boot:run
```

En Windows:
```bash
mvnw.cmd spring-boot:run
```

## 3. Microservicio de Machine Learning

Ingresar al directorio correspondiente al servicio Python e instalar las dependencias:

```bash
pip install -r requirements.txt
```

Ejecutar el servicio mediante Uvicorn:

```bash
uvicorn <modulo>:app --reload
```

Reemplaza `<modulo>` por el módulo principal definido en el repositorio

---

## 4. Variables de entorno

El proyecto utiliza variables de entorno para configurar el acceso a los modelos almacenados en OCI.

Las URLs de los Pre-Authenticated Requests (PAR) deben configurarse en el entorno de ejecución y no deben almacenarse directamente en el repositorio. 

Ejemplo conceptual:

- `OCI_URL_MODELO_CLASIFICADOR`= `<URL_CLASIFICADOR>`
- `OCI_URL_MODELO_PERFIL` = `<URL_MODELO_PERFIL>`
- `OCI_URL_VECTORIZER`    = `<URL_VECTORIZER>`
---

# 🚀 Probar la aplicación en línea
### 1. 🤖 Despertar el microservicio de Machine Learning

Ingresa a [FlowFi ML Service](https://flowfi-ml-service.onrender.com/) y espera hasta que aparezca:

```json
{
  "status": "ok",
  "servicio":"finance-ai-ml-service"
}
```

### 2. ⚙️ Despertar el backend
Ingresa a [FlowFi Backend](https://flowfi-backend-java.onrender.com/) y espera hasta que el servicio responda:

```json
{
  "status": "ok",
  "servicio":"financeai-backend-java"
}
```
Esto indica que el backend y el microservicio de Machine Learning está activo y listo para recibir solicitudes.

### 3. 🌐 Abrir FlowFi
Finalmente, ingresa a [Flowfi Interfaz](https://flowfi-frontend.onrender.com/) 

Una vez cargada la interfaz, podrás realizar un análisis financiero y probar las funcionalidades principales de FlowFi.

> [!IMPORTANT]
> Es necesario despertar primero el microservicio ML, después el backend y finalmente acceder al frontend. Si la aplicación no responde inmediatamente, espera unos segundos y vuelve a cargar la página.

--- 

# 📂 Estructura del proyecto
La arquitectura documentada separa el frontend, backend y microservicio de Machine Learning.

```text
G9-LATAM-Team-26/
│
├── backend/src
│   │      ├── main
│   │      │   ├── java
│   │      │   │   ├── client
│   │      │   │   ├── config
│   │      │   │   ├── controller
│   │      │   │   ├── dto
│   │      │   │   ├── entity
│   │      │   │   ├── exception
│   │      │   │   ├── repository
│   │      │   │   └── service
│   │      │   └── resources
│   │      │       ├── application.properties
│   │      │       └── static
│   │      └── test
│   ├── pom.xml
│   └── Dockerfile
│
├── data-science
│         └── dataset
│ 
├── fastapi-service
│         ├── app
│         ├── models
│         └── tests
└── frontend
          ├── img
          ├── app.js
          ├── ejemplo-transacciones.csv
          ├── ejemplo-transacciones2.csv
          ├── index.html
          └── styles.css
```
---

# 👥 Integrantes 
### G9 LATAM Team 26

| Nombre  | Rol |
|---------|-----|
| [Juan Armando Cosco](https://github.com/JuanCosco)                 | FullStack Developer |
| [Frank Molina](https://github.com/FrankMolinaCuevas)                       | Backend Developer   |
| [Haziel Ibares](https://github.com/Haziel08)                      | Backend Developer   |
| [Diego Aldrighetti](https://github.com/Sorx4s)                  | Backend Developer   |
| [Brenda Mitzi Romero Quezada](https://github.com/Mitziq)        | Data Scientist      |
| [Angela Olivares](https://github.com/angelaoica-dev)                    | Data Scientist      |
---

# 💛 Agradecimientos

Queremos expresar nuestro más sincero agradecimiento a **Oracle** y **Alura Latam** por brindarnos la oportunidad de formar parte de esta experiencia de aprendizaje.

Gracias al programa **Oracle Next Education (ONE)** hemos fortalecido nuestros conocimientos en desarrollo de software, computación en la nube, ciencia de datos y buenas prácticas de ingeniería, permitiéndonos afrontar retos reales y continuar creciendo como profesionales.

Asimismo, extendemos nuestro agradecimiento a **No Country** por abrirnos las puertas para participar en este Hackathon. Esta experiencia nos permitió aplicar nuestros conocimientos en un entorno colaborativo, enfrentar desafíos del mundo real y desarrollar una solución con un enfoque innovador.

Finalmente, queremos reconocer el compromiso y la dedicación de todos los integrantes del **Equipo 26**. Cada miembro aportó ideas, conocimientos y esfuerzo en las distintas etapas del desarrollo, demostrando que el trabajo en equipo, la colaboración y el aprendizaje compartido son fundamentales para alcanzar objetivos comunes.

**¡Gracias a todos por hacer posible este proyecto y por impulsar el crecimiento de la comunidad tecnológica! 🚀✨**

<a href="https://github.com/No-Country-simulation/G9-LATAM-Team-26">
  <img src="https://contrib.rocks/image?repo=No-Country-simulation/G9-LATAM-Team-26" />
</a>