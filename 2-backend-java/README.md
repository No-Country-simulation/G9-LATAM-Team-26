# 💰 Finance AI — Asistente Inteligente de Salud Financiera FlowFi
### Backend — Hackathon Oracle & Alura

Este `README` contiene el trabajo del equipo de **Backend** para el asistente de salud financiera: 

Este servicio funciona como la capa principal de negocio de la aplicación. Se encarga de recibir las solicitudes del frontend, validar los datos, gestionar la información financiera y comunicarse con el microservicio de Machine Learning desarrollado en FastAPI.

El backend está organizado siguiendo una separación por responsabilidades, utilizando controladores, servicios, repositorios, entidades, DTOs, clientes externos, configuración y manejo centralizado de excepciones.

---

## 🛠️ Tecnologías utilizadas
| Tecnologia       | Uso                                 |
|------------------|--------------------------------     |
|Java 17           | Lenguaje principal                  |
|Spring Boot 4.1.0 | Framework principal                 |
|Spring Web MVC    | Desarrollo de API REST              |
|H2 Database       | Base de datos utilizada en ejecución|
|Spring Validation | Validación de datos de entrada      |
|Spring RestClient | Comunicación con servicios externos|
|Lombok            | Reducción de código repetitivo     |
|Jackson           | Serialización/deserialización JSON |
|SpringDoc OpenAPI | Documentación de la API            |
|Maven             | Gestión de dependencias y build    |
|Docker            | Contenerización                    |

Las dependencias y versiones utilizadas se encuentran definidas en `pom.xml`. El proyecto está configurado en Java 17 y SpringBoot 4.1.0.

---

## 🔌 API REST
La API REST desarrollada cuenta con los siguientes Endpoints:

|`Método`	 |Endpoint	  | Descripción  |
|----------|------------|--------------|
|`POST`	|`/analisis-financiero`	|Realiza un análisis financiero completo|
|`GET`	|`/analisis-financiero/{id}`	|Consulta un análisis guardado|
|`PUT`	|`/analisis-financiero/{id}`	|Edita un análisis y recalcula el diagnóstico|
|`POST`	|`/clasificar-transaccion`	|Clasifica una o varias transacciones|

### 📌 Ejemplos de uso

#### Solicitud

```json
{
  "ingreso_mensual": 4500,
  "nivel_endeudamiento": 25,
  "frecuencia_ahorro": "Media",
  "transacciones": [
    {
      "descripcion": "Supermercado",
      "valor": 420
    },
    {
      "descripcion": "Combustible",
      "valor": 300
    },
    {
      "descripcion": "Streaming",
      "valor": 40
    }
  ]
}
```

#### Respuesta

```json
{
  "perfil_financiero": "En observación",
  "probabilidad": 0.82,
  "resumen_gastos": {
    "alimentacion": 420,
    "transporte": 300,
    "entretenimiento": 40
  },
  "recomendaciones": [
    "Monitorear gastos recurrentes de entretenimiento",
    "Aumentar reserva financiera mensual"
  ]
}
```
---

## 📁 Estructura del proyecto
```bash
2-backend-java/
│
├── src/
│   ├── main/
│   │   ├── java/com/equipo26/financeai/
│   │   │   ├── client/        # Comunicación con servicios externos
│   │   │   ├── config/        # Configuraciones de la aplicación
│   │   │   ├── controller/    # Endpoints REST
│   │   │   ├── dto/           # Objetos de transferencia de datos
│   │   │   ├── entity/        # Entidades de persistencia
│   │   │   ├── exception/     # Manejo de excepciones
│   │   │   ├── repository/    # Acceso a datos
│   │   │   ├── service/       # Lógica de negocio
│   │   │   └── FinanceaiApplication.java
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       └── application.properties
│   │
│   └── test/                  # Pruebas automatizadas
│
├── .mvn/                      # Maven Wrapper
├── Dockerfile                 # Configuración para Docker
├── pom.xml                    # Dependencias y configuración Maven
├── mvnw / mvnw.cmd            # Maven Wrapper
├── .dockerignore
├── .gitignore
└── .gitattributes

```

## 🏛️ Arquitectura
El backend sigue una arquitectura por capas.

                    ┌─────────────────────┐
                    │      Frontend       │
                    └──────────┬──────────┘
                               │
                               │ HTTP / JSON
                               ▼
                    ┌─────────────────────┐
                    │     Controller      │
                    │      REST API       │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │       Service       │
                    │    Lógica negocio   │
                    └───────┬─────┬───────┘
                            │     │
                  ┌─────────┘     └─────────┐
                  ▼                         ▼
        ┌──────────────────┐      ┌──────────────────┐
        │    Repository    │      │      Client      │
        │    Persistencia  │      │   FastAPI / ML   │
        └────────┬─────────┘      └────────┬─────────┘
                 │                         │
                 ▼                         ▼
        ┌──────────────────┐      ┌──────────────────┐
        │  Base de datos   │      │ FastAPI Service  │
        │       H2         │      │ Machine Learning │
        └──────────────────┘      └──────────────────┘

Los DTOs actúan como objetos de transferencia entre las diferentes capas y la API, mientras que las entidades representan los objetos persistidos.

## 📦 Paquetes
### 🕹️ Controller
Contiene los controladores REST encargados de exponer los endpoints de la aplicación y recibir las solicitudes HTTP.

```
controller/
├── FinancialController.java
├── HealthController.java
└── TransactionController.java
```
- `FinancialController`: Gestiona las solicitudes relacionadas con el análisis financiero y delega el procesamiento al servicio correspondiente.
- `HealthController`: Proporciona un endpoint para verificar la disponibilidad del backend.
- `TransactionController` : Gestiona las solicitudes relacionadas con las transacciones financieras.

### 🔩 Service
Contiene la lógica de negocio de la aplicación.
```
service/
├── FinancialService.java
└── FinancialServiceImpl.java
```
- `FinancialService`: Es la interfaz que define las operaciones relacionadas con el procesamiento financiero.
- `FinancialServiceImpl`: Implementa la lógica de negocio y coordina las operaciones necesarias para procesar las solicitudes financieras.

### 📦 DTO
Los DTO (Data Transfer Objects) representan los datos intercambiados entre el cliente, el backend y los servicios externos, evitando exponer directamente las entidades internas.

```
dto/
├── FinancialRequest.java
├── FinancialResponse.java
├── FrecuenciaAhorro.java
├── MlAnalysisResponse.java
├── ClasificarTransaccionesRequest.java
├── TransaccionClasificadaDTO.java
└── TransactionDTO.java
```
- `FinancialRequest`: Datos enviados para solicitar un análisis financiero.
- `FinancialResponse`: Información devuelta al cliente después del análisis.
- `FrecuenciaAhorro`: Representa la frecuencia de ahorro del usuario.
- `MlAnalysisResponse`: Representa la respuesta recibida desde el servicio de Machine Learning.
- `ClasificarTransaccionesRequest`: Datos necesarios para solicitar la clasificación de transacciones.
- `TransaccionClasificadaDTO`: Representa una transacción después de ser procesada y clasificada.
- `TransactionDTO`: Representa la información de una transacción utilizada en el intercambio de datos.

### 🧱 Entity
Contiene las entidades que representan información persistida.
```
entity/
└── AnalisisFinanciero.java
```
- `AnalisisFinanciero`: Entidad JPA utilizada para mapear la información de los análisis financieros con la base de datos.

### 🗄️ Repository
Contiene las interfaces encargadas del acceso a datos mediante Spring Data JPA.
```
repository/
└── AnalisisFinancieroRepository.java
```
- `AnalisisFinancieroRepository`: Permite consultar y persistir información relacionada con los análisis financieros.


### 🤖 Client
Este paquete contiene la comunicación con servicios externos.
```
client/
└── MlServiceClient.java
```
- `MlServiceClient`: Se comunica con el microservicio de Machine Learning desarrollado con FastAPI.

### ⚠️ Exception
Contiene las clases relacionadas con el manejo de errores y excepciones
```
exception/
├── ErrorResponse.java
├── FinancialNotFoundException.java
├── GlobalExceptionHandler.java
├── MlServiceException.java
├── ModelUnavailableException.java
└── ValidationErrorResponse.java
``` 
- `GlobalExceptionHandler`: Centraliza el manejo de excepciones y genera respuestas HTTP consistentes.
- `FinancialNotFoundException`: Indica que no se encontró información financiera requerida.
- `MlServiceException`: Representa errores relacionados con la comunicación o procesamiento del servicio de Machine Learning..
- `ModelUnavailableException`: Indica que el modelo de Machine Learning no se encuentra disponible
- `ErrorResponse`: Define la estructura de las respuestas de error.
- `ValidationErrorResponse`: Representa los errores producidos durante la validación de datos.

### 🔧 config
Contiene las configuraciones adicionales de Spring.
```
config/
├── CorsConfig.java
└── OpenApiConfig.java
```
- `CorsConfig`: Configura las políticas CORS y controla qué aplicaciones pueden realizar solicitudes al backend.
- `OpenApiConfig`: Configura la documentación de la API mediante OpenAPI/Swagger.

### 🚀 FinanceApplication
Clase principal de la aplicación Spring Boot:
```
com.equipo26.financeai
└── FinanceApplication.java
```
- `FinanceaiApplication`: Punto de entrada de la aplicación Spring Boot. Inicializa el contexto de Spring y levanta el servidor.

### 🧪 Test
Contiene las pruebas automatizadas del backend.
```
src/test/java/com/equipo26/
└──financeai/
```
Su objetivo es verificar el comportamiento de los diferentes componentes de la aplicación y detectar errores sin depender del frontend.


### ⚙️ Resources
Contiene los recursos y archivos de configuración de Spring Boot.


```
src/main/resources/
├── static/
└── application.properties
```
El archivo `application.properties` contiene configuraciones como:
- Puerto de la aplicación.
- Configuración de base de datos.
- Configuración JPA.
- URL del servicio FastAPI.
- Configuración de logs.
- Variables específicas del entorno.

> [!NOTE]
> Las credenciales, API Keys y demás información sensible deberían gestionarse mediante variables de entorno.

---

### 📌 Resumen de responsabilidades
|Componente |	Responsabilidad                |
|-----------|------------------                |
|`controller`	|Exponer endpoints REST            |
|`service`	|Implementar lógica de negocio     |
|`repository` |	Acceder a la base de datos     |
|`entity`	    |Representar entidades persistentes|
|`dto`	    |Transportar información           |
|`client`	    |Comunicarse con servicios externos|
|`config`	    |Configuración de Spring           |
|`exception`  |	Manejar errores                |
|`resources`  |	Configuración y recursos       |
|`test`     |	Pruebas automatizadas          |
---

## 📚 Dependencias
Las dependencias estan administradas mediante Maven en el archivo `pom.xml`
- `spring-boot-starter-validation`: Se utiliza para validar los datos recibidos mediante la API. Permite utilizar anotaciones como `@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`.
- `spring-boot-starter-webmvc`: Proporciona la infraestructura necesaria para consultar la API REST.
- `spring-boot-starter-resclient`: Se utiliza para realizar llamadas HTTP desde el backend hacia servicios externos, especialmente el servicio de Machine Learning.
- `spring-boot-starter-data-jpa`: Proporciona integración con JPA para trabajar con la persistencia de datos.
- `com.h2database:h2`: Base de datos utilizada en tiempo de ejecución para el proyecto actual.
- `org.projectlombok:lombok`: Reduce código repetitivo mediante anotaciones como `@Getter`,`@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- `com.fasterxml.jackson.core:jackson-databind`: Se utiliza para convertir objetos Java a JSON y viceversa.
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`: Permite generar la documentación OpenAPI/Swagger para los endpoints de la aplicación.

## ▶️ Ejecución local
**Requisitos**

Antes de ejecutar el proyecto se recomienda tener instalado:
- Java 17
- Maven, o utilizar el Maven Wrapper incluido
- Git
- Docker (opcional)

El proyecto incluye Maven Wrapper:
```
mvnw
mvnw.cmd
```
Clonar el repositorio:
```bash
git clone https://github.com/No-Country-simulation/G9-LATAM-Team-26.git
```
Entrar al backend:
```bash
cd G9-LATAM-Team-26/2-backend-java
```
**Ejecutar en 🐧Linux/🍎macOS:**
```zsh
./mvnw spring-boot:run
```
**Ejecutar en 🪟 Windows:**
```bash
mvnw.cmd spring-boot:run
```





