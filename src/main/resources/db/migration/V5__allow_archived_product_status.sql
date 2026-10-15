ALTER TABLE products DROP CONSTRAINT chk_products_status;

ALTER TABLE products ADD CONSTRAINT chk_products_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'));