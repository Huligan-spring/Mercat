# 🛒 Mercat

**Asistente de compras web (PWA) para registro de artículos por código de barras y cálculo de totales en tiempo real.**

Mercat es una solución desacoplada compuesta por un cliente **PWA (Progressive Web App)** servido a través de **GitHub Pages**, una **API REST en Spring Boot (Java 21)** desplegada en **Render**, y una base de datos **PostgreSQL gestionada en la nube con Supabase**.

---

## 📚 Tabla de contenidos

* [Arquitectura general](#-arquitectura-general)
* [Objetivos](#-objetivos)
* [Funcionalidades del MVP](#-funcionalidades-del-mvp)
* [Stack tecnológico](#-stack-tecnológico)
* [Estructura del proyecto](#-estructura-del-proyecto)
* [Modelo de datos (Supabase)](#-modelo-de-datos-supabase)
* [API REST y CORS](#-api-rest-y-cors)
* [Configuración y despliegue](#-configuración-y-despliegue)
  * [1. Base de datos (Supabase)](#1-base-de-datos-supabase)
  * [2. Backend API (Render)](#2-backend-api-render)
  * [3. Frontend PWA (GitHub Pages)](#3-frontend-pwa-github-pages)
* [Desarrollo en local](#-desarrollo-en-local)
* [Seguridad](#-seguridad)
* [Evolución futura](#-evolución-futura)

---

## 🌐 Arquitectura general

El sistema opera bajo un esquema de tres capas completamente desacopladas y conectadas vía HTTPS:

```text
┌───────────────────────────────┐
│       Dispositivo Móvil       │
│  (PWA instalada en pantalla)  │
└───────────────┬───────────────┘
                │
                ▼ (HTTPS / PWA Hosting)
┌───────────────────────────────┐
│         GitHub Pages          │
│   Frontend estático (SPA)     │
│  HTML5 + CSS3 + JS + SW       │
└───────────────┬───────────────┘
                │
                ▼ (Llamadas REST Fetch con CORS)
┌───────────────────────────────┐
│         Render (PaaS)         │
│   Backend Spring Boot 3 /     │
│           Java 21             │
└───────────────┬───────────────┘
                │
                ▼ (Conexión JDBC Segura SSL)
┌───────────────────────────────┐
│       Supabase Cloud          │
│    PostgreSQL Relacional      │
└───────────────────────────────┘
```

---

## 🎯 Objetivos

* Implementar una arquitectura desacoplada frontend/backend sin dependencias de infraestructura local compleja.
* Alojar un frontend ligero con capacidades PWA (instalable en móviles, con Service Worker y soporte offline) bajo **HTTPS gratuito en GitHub Pages**.
* Exponer una API REST estructurada en capas (**Controller, Service, Repository, Model**) con **Spring Boot** desplegada en **Render**.
* Persistir datos transaccionales en un motor **PostgreSQL en la nube (Supabase)**.
* Gestionar la comunicación entre orígenes cruzados (**CORS**) entre GitHub Pages y Render.

---

## ✨ Funcionalidades del MVP

* 🔎 **Búsqueda por código de barras:** Entrada manual o mediante lector/teclado conectada contra la API.
* 🧺 **Cesta de compra en memoria cliente:** Añadir artículos, alterar unidades y suprimir líneas.
* 🧮 **Cálculo de importes:** Suma precisa de subtotales y cálculo global respaldado por `BigDecimal` en el backend.
* 💾 **Finalización de compra:** Envío de la transacción a la base de datos para registrar la venta y el histórico de artículos.
* 📱 **Instalabilidad PWA:** Soporte para añadir como aplicación nativa en dispositivos móviles Android e iOS.

---

## 🧰 Stack tecnológico

| Módulo | Tecnología | Plataforma / Hosting |
| :--- | :--- | :--- |
| **Frontend PWA** | HTML5, CSS3, JavaScript (ES6+), Web App Manifest, Service Worker | **GitHub Pages** |
| **Backend API** | Java 21 LTS, Spring Boot, Maven, Spring Data JPA / Hibernate | **Render** |
| **Base de Datos** | PostgreSQL 15+ (con soporte SSL) | **Supabase** |
| **CI / CD** | GitHub Actions / GitHub Pages Deployment | **GitHub** |

---

## 🏗️ Estructura del proyecto

El repositorio organiza el frontend y el backend en carpetas independientes para permitir un mantenimiento y despliegue modular:

```text
mercat/
├── backend/                         # Backend Spring Boot (API REST)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/mercat/
│       │   │   ├── MercatApiApplication.java
│       │   │   ├── config/
│       │   │   │   └── CorsConfig.java          # Configuración de CORS para GitHub Pages
│       │   │   ├── controller/
│       │   │   │   ├── CartController.java
│       │   │   │   └── ProductController.java
│       │   │   ├── model/
│       │   │   │   ├── Product.java
│       │   │   │   ├── Purchase.java
│       │   │   │   └── PurchaseItem.java
│       │   │   ├── repository/
│       │   │   │   ├── ProductRepository.java
│       │   │   │   └── PurchaseRepository.java
│       │   │   └── service/
│       │   │       ├── CartService.java
│       │   │       ├── ProductService.java
│       │   │       └── PurchaseService.java
│       │   └── resources/
│       │       ├── application.properties       # Plantilla de variables de entorno
│       │       └── application-prod.properties  # Perfil de producción en Render
│       └── test/
│           └── java/com/mercat/
│
├── frontend/                        # Frontend PWA (Desplegado a GitHub Pages)
│   ├── index.html                   # Interfaz única de usuario (SPA)
│   ├── manifest.json                # Manifiesto de instalación PWA
│   ├── sw.js                        # Service Worker para caché offline
│   ├── css/
│   │   └── styles.css
│   ├── js/
│   │   ├── config.js                # URL base de la API (Render vs Localhost)
│   │   ├── api.js                   # Módulo de peticiones fetch
│   │   └── app.js                   # Lógica de renderizado y eventos
│   └── icons/
│       ├── icon-192.png
│       └── icon-512.png
│
├── .gitignore
└── README.md
```

---

## 🗃️ Modelo de datos (Supabase)

Tablas relacionales creadas en el panel SQL de Supabase:

### 1. `products` (Catálogo maestro)
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    barcode VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0)
);
```

### 2. `purchases` (Cabeceras de venta)
```sql
CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    purchased_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    total NUMERIC(10,2) NOT NULL
);
```

### 3. `purchase_items` (Líneas de detalle)
```sql
CREATE TABLE purchase_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_id BIGINT NOT NULL REFERENCES purchases(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    product_name VARCHAR(150) NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    subtotal NUMERIC(10,2) NOT NULL
);
```

---

## 🔌 API REST y CORS

Dado que el frontend se sirve desde un dominio de GitHub y la API desde Render, el backend expone los endpoints con **CORS explícito** (`@CrossOrigin` o vía `WebMvcConfigurer`):

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/products/{barcode}` | Consulta de un artículo por su código |
| `POST` | `/api/purchases/checkout` | Confirma la cesta y guarda la compra en Supabase |

### Configuración CORS en Spring Boot (`CorsConfig.java`):
Permite peticiones desde la URL del frontend:
* Entorno producción: `https://<tu-usuario>.github.io`
* Entorno local: `http://localhost:5500` / `http://127.0.0.1:5500`

---

## 🚀 Configuración y despliegue

### 1. Base de datos (Supabase)
1. Crear un proyecto nuevo en [Supabase](https://supabase.com).
2. En **Project Settings > Database**, copiar los datos de conexión (**Connection String - JDBC** o URI).
3. Asegurarse de activar la conexión con SSL (`sslmode=require`).

### 2. Backend API (Render)
1. Iniciar sesión en [Render](https://render.com) y crear un **Web Service**.
2. Conectar el repositorio de GitHub y fijar el subdirectorio de compilación en `backend`.
3. Configurar los comandos:
   * **Build Command:** `mvn clean package -DskipTests`
   * **Start Command:** `java -jar target/mercat-api-1.0.0.jar`
4. Añadir las variables de entorno en Render:
   * `SPRING_PROFILES_ACTIVE`: `prod`
   * `DB_URL`: `jdbc:postgresql://db.xxxx.supabase.co:5432/postgres?sslmode=require`
   * `DB_USER`: `postgres`
   * `DB_PASSWORD`: `<tu_contraseña_supabase>`
   * `CORS_ALLOWED_ORIGINS`: `https://<tu-usuario>.github.io`

> **Nota sobre el plan gratuito de Render:** Las instancias entran en modo reposo tras 15 minutos de inactividad; la primera petición tras un periodo de inactividad puede tardar aproximadamente 50 segundos en despertar.

### 3. Frontend PWA (GitHub Pages)
1. En `frontend/js/config.js`, definir la URL pública generada por Render:
   ```javascript
   export const API_BASE_URL = "https://mercat-api.onrender.com/api";
   ```
2. En el repositorio de GitHub, ir a **Settings > Pages**.
3. Seleccionar la rama `main` y la carpeta `/frontend` (o desplegar mediante una GitHub Action orientada al directorio `/frontend`).
4. Al completarse, la PWA estará disponible en `https://<tu-usuario>.github.io/mercat/` con certificado HTTPS listo para su instalación en teléfonos móviles.

---

## 💻 Desarrollo en local

Para ejecutar y probar cambios en tu propio equipo:

1. **Backend:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   La API quedará escuchando en `http://localhost:8080`.

2. **Frontend:**
   Abre la carpeta `frontend/` mediante una extensión de servidor estático (como *Live Server* en VS Code o similar en IntelliJ) o usando Python:
   ```bash
   cd frontend
   python3 -m http.server 5500
   ```
   Abre `http://localhost:5500` en el navegador.

---

## 🔐 Seguridad

* Ninguna contraseña de Supabase ni clave de base de datos se guarda en archivos versionados.
* El archivo `.env` está expresamente excluido en `.gitignore`.
* Las credenciales de producción son suministradas exclusivamente a través de los paneles de variables de entorno de Render.

---

## 🔮 Evolución futura

* Contenedorización del backend mediante `Dockerfile` y `docker-compose` para pruebas locales sin conexión.
* Creación de manifiestos de Kubernetes (`Deployment`, `Service`, `Ingress`) en un repositorio paralelo para el módulo de orquestación.
* Escaneo de códigos de barra mediante la cámara del móvil usando la API nativa `BarcodeDetector`.
