CREATE TABLE cart_items (
    id INT IDENTITY(1,1) PRIMARY KEY,

    customer_id INT NOT NULL,
    product_variant_id INT NOT NULL,

    quantity INT NOT NULL CHECK (quantity > 0),

    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT uq_cart_customer_variant
        UNIQUE (customer_id, product_variant_id)
);

ALTER TABLE cart_items
ADD CONSTRAINT fk_cart_items_customer
FOREIGN KEY (customer_id)
REFERENCES customers(id)
ON DELETE CASCADE;

ALTER TABLE cart_items
ADD CONSTRAINT fk_cart_items_product_variant
FOREIGN KEY (product_variant_id)
REFERENCES product_variants(id);

CREATE INDEX idx_cart_items_customer
ON cart_items(customer_id);

CREATE INDEX idx_cart_items_variant
ON cart_items(product_variant_id);

