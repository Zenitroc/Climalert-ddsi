# Climalert

Franco M. Cortinez (@Zenitroc) - Legajo: 1762060

## Descripción

Climalert es un sistema autónomo de monitoreo climático y envío automático de alertas.

El sistema no posee interfaz gráfica propia. Su funcionamiento se basa en tareas programadas que se ejecutan periódicamente para:

1. Consultar información climática actual desde WeatherAPI.
2. Guardar las mediciones obtenidas en una base de datos local.
3. Analizar la última medición disponible.
4. Generar una alerta si se detectan condiciones climáticas críticas.
5. Enviar una notificación por correo electrónico mediante Mailtrap.

Para esta primera iteración, se considera una condición crítica cuando:

- La temperatura es mayor a 35 °C.
- La humedad es superior a 60 %.

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Scheduling
- Spring Data JPA
- H2 Database
- RestClient
- Lombok
- Maven
- WeatherAPI
- Mailtrap API

## Integraciones externas

### WeatherAPI

El sistema se integra con WeatherAPI mediante una interfaz saliente REST.

Endpoint utilizado:

GET /current.json

Base URL configurada:

https://api.weatherapi.com/v1

Parámetros utilizados:

key  -> API Key de WeatherAPI
q    -> ubicación consultada
lang -> idioma de respuesta

La ubicación configurada por defecto corresponde a CABA mediante coordenadas:

-34.6037,-58.3816

La información obtenida desde WeatherAPI se transforma en una entidad MedicionClima y se guarda localmente para registro histórico.

### Mailtrap

El sistema utiliza Mailtrap API para simular el envío de correos electrónicos.

Endpoint utilizado:

POST https://send.api.mailtrap.io/api/send

El correo se envía cuando se genera una alerta climática.

Por defecto, los destinatarios configurados son:

admin@clima.com
emergencias@clima.com
meteorologia@clima.com

Para pruebas con Mailtrap Demo Domain, puede ser necesario sobrescribir los destinatarios y usar el mail asociado a la cuenta de Mailtrap.

## Tareas programadas

El sistema posee dos tareas programadas principales.

### Obtener clima actual

Consulta WeatherAPI y guarda una nueva medición climática.

Configuración original del TP:

obtener-clima: "0 */5 * * * *"

Esto significa: cada 5 minutos.

### Analizar alertas

Analiza la última medición guardada. Si la medición es crítica y todavía no tiene una alerta asociada, genera una alerta y envía un correo.

Configuración original del TP:

analizar-alertas: "0 * * * * *"

Esto significa: cada 1 minuto.

Durante desarrollo se pueden usar valores más cortos para probar:

obtener-clima: "*/15 * * * * *"
analizar-alertas: "*/10 * * * * *"

## Variables de entorno

El proyecto utiliza variables de entorno para evitar subir credenciales al repositorio público.

### Variables obligatorias

#### WEATHER_API_KEY

API Key de WeatherAPI.

PowerShell:

$env:WEATHER_API_KEY="TU_API_KEY_DE_WEATHERAPI"

#### MAILTRAP_TOKEN

Token de autenticación de Mailtrap.

PowerShell:

$env:MAILTRAP_TOKEN="TU_TOKEN_DE_MAILTRAP"

### Variables opcionales

#### MAILTRAP_TO_EMAILS

Permite sobrescribir los destinatarios del correo.

Por defecto:

admin@clima.com,emergencias@clima.com,meteorologia@clima.com

Ejemplo para pruebas con Mailtrap:

$env:MAILTRAP_TO_EMAILS="tu-email@dominio.com"

#### MAILTRAP_FROM_EMAIL

Permite sobrescribir el remitente.

Por defecto:

hello@demomailtrap.co

Ejemplo:

$env:MAILTRAP_FROM_EMAIL="hello@demomailtrap.co"

## Configuración principal

Archivo:

src/main/resources/application.yaml

Ejemplo de configuración:

server:
port: 8081

spring:
application:
name: climalert

datasource:
url: jdbc:h2:mem:climalert-db
driver-class-name: org.h2.Driver
username: sa
password:

h2:
console:
enabled: true
path: /h2-console

jpa:
hibernate:
ddl-auto: update
show-sql: true

climalert:
scheduler:
obtener-clima: "0 */5 * * * *"
analizar-alertas: "0 * * * * *"

weather:
base-url: https://api.weatherapi.com/v1
api-key: ${WEATHER_API_KEY:}
location: "-34.6037,-58.3816"

mailtrap:
base-url: https://send.api.mailtrap.io
token: ${MAILTRAP_TOKEN:}
from-email: ${MAILTRAP_FROM_EMAIL:hello@demomailtrap.co}
from-name: Climalert
category: Climalert Alert
to-emails: ${MAILTRAP_TO_EMAILS:admin@clima.com,emergencias@clima.com,meteorologia@clima.com}

## Ejecución del proyecto

### 1. Clonar el repositorio

git clone <URL_DEL_REPOSITORIO>
cd Climalert-ddsi

### 2. Configurar variables de entorno

En PowerShell:

$env:WEATHER_API_KEY="TU_API_KEY_DE_WEATHERAPI"
$env:MAILTRAP_TOKEN="TU_TOKEN_DE_MAILTRAP"
$env:MAILTRAP_TO_EMAILS="tu-email@dominio.com"

### 3. Ejecutar la aplicación

.\mvnw.cmd spring-boot:run

La aplicación quedará ejecutándose y las tareas programadas comenzarán a correr automáticamente.

## Consola H2

La base utilizada es H2 en memoria.

Mientras la aplicación esté corriendo, se puede acceder a:

http://localhost:8081/h2-console

Datos de conexión:

JDBC URL: jdbc:h2:mem:climalert-db
User Name: sa
Password:

La contraseña se deja vacía.

Consultas útiles:

SELECT * FROM MEDICION_CLIMA;

SELECT * FROM ALERTA_CLIMATICA;

## Estructura de carpetas

src/
└── main/
├── java/
│   └── ar/
│       └── edu/
│           └── utn/
│               └── dds/
│                   └── climalert/
│                       ├── ClimalertApplication.java
│                       ├── config/
│                       │   └── MailtrapRestClientConfig.java
│                       ├── domain/
│                       │   ├── AlertaClimatica.java
│                       │   └── MedicionClima.java
│                       ├── integration/
│                       │   ├── WeatherApiClient.java
│                       │   └── dto/
│                       │       └── WeatherApiResponse.java
│                       ├── repository/
│                       │   ├── AlertaClimaticaRepository.java
│                       │   └── MedicionClimaRepository.java
│                       ├── scheduler/
│                       │   └── ClimaScheduler.java
│                       └── service/
│                           ├── ClimaService.java
│                           └── NotificadorEmailService.java
└── resources/
└── application.yaml

## Descripción de componentes

### ClimalertApplication

Clase principal de la aplicación.

Habilita Spring Boot y el uso de tareas programadas mediante @EnableScheduling.

### ClimaScheduler

Contiene las tareas programadas.

Responsabilidades:

- Ejecutar periódicamente la obtención del clima.
- Ejecutar periódicamente el análisis de alertas.

No contiene lógica de negocio; delega en ClimaService.

### ClimaService

Contiene la lógica principal del sistema.

Responsabilidades:

- Obtener clima actual desde WeatherApiClient.
- Guardar mediciones climáticas.
- Analizar si una medición es crítica.
- Generar alertas.
- Solicitar el envío de notificaciones por mail.

### WeatherApiClient

Cliente REST encargado de consumir WeatherAPI.

Responsabilidades:

- Llamar al endpoint /current.json.
- Enviar API Key y ubicación configurada.
- Transformar la respuesta JSON en un DTO Java.

### WeatherApiResponse

DTO que representa la respuesta recibida desde WeatherAPI.

Incluye información de:

- Ubicación.
- Temperatura.
- Humedad.
- Condición climática.
- Viento.
- Presión.
- Fecha de actualización del proveedor.

### MedicionClima

Entidad JPA que representa una medición climática guardada localmente.

Campos principales:

- Ubicación.
- Temperatura.
- Humedad.
- Condición.
- Viento.
- Presión.
- Fecha de medición del proveedor.
- Fecha de registro local.

También contiene el método esCritica(), que determina si la medición representa una alerta climática.

### AlertaClimatica

Entidad JPA que representa una alerta generada a partir de una medición crítica.

Cada alerta se asocia a una única medición.

### MedicionClimaRepository

Repositorio JPA para persistir y consultar mediciones climáticas.

Incluye un método para obtener la última medición registrada.

### AlertaClimaticaRepository

Repositorio JPA para persistir y consultar alertas.

Incluye un método para verificar si una medición ya tiene una alerta generada, evitando duplicados.

### NotificadorEmailService

Servicio encargado de enviar correos mediante Mailtrap API.

Responsabilidades:

- Armar el cuerpo del mail.
- Configurar destinatarios.
- Llamar a Mailtrap mediante REST.
- Registrar errores de envío si ocurren.

### MailtrapRestClientConfig

Configuración del cliente REST utilizado para comunicarse con Mailtrap.

## Criterio de alerta

Una medición se considera crítica cuando:

temperaturaC > 35 && humedad > 60

Si una medición cumple la condición y todavía no tiene una alerta asociada, el sistema:

1. Crea una entidad AlertaClimatica.
2. Guarda la alerta en la base de datos.
3. Envía un correo con el detalle completo del clima.

## Notas sobre credenciales

Las credenciales no deben subirse al repositorio.

Por eso, el proyecto utiliza variables de entorno para:

- API Key de WeatherAPI.
- Token de Mailtrap.
- Destinatarios de prueba.
- Remitente de Mailtrap.

## Estado del proyecto

El proyecto implementa:

- Consulta periódica de clima actual.
- Integración REST con WeatherAPI.
- Persistencia local de mediciones climáticas.
- Análisis periódico de alertas.
- Generación y persistencia de alertas.
- Envío de notificaciones mediante Mailtrap API.
- Consola H2 para inspeccionar los datos generados.

## Nota final

Antes de entregar, verificar que los cron estén configurados con los tiempos reales del TP:

obtener-clima: "0 */5 * * * *"
analizar-alertas: "0 * * * * *"
