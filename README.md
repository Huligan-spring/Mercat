# 🛒 Mercat

**Un asistente de compras sencillo para añadir productos por código de barras y calcular el importe total de una compra.**

Mercat es un MVP de aplicación web desarrollado con Java y Spring Boot. Permite consultar productos por su código de barras, añadirlos a una cesta temporal, calcular subtotales y el total, y guardar las compras finalizadas en PostgreSQL.

El proyecto está planteado como un recorrido de aprendizaje incremental: cada etapa incorpora una tecnología y termina con un resultado comprobable.

> **Estado del proyecto:** propuesta de MVP en desarrollo. La estructura y las funcionalidades descritas representan el objetivo previsto; se incorporarán progresivamente.

## 📚 Tabla de contenidos

* [Objetivos](#-objetivos)
* [Funcionalidades](#-funcionalidades)
* [Tecnologías](#-tecnologías)
* [Arquitectura y estructura](#-arquitectura-y-estructura)
* [Modelo de datos](#-modelo-de-datos)
* [Plan de desarrollo](#-plan-de-desarrollo)
* [Configuración y ejecución](#-configuración-y-ejecución)
* [API REST](#-api-rest)
* [Pruebas](#-pruebas)
* [Docker y Kubernetes](#-docker-y-kubernetes)
* [Seguridad y configuración](#-seguridad-y-configuración)
* [Criterios de finalización](#-criterios-de-finalización)
* [Alcance futuro](#-alcance-futuro)

## 🎯 Objetivos

* Construir una aplicación web funcional y sencilla.
* Aprender a organizar un proyecto Java con Maven y Spring Boot.
* Persistir información con JPA/Hibernate y PostgreSQL.
* Crear una interfaz web con Thymeleaf y una API REST básica.
* Introducir integración continua con Jenkins.
* Empaquetar la aplicación mediante Docker.
* Realizar un despliegue local básico en Kubernetes.
* Mantener un repositorio documentado y reproducible en GitHub.

## ✨ Funcionalidades

### Incluidas en el MVP

* 🔎 Buscar productos mediante su código de barras.
* 🧺 Añadir productos a una cesta temporal.
* ➕ Consultar nombre, precio unitario, cantidad y subtotal de cada línea.
* 🧮 Calcular el importe total de la compra.
* ⚠️ Mostrar un mensaje comprensible si el producto no existe.
* 💾 Finalizar una compra y guardar sus datos en PostgreSQL.
* 🧾 Conservar el nombre y el precio del producto tal como eran en el momento de la compra.
* 🔌 Exponer operaciones básicas mediante una API REST.
* ✅ Ejecutar pruebas automatizadas y preparar la aplicación para su despliegue.

El código de barras se introducirá inicialmente en un campo de texto. También podrá utilizarse un lector físico que funcione como teclado.

### Fuera del alcance inicial

Para mantener el alcance del MVP y ajustarse al tiempo estimado, no se incluyen inicialmente:

* Registro de usuarios ni autenticación.
* Pagos electrónicos.
* Escaneo de códigos con la cámara.
* Integración con catálogos externos de supermercados.
* Descuentos, cupones o programas de fidelización.
* Gestión de inventario.
* Una interfaz independiente para consultar el historial de compras.
* Microservicios o infraestructura de producción.

## 🧰 Tecnologías

| Tecnología | Uso previsto |
| --- | --- |
| Java | Lenguaje de programación |
| Maven | Gestión de dependencias y construcción |
| Spring Boot | Base de la aplicación |
| JPA / Hibernate | Mapeo objeto-relacional y persistencia |
| PostgreSQL en Supabase | Base de datos |
| Thymeleaf | Interfaz web del servidor |
| API REST | Consulta de productos y operaciones básicas |
| Jenkins | Integración continua |
| Docker | Construcción y ejecución de contenedores |
| Kubernetes | Despliegue local básico |
| IntelliJ IDEA | Entorno de desarrollo |
| Git y GitHub | Control de versiones y alojamiento del código |

## 🏗️ Arquitectura y estructura

La aplicación se organizará por responsabilidades: productos, compras, cesta y capa web. La lógica de negocio se mantendrá separada de los controladores.

    mercat/
    ├── pom.xml
    ├── src/
    │   ├── main/
    │   │   ├── java/com/mercat/
    │   │   │   ├── MercatApplication.java
    │   │   │   ├── product/
    │   │   │   │   ├── Product.java
    │   │   │   │   ├── ProductRepository.java
    │   │   │   │   └── ProductService.java
    │   │   │   ├── purchase/
    │   │   │   │   ├── Purchase.java
    │   │   │   │   ├── PurchaseItem.java
    │   │   │   │   ├── PurchaseRepository.java
    │   │   │   │   └── PurchaseService.java
    │   │   │   ├── cart/
    │   │   │   │   └── CartService.java
    │   │   │   └── web/
    │   │   │       ├── CartController.java
    │   │   │       └── ProductApiController.java
    │   │   └── resources/
    │   │       ├── application.properties
    │   │       └── templates/
    │   │           └── cart.html
    │   └── test/
    │       └── java/com/mercat/
    ├── Jenkinsfile
    ├── Dockerfile
    ├── .dockerignore
    ├── k8s/
    │   ├── deployment.yaml
    │   └── service.yaml
    ├── .gitignore
    ├── .env.example
    └── README.md

> Esta es la estructura objetivo. Los archivos se añadirán a medida que avance cada etapa.

## 🗃️ Modelo de datos

La persistencia se organizará en tres tablas relacionadas.

### `products`

Catálogo de productos.

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `id` | `BIGINT` | Identificador único |
| `barcode` | `VARCHAR(32)` | Código de barras único |
| `name` | `VARCHAR(150)` | Nombre del producto |
| `price` | `NUMERIC(10,2)` | Precio unitario actual |

**Reglas de negocio:**

* El código de barras debe ser único.
* El precio no puede ser negativo.
* Los importes se gestionarán en Java con `BigDecimal`.

### `purchases`

Compras finalizadas.

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `id` | `BIGINT` | Identificador único de la compra |
| `purchased_at` | `TIMESTAMPTZ` | Fecha y hora de la compra |
| `total` | `NUMERIC(10,2)` | Importe total |

El total se calculará en el servidor a partir de las líneas de detalle.

### `purchase_items`

Productos incluidos en cada compra.

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `id` | `BIGINT` | Identificador de la línea |
| `purchase_id` | `BIGINT` | Referencia a la compra |
| `product_id` | `BIGINT` | Referencia al producto del catálogo |
| `product_name` | `VARCHAR(150)` | Nombre del producto en el momento de la compra |
| `unit_price` | `NUMERIC(10,2)` | Precio unitario en el momento de la compra |
| `quantity` | `INTEGER` | Cantidad de unidades |
| `subtotal` | `NUMERIC(10,2)` | Importe de la línea |

**Reglas de negocio:**

* Cada línea pertenece a una única compra y referencia un producto.
* La cantidad debe ser mayor que cero.
* El subtotal se calcula como precio unitario por cantidad.
* El nombre y el precio se guardan como una instantánea histórica, aunque el catálogo cambie.

### Relaciones y comportamiento de la cesta

* Un producto puede aparecer en muchas líneas de compra.
* Una compra puede contener varias líneas.
* La relación muchos a muchos entre compras y productos se resuelve mediante `purchase_items`.
* La cesta se mantiene temporalmente durante la sesión web y no se persiste hasta finalizar la compra.
* La compra y todas sus líneas se guardan en una única transacción para evitar registros incompletos.

## 🗺️ Plan de desarrollo

La duración estimada del proyecto es de **15 horas**. El orden de incorporación de tecnologías será:

**Maven → Spring Boot → JPA/Hibernate → Thymeleaf y API REST → Jenkins → Docker → Kubernetes**

| Etapa | Tiempo | Resultado comprobable |
| --- | --- | --- |
| 1. Maven | 1,5 h | El proyecto compila y el repositorio está publicado en GitHub |
| 2. Spring Boot | 3 h | La aplicación arranca y calcula una compra de prueba |
| 3. JPA/Hibernate | 2,5 h | Se consultan productos y se guardan compras en Supabase PostgreSQL |
| 4. Thymeleaf y API REST | 3,5 h | Se puede realizar una compra desde el navegador y consultar productos por API |
| 5. Jenkins | 1 h | El pipeline ejecuta las pruebas y falla cuando una prueba no se supera |
| 6. Docker | 2 h | La aplicación se construye y ejecuta en un contenedor |
| 7. Kubernetes | 1,5 h | La aplicación se despliega en un clúster local y se expone mediante un Service |
| **Total** | **15 h** |     |

### Hitos por etapa

<details><summary><strong>Etapa 1 — Maven</strong> · 1,5 horas</summary>

* Crear el repositorio `mercat` en GitHub.
* Crear el proyecto Maven en IntelliJ IDEA.
* Configurar un JDK compatible con la versión elegida de Spring Boot.
* Revisar las dependencias y la estructura de `pom.xml`.
* Ejecutar la compilación y las pruebas básicas.

**Finalización:** Maven resuelve las dependencias y la compilación termina sin errores.

</details>

<details><summary><strong>Etapa 2 — Spring Boot</strong> · 3 horas</summary>

* Crear la clase principal de Spring Boot.
* Crear la clase `Product`.
* Implementar un servicio de consulta de productos de prueba.
* Crear `CartService` para gestionar la cesta.
* Calcular subtotales y total usando `BigDecimal`.
* Probar los casos principales.

**Finalización:** la aplicación arranca, los servicios están separados de los controladores y los cálculos son correctos.

</details>

<details><summary><strong>Etapa 3 — JPA/Hibernate</strong> · 2,5 horas</summary>

* Crear el proyecto PostgreSQL en Supabase.
* Crear las tablas del modelo de datos.
* Configurar JPA y el controlador de PostgreSQL.
* Mapear `Product`, `Purchase` y `PurchaseItem`.
* Crear los repositorios.
* Configurar la conexión mediante variables de entorno.
* Sustituir el catálogo en memoria por consultas a PostgreSQL.

**Finalización:** se consultan productos por código de barras y se guardan compras con sus líneas mediante una transacción.

</details>

<details><summary><strong>Etapa 4 — Thymeleaf y API REST</strong> · 3,5 horas</summary>

* Crear una página para introducir el código de barras.
* Mostrar los productos, cantidades, subtotales y total de la cesta.
* Añadir una acción para quitar productos.
* Implementar la finalización de la compra.
* Crear endpoints para consultar productos y añadir productos a la cesta.
* Probar los casos de éxito y error.

**Finalización:** la compra se puede completar desde el navegador y la API devuelve JSON con códigos HTTP apropiados.

</details>

<details><summary><strong>Etapa 5 — Jenkins</strong> · 1 hora</summary>

* Crear un `Jenkinsfile`.
* Configurar etapas de obtención del código, compilación y pruebas.
* Ejecutar el pipeline.
* Verificar que una prueba fallida genera un resultado fallido.

**Nota:** para ajustarse al tiempo disponible, se utilizará un entorno Jenkins ya preparado si está disponible.

</details>

<details><summary><strong>Etapa 6 — Docker</strong> · 2 horas</summary>

* Crear un `Dockerfile`.
* Construir el JAR con Maven.
* Crear la imagen Docker.
* Configurar las variables de entorno necesarias.
* Ejecutar el contenedor y comprobar la conexión con Supabase.

**Finalización:** la imagen se construye correctamente, el contenedor arranca y las credenciales no quedan incluidas en la imagen.

</details>

<details><summary><strong>Etapa 7 — Kubernetes</strong> · 1,5 horas</summary>

* Utilizar un clúster local previamente preparado.
* Crear un `Deployment` con una réplica y un `Service`.
* Desplegar la imagen de Mercat.
* Comprobar el estado del Pod y consultar sus logs.

**Finalización:** la aplicación se ejecuta en Kubernetes y es accesible mediante el Service.

</details>

El calendario es orientativo y ajustado para una persona que está aprendiendo estas tecnologías. Se priorizará completar el flujo de compra y realizar demostraciones mínimas de automatización y despliegue.

## 🚀 Configuración y ejecución

> **Nota:** esta propuesta describe la configuración prevista, pero no presupone que todos los archivos o pasos estén implementados todavía. Las instrucciones definitivas se completarán a medida que avance el desarrollo.

### Requisitos previstos

* JDK compatible con la versión de Spring Boot elegida.
* Maven.
* Git.
* IntelliJ IDEA (recomendado para el desarrollo).
* Un proyecto PostgreSQL en Supabase para la etapa de persistencia.

Clona el repositorio:

    git clone <URL_DEL_REPOSITORIO>
    cd mercat

Cuando el proyecto Maven esté configurado, podrás compilarlo y ejecutar las pruebas con:

    mvn test
    mvn package

El modo concreto de arrancar la aplicación y las variables necesarias se documentarán cuando estén definidas la configuración de Spring Boot y la conexión a la base de datos.

## 🔌 API REST

Los endpoints mínimos propuestos para el MVP son:

| Método | Endpoint | Función |
| --- | --- | --- |
| `GET` | `/api/products/{barcode}` | Consultar un producto por su código de barras |
| `POST` | `/api/cart/items` | Añadir un producto a la cesta |

La API devolverá respuestas JSON y códigos HTTP apropiados para los casos de éxito y error. Los contratos de petición y respuesta se documentarán cuando se implementen los controladores.

## 🧪 Pruebas

Se probarán, como mínimo, los casos principales de la lógica de compra:

* Consulta de un producto existente y gestión de un código inexistente.
* Cálculo correcto de subtotales y total.
* Validación de cantidades y precios.
* Persistencia de una compra y todas sus líneas.
* Respuestas de éxito y error de la API.
* Ejecución de pruebas automatizadas en el pipeline de Jenkins.

## 🐳 Docker y ☸️ Kubernetes

Como parte de las etapas finales se prepararán:

* `Dockerfile` y `.dockerignore` para construir y ejecutar la aplicación en un contenedor.
* `k8s/deployment.yaml` para desplegar una réplica de Mercat.
* `k8s/service.yaml` para permitir el acceso a la aplicación desde el entorno local.
* Instrucciones para configurar variables de entorno, iniciar el contenedor y comprobar el estado del despliegue.

El despliegue previsto es local y básico; no representa una infraestructura de producción.

## 🔐 Seguridad y configuración

* No subir contraseñas, tokens ni credenciales.
* No versionar archivos `.env` reales.
* Mantener `.env.example` con los nombres de las variables necesarias, sin valores secretos.
* No incluir credenciales dentro de la imagen Docker.
* Subir únicamente configuraciones no sensibles.

## ✅ Criterios de finalización

El MVP se considerará terminado cuando sea posible:

* [ ] Consultar un producto por su código de barras.
* [ ] Añadir productos a la cesta y calcular correctamente el total.
* [ ] Finalizar una compra y guardar su cabecera y líneas en PostgreSQL.
* [ ] Consultar productos mediante la API REST.
* [ ] Ejecutar las pruebas automatizadas.
* [ ] Arrancar la aplicación en Docker.
* [ ] Desplegar la aplicación en Kubernetes o, si el entorno local no está disponible, dejar los manifiestos preparados y documentados.
* [ ] Documentar la instalación, configuración, pruebas y despliegue en GitHub.

## 🌱 Organización del repositorio y contribuciones

El proyecto se desarrollará en un único repositorio llamado `mercat`. Se recomienda realizar commits pequeños y descriptivos que reflejen los avances de cada etapa.

El repositorio final incluirá código Java y pruebas, configuración Maven, plantillas Thymeleaf, controladores REST, `Jenkinsfile`, archivos Docker, manifiestos de Kubernetes y documentación de configuración y ejecución.

## 🔮 Posibles mejoras futuras

Las siguientes funcionalidades quedan fuera del MVP inicial y podrían valorarse en futuras iteraciones:

* Registro y autenticación de usuarios.
* Escaneo de códigos de barras mediante cámara.
* Integración con catálogos externos.
* Descuentos y programas de fidelización.
* Gestión de inventario.
* Interfaz para consultar el historial de compras.

## 📝 Conclusión

Mercat es un proyecto práctico para aplicar de forma progresiva tecnologías del ecosistema Java sobre un caso de uso concreto. Su alcance reducido permite centrarse en el flujo esencial de compra, la persistencia de datos, una interfaz web, una API REST y una primera experiencia con integración continua y despliegue.

El objetivo principal es comprender cada etapa, documentar las decisiones y mantener una aplicación que pueda revisarse y ejecutarse desde GitHub.
