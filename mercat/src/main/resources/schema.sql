-- =========================================================================
-- MERCAT DATABASE DDL SCHEMA (PostgreSQL 15+ / Supabase)
-- =========================================================================

-- 1. LIMPIEZA PREVIA (En orden inverso de dependencias de clave foránea)
DROP TABLE IF EXISTS purchase_items CASCADE;
DROP TABLE IF EXISTS purchases CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 2. TABLA: USERS (Usuarios y Roles de Spring Security)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. TABLA: PRODUCTS (Catálogo Híbrido: Maestro Global vs. Personal)
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    barcode VARCHAR(13) NOT NULL,
    name VARCHAR(150) NOT NULL,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE
);

-- ÍNDICES CONDICIONALES CLAVE PARA EL MODELO HÍBRIDO:
CREATE UNIQUE INDEX idx_products_barcode_global 
ON products (barcode) 
WHERE user_id IS NULL;

CREATE UNIQUE INDEX idx_products_barcode_user 
ON products (barcode, user_id) 
WHERE user_id IS NOT NULL;

CREATE INDEX idx_products_lookup 
ON products (barcode, user_id DESC NULLS LAST);

CREATE INDEX idx_products_search 
ON products (user_id, LOWER(name));

-- 4. TABLA: PURCHASES (Cabeceras de Compra Atómica)
CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    purchased_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total NUMERIC(10, 2) NOT NULL CHECK (total >= 0),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_purchases_user_date 
ON purchases (user_id, purchased_at DESC);

-- 5. TABLA: PURCHASE_ITEMS (Líneas de Compra con Precios Históricos Inmutables)
CREATE TABLE purchase_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_id BIGINT NOT NULL REFERENCES purchases(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    product_name VARCHAR(150) NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    subtotal NUMERIC(10, 2) NOT NULL CHECK (subtotal >= 0)
);

CREATE INDEX idx_purchase_items_purchase 
ON purchase_items (purchase_id);

-- =========================================================================
-- DATOS SEMILLA (SEED DATA)
-- =========================================================================

INSERT INTO users (email, password, role) VALUES 
('test@mercat.com', '$2a$10$wEkgz6m9QO8q16hMhB9vEeeXkZ6b6T5j79h3oT4LqfR3aN6zG1yK.', 'ROLE_ADMIN');

INSERT INTO products (barcode, name, price, user_id) VALUES
('8480000861900', 'Pistacho tostado 0% sal', 3.40, NULL),
('8480000340245', 'Nuez natural 200g', 2.50, NULL),
('8480000349972', 'Nuez pecana 200g', 3.90, NULL),
('8480000510501', 'Mozarela fresca 🧀', 0.90, NULL),
('8436547770137', 'Kefir 500g 🥛', 1.10, NULL),
('8480000223135', 'Yogur natural x6 🥛', 1.00, NULL),
('8436581960624', 'Jamoncitos 🐔', 3.55, NULL),
('8421384152932', 'Entrecot de cerdo 🐷', 5.94, NULL),
('8402001038990', 'Pan espelta 100% integral 🍞', 1.65, NULL),
('8480000040404', 'Aceite girasol 1L 🌻', 1.85, NULL);
