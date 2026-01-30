IF OBJECT_ID('dbo.system_settings', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.system_settings (
        setting_key   NVARCHAR(50) NOT NULL PRIMARY KEY,
        setting_value NVARCHAR(100) NOT NULL,
        updated_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.system_settings WHERE setting_key = 'DEFAULT_CURRENCY')
BEGIN
    INSERT INTO dbo.system_settings(setting_key, setting_value) VALUES ('DEFAULT_CURRENCY', 'VND');
END
GO

ALTER TABLE dbo.user_logins
ADD updated_at DATETIME2 NOT NULL
    CONSTRAINT DF_user_logins_updated_at DEFAULT SYSDATETIME();
GO

ALTER TABLE user_logins
ALTER COLUMN updated_at DATETIME2 NULL;
GO

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

ALTER TABLE dbo.customers
ADD last_login_at DATETIME2 NULL;
GO

CREATE INDEX IX_customers_last_login ON dbo.customers(last_login_at DESC);
GO