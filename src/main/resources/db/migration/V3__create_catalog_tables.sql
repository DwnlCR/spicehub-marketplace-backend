CREATE TABLE categories(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uk_categories_name_lower ON categories (LOWER(name));

CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    category_id UUID NOT NULL,
    image_key VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),

    CONSTRAINT chk_products_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);


CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    measurement_unit VARCHAR(20) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    availability VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_product_variants_product FOREIGN KEY (product_id) REFERENCES products(id),

    CONSTRAINT chk_product_variants_quantity CHECK (quantity > 0),

    CONSTRAINT chk_product_variants_price CHECK (price > 0),

    CONSTRAINT chk_product_variants_measurement_unit CHECK (measurement_unit IN ('GRAM', 'KILOGRAM', 'UNIT')),

    CONSTRAINT chk_product_variants_availability CHECK (availability IN ('AVAILABLE', 'SOLD_OUT')),

    CONSTRAINT uk_product_variants_measurement UNIQUE (product_id, quantity, measurement_unit)
);

CREATE INDEX idx_products_category_id ON products(category_id);

CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
