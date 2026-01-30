SET NOCOUNT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

CREATE DATABASE retail_management_v5;
GO
USE retail_management_v5;
GO

---------------------------------------------------------------
-- SEQUENCES
---------------------------------------------------------------
CREATE SEQUENCE dbo.order_seq
    AS BIGINT
    START WITH 1000
    INCREMENT BY 1;
GO

---------------------------------------------------------------
-- 1) USERS / AUTH / AUDIT
---------------------------------------------------------------
CREATE TABLE dbo.users (
    id            INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_users PRIMARY KEY,
    username      NVARCHAR(50)  NOT NULL,
    email         NVARCHAR(100) NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    role          NVARCHAR(30)  NOT NULL CONSTRAINT DF_users_role DEFAULT N'CUSTOMER',
    is_active     BIT NOT NULL CONSTRAINT DF_users_is_active DEFAULT 1,
    created_at    DATETIME2 NOT NULL CONSTRAINT DF_users_created_at DEFAULT SYSDATETIME(),
    updated_at    DATETIME2 NOT NULL CONSTRAINT DF_users_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_users_username UNIQUE (username),
    CONSTRAINT UQ_users_email UNIQUE (email),
    CONSTRAINT CK_users_role CHECK (role IN (N'ADMIN', N'SALES', N'INVENTORY', N'CUSTOMER'))
);
GO

CREATE TABLE dbo.user_logins (
    id         BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_user_logins PRIMARY KEY,
    user_id    INT NULL,
    username   NVARCHAR(50) NULL,
    success    BIT NOT NULL,
    ip_address NVARCHAR(45) NULL,
    user_agent NVARCHAR(400) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_user_logins_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NULL CONSTRAINT DF_user_logins_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT FK_user_logins_user FOREIGN KEY (user_id)
        REFERENCES dbo.users(id) ON DELETE SET NULL
);
GO
CREATE INDEX IX_user_logins_user_date ON dbo.user_logins(user_id, created_at DESC);
CREATE INDEX IX_user_logins_success_date ON dbo.user_logins(success, created_at DESC);
GO

CREATE TABLE dbo.password_resets (
    id           BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_password_resets PRIMARY KEY,
    user_id      INT NOT NULL,
    token_hash   NVARCHAR(255) NOT NULL,
    expires_at   DATETIME2 NOT NULL,
    used_at      DATETIME2 NULL,
    requested_ip NVARCHAR(45) NULL,
    created_at   DATETIME2 NOT NULL CONSTRAINT DF_password_resets_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT FK_password_resets_user FOREIGN KEY (user_id)
        REFERENCES dbo.users(id) ON DELETE CASCADE
);
GO
CREATE INDEX IX_password_resets_user_date ON dbo.password_resets(user_id, created_at DESC);
GO

CREATE TABLE dbo.audit_logs (
    id           BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_audit_logs PRIMARY KEY,
    user_id      INT NULL,
    module       NVARCHAR(50) NOT NULL,
    action       NVARCHAR(50) NOT NULL,
    target_type  NVARCHAR(50) NULL,
    target_id    BIGINT NULL,
    details_json NVARCHAR(MAX) NULL,
    ip_address   NVARCHAR(45) NULL,
    created_at   DATETIME2 NOT NULL CONSTRAINT DF_audit_logs_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT FK_audit_user FOREIGN KEY (user_id)
        REFERENCES dbo.users(id) ON DELETE SET NULL
);
GO
CREATE INDEX IX_audit_user_date   ON dbo.audit_logs(user_id, created_at DESC);
CREATE INDEX IX_audit_module_date ON dbo.audit_logs(module, created_at DESC);
CREATE INDEX IX_audit_target      ON dbo.audit_logs(target_type, target_id, created_at DESC);
GO

---------------------------------------------------------------
-- 2) CUSTOMERS + LOYALTY
---------------------------------------------------------------
CREATE TABLE dbo.customers (
    id             INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_customers PRIMARY KEY,
    user_id         INT NULL,
    name           NVARCHAR(150) NOT NULL,
    email          NVARCHAR(100) NULL,
    phone          NVARCHAR(30)  NULL,
    date_of_birth  DATE NULL,
    customer_type  NVARCHAR(20) NOT NULL CONSTRAINT DF_customers_customer_type DEFAULT N'REGULAR',
    vip_tier       NVARCHAR(30) NULL,
    segments_json  NVARCHAR(MAX) NULL,
    loyalty_points INT NOT NULL CONSTRAINT DF_customers_loyalty_points DEFAULT 0,
    total_spent    DECIMAL(15,2) NOT NULL CONSTRAINT DF_customers_total_spent DEFAULT 0,
    last_order_at  DATETIME2 NULL,
    last_login_at  DATETIME2 NULL,
    address        NVARCHAR(500) NULL,
    notes          NVARCHAR(MAX) NULL,
    is_active      BIT NOT NULL CONSTRAINT DF_customers_is_active DEFAULT 1,
    created_at     DATETIME2 NOT NULL CONSTRAINT DF_customers_created_at DEFAULT SYSDATETIME(),
    updated_at     DATETIME2 NOT NULL CONSTRAINT DF_customers_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_customers_user UNIQUE (user_id),
    CONSTRAINT UQ_customers_email UNIQUE (email),
    CONSTRAINT FK_customers_user FOREIGN KEY (user_id)
        REFERENCES dbo.users(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_customers_type ON dbo.customers(customer_type);
CREATE INDEX IX_customers_points ON dbo.customers(loyalty_points DESC);
CREATE INDEX IX_customers_spent ON dbo.customers(total_spent DESC);
CREATE INDEX IX_customers_last_order ON dbo.customers(last_order_at DESC);
CREATE INDEX IX_customers_last_login ON dbo.customers(last_login_at DESC);
GO

CREATE TABLE dbo.loyalty_ledger (
    id             BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_loyalty_ledger PRIMARY KEY,
    customer_id    INT NOT NULL,
    points_delta   INT NOT NULL,
    reason         NVARCHAR(200) NULL,
    reference_type NVARCHAR(50) NULL,
    reference_id   BIGINT NULL,
    note           NVARCHAR(500) NULL,
    created_by     INT NULL,
    created_at     DATETIME2 NOT NULL CONSTRAINT DF_loyalty_ledger_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT FK_loyalty_customer FOREIGN KEY (customer_id)
        REFERENCES dbo.customers(id) ON DELETE CASCADE,
    CONSTRAINT FK_loyalty_user FOREIGN KEY (created_by)
        REFERENCES dbo.users(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_loyalty_customer_date ON dbo.loyalty_ledger(customer_id, created_at DESC);
GO

---------------------------------------------------------------
-- 3) CATEGORIES & TAGS
---------------------------------------------------------------
CREATE TABLE dbo.categories (
    id            INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_categories PRIMARY KEY,
    name          NVARCHAR(150) NOT NULL,
    description   NVARCHAR(500) NULL,
    image_url     NVARCHAR(500) NULL,
    parent_id     INT NULL,
    display_order INT NOT NULL CONSTRAINT DF_categories_display_order DEFAULT 0,
    is_active     BIT NOT NULL CONSTRAINT DF_categories_is_active DEFAULT 1,
    created_at    DATETIME2 NOT NULL CONSTRAINT DF_categories_created_at DEFAULT SYSDATETIME(),
    updated_at    DATETIME2 NOT NULL CONSTRAINT DF_categories_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT FK_categories_parent FOREIGN KEY (parent_id)
        REFERENCES dbo.categories(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_categories_parent ON dbo.categories(parent_id);
GO

CREATE TABLE dbo.tags (
    id         INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_tags PRIMARY KEY,
    name       NVARCHAR(80) NOT NULL,
    tag_type   NVARCHAR(30) NOT NULL CONSTRAINT DF_tags_tag_type DEFAULT N'GENERAL',
    is_active  BIT NOT NULL CONSTRAINT DF_tags_is_active DEFAULT 1,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_tags_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_tags_name UNIQUE (name)
);
GO

---------------------------------------------------------------
-- 4) PRODUCTS & VARIANTS
---------------------------------------------------------------
CREATE TABLE dbo.products (
    id              INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_products PRIMARY KEY,
    name            NVARCHAR(200) NOT NULL,
    sku             NVARCHAR(100) NOT NULL,
    description     NVARCHAR(MAX) NULL,
    is_visible      BIT NOT NULL CONSTRAINT DF_products_is_visible DEFAULT 1,
    is_featured     BIT NOT NULL CONSTRAINT DF_products_is_featured DEFAULT 0,
    view_count      INT NOT NULL CONSTRAINT DF_products_view_count DEFAULT 0,
    sold_count      INT NOT NULL CONSTRAINT DF_products_sold_count DEFAULT 0,
    attributes_json NVARCHAR(MAX) NULL,
    created_at      DATETIME2 NOT NULL CONSTRAINT DF_products_created_at DEFAULT SYSDATETIME(),
    updated_at      DATETIME2 NOT NULL CONSTRAINT DF_products_updated_at DEFAULT SYSDATETIME(),
    deleted_at      DATETIME2 NULL,
    CONSTRAINT UQ_products_sku UNIQUE (sku)
);
GO
CREATE INDEX IX_products_visible ON dbo.products(is_visible, created_at DESC);
CREATE INDEX IX_products_created ON dbo.products(created_at DESC);
CREATE INDEX IX_products_sold    ON dbo.products(sold_count DESC);
CREATE INDEX IX_products_name    ON dbo.products(name);
GO

CREATE TABLE dbo.product_categories (
    product_id  INT NOT NULL,
    category_id INT NOT NULL,
    is_primary  BIT NOT NULL CONSTRAINT DF_product_categories_is_primary DEFAULT 0,
    created_at  DATETIME2 NOT NULL CONSTRAINT DF_product_categories_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT PK_product_categories PRIMARY KEY (product_id, category_id),
    CONSTRAINT FK_pc_product FOREIGN KEY (product_id)
        REFERENCES dbo.products(id) ON DELETE NO ACTION,
    CONSTRAINT FK_pc_category FOREIGN KEY (category_id)
        REFERENCES dbo.categories(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_pc_category ON dbo.product_categories(category_id, product_id);
GO

CREATE TABLE dbo.product_tags (
    product_id INT NOT NULL,
    tag_id     INT NOT NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_product_tags_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT PK_product_tags PRIMARY KEY (product_id, tag_id),
    CONSTRAINT FK_pt_product FOREIGN KEY (product_id)
        REFERENCES dbo.products(id) ON DELETE NO ACTION,
    CONSTRAINT FK_pt_tag FOREIGN KEY (tag_id)
        REFERENCES dbo.tags(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_pt_tag ON dbo.product_tags(tag_id, product_id);
GO

CREATE TABLE dbo.product_variants (
    id               INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_product_variants PRIMARY KEY,
    product_id       INT NOT NULL,
    variant_name     NVARCHAR(150) NOT NULL,
    sku              NVARCHAR(100) NOT NULL,
    barcode          NVARCHAR(100) NULL,
    currency_code    CHAR(3) NOT NULL CONSTRAINT DF_product_variants_currency_code DEFAULT 'VND',
    price            DECIMAL(12,2) NOT NULL,
    cost_price       DECIMAL(12,2) NULL,
    price_tiers_json NVARCHAR(MAX) NULL,
    stock_quantity   INT NOT NULL CONSTRAINT DF_product_variants_stock_quantity DEFAULT 0,
    reserved_qty     INT NOT NULL CONSTRAINT DF_product_variants_reserved_qty DEFAULT 0,
    attributes_json  NVARCHAR(MAX) NULL,
    is_active        BIT NOT NULL CONSTRAINT DF_product_variants_is_active DEFAULT 1,
    created_at       DATETIME2 NOT NULL CONSTRAINT DF_product_variants_created_at DEFAULT SYSDATETIME(),
    updated_at       DATETIME2 NOT NULL CONSTRAINT DF_product_variants_updated_at DEFAULT SYSDATETIME(),
    deleted_at       DATETIME2 NULL,
    CONSTRAINT UQ_product_variants_sku UNIQUE (sku),
    CONSTRAINT FK_variants_product FOREIGN KEY (product_id)
        REFERENCES dbo.products(id) ON DELETE CASCADE
);
GO
CREATE INDEX IX_variants_product ON dbo.product_variants(product_id);
CREATE INDEX IX_variants_stock   ON dbo.product_variants(stock_quantity, reserved_qty);
CREATE INDEX IX_variants_created ON dbo.product_variants(created_at DESC);
GO

CREATE TABLE dbo.images (
    id         BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_images PRIMARY KEY,
    product_id INT NULL,
    variant_id INT NULL,
    url        NVARCHAR(800) NOT NULL,
    is_primary BIT NOT NULL CONSTRAINT DF_images_is_primary DEFAULT 0,
    sort_order INT NOT NULL CONSTRAINT DF_images_sort_order DEFAULT 0,
    alt_text   NVARCHAR(200) NULL,
    meta_json  NVARCHAR(MAX) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_images_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT FK_images_product FOREIGN KEY (product_id)
        REFERENCES dbo.products(id) ON DELETE NO ACTION,
    CONSTRAINT FK_images_variant FOREIGN KEY (variant_id)
        REFERENCES dbo.product_variants(id) ON DELETE NO ACTION,
    CONSTRAINT CK_images_owner CHECK (
        (product_id IS NOT NULL AND variant_id IS NULL) OR
        (product_id IS NULL AND variant_id IS NOT NULL)
    )
);
GO
CREATE INDEX IX_images_product ON dbo.images(product_id, is_primary DESC, sort_order, id DESC);
CREATE INDEX IX_images_variant ON dbo.images(variant_id, is_primary DESC, sort_order, id DESC);
GO

---------------------------------------------------------------
-- 5) INVENTORY TRANSACTIONS
---------------------------------------------------------------
CREATE TABLE dbo.stock_transactions (
    id             BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_stock_transactions PRIMARY KEY,
    variant_id     INT NOT NULL,
    quantity       INT NOT NULL,
    type           NVARCHAR(30) NOT NULL,
    reference_type NVARCHAR(50) NULL,
    reference_id   BIGINT NULL,
    note           NVARCHAR(500) NULL,
    created_by     INT NULL,
    created_at     DATETIME2 NOT NULL CONSTRAINT DF_stock_transactions_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT FK_st_variant FOREIGN KEY (variant_id)
        REFERENCES dbo.product_variants(id) ON DELETE CASCADE,
    CONSTRAINT FK_st_user FOREIGN KEY (created_by)
        REFERENCES dbo.users(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_st_variant_date ON dbo.stock_transactions(variant_id, created_at DESC);
CREATE INDEX IX_st_ref ON dbo.stock_transactions(reference_type, reference_id);
GO

---------------------------------------------------------------
-- 6) PROMOTIONS + PRICE HISTORY
---------------------------------------------------------------
CREATE TABLE dbo.promotions (
    id                 INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_promotions PRIMARY KEY,
    code               NVARCHAR(50) NOT NULL,
    name               NVARCHAR(150) NOT NULL,
    description        NVARCHAR(500) NULL,
    discount_type      NVARCHAR(20) NOT NULL,
    discount_value     DECIMAL(12,2) NOT NULL,
    min_order_amount   DECIMAL(15,2) NULL,
    applicability_json NVARCHAR(MAX) NULL,
    rules_json         NVARCHAR(MAX) NULL,
    priority           INT NOT NULL CONSTRAINT DF_promotions_priority DEFAULT 0,
    stackable          BIT NOT NULL CONSTRAINT DF_promotions_stackable DEFAULT 0,
    start_date         DATETIME2 NOT NULL,
    end_date           DATETIME2 NOT NULL,
    usage_limit        INT NULL,
    used_count         INT NOT NULL CONSTRAINT DF_promotions_used_count DEFAULT 0,
    is_active          BIT NOT NULL CONSTRAINT DF_promotions_is_active DEFAULT 1,
    created_by         INT NULL,
    created_at         DATETIME2 NOT NULL CONSTRAINT DF_promotions_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_promotions_code UNIQUE (code),
    CONSTRAINT FK_promotions_user FOREIGN KEY (created_by)
        REFERENCES dbo.users(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_promotions_active_dates ON dbo.promotions(is_active, start_date, end_date);
GO

CREATE TABLE dbo.price_history (
    id             BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_price_history PRIMARY KEY,
    variant_id     INT NOT NULL,
    currency_code  CHAR(3) NOT NULL CONSTRAINT DF_price_history_currency_code DEFAULT 'VND',
    price          DECIMAL(12,2) NOT NULL,
    cost_price     DECIMAL(12,2) NULL,
    reason         NVARCHAR(200) NULL,
    effective_from DATETIME2 NOT NULL CONSTRAINT DF_price_history_effective_from DEFAULT SYSDATETIME(),
    effective_to   DATETIME2 NULL,
    created_by     INT NULL,
    created_at     DATETIME2 NOT NULL CONSTRAINT DF_price_history_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT FK_ph_variant FOREIGN KEY (variant_id)
        REFERENCES dbo.product_variants(id) ON DELETE CASCADE,
    CONSTRAINT FK_ph_user FOREIGN KEY (created_by)
        REFERENCES dbo.users(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_ph_variant_date ON dbo.price_history(variant_id, effective_from DESC);
GO

---------------------------------------------------------------
-- 7) ORDERS / ITEMS / PAYMENTS / RETURNS
---------------------------------------------------------------
CREATE TABLE dbo.orders (
    id             BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_orders PRIMARY KEY,
    order_number   NVARCHAR(40) NOT NULL
        CONSTRAINT DF_orders_order_number DEFAULT (
            CONCAT(
                N'ORD-',
                CONVERT(CHAR(8), SYSDATETIME(), 112),
                N'-',
                RIGHT(N'000000' + CONVERT(VARCHAR(20), NEXT VALUE FOR dbo.order_seq), 6)
            )
        ),
    customer_id    INT NULL,
    user_id        INT NULL,
    channel        NVARCHAR(20) NOT NULL CONSTRAINT DF_orders_channel DEFAULT N'OFFLINE',
    status         NVARCHAR(30) NOT NULL CONSTRAINT DF_orders_status DEFAULT N'PENDING',
    payment_method NVARCHAR(50) NULL,
    payment_status NVARCHAR(20) NOT NULL CONSTRAINT DF_orders_payment_status DEFAULT N'UNPAID',
    shipping_address NVARCHAR(500) NULL,
    shipping_fee   DECIMAL(12,2) NOT NULL CONSTRAINT DF_orders_shipping_fee DEFAULT 0,
    shipping_status NVARCHAR(30) NULL,
    tracking_code  NVARCHAR(100) NULL,
    paid_at        DATETIME2 NULL,
    shipped_at     DATETIME2 NULL,
    delivered_at   DATETIME2 NULL,
    cancelled_at   DATETIME2 NULL,
    returned_at    DATETIME2 NULL,
    subtotal       DECIMAL(15,2) NOT NULL CONSTRAINT DF_orders_subtotal DEFAULT 0,
    discount_total DECIMAL(15,2) NOT NULL CONSTRAINT DF_orders_discount_total DEFAULT 0,
    tax_total      DECIMAL(15,2) NOT NULL CONSTRAINT DF_orders_tax_total DEFAULT 0,
    total_amount   DECIMAL(15,2) NOT NULL CONSTRAINT DF_orders_total_amount DEFAULT 0,
    applied_promotion_code NVARCHAR(50) NULL,
    applied_promotion_json NVARCHAR(MAX) NULL,
    notes          NVARCHAR(MAX) NULL,
    created_at     DATETIME2 NOT NULL CONSTRAINT DF_orders_created_at DEFAULT SYSDATETIME(),
    updated_at     DATETIME2 NOT NULL CONSTRAINT DF_orders_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_orders_order_number UNIQUE (order_number),
    CONSTRAINT FK_orders_customer FOREIGN KEY (customer_id)
        REFERENCES dbo.customers(id) ON DELETE SET NULL,
    CONSTRAINT FK_orders_user FOREIGN KEY (user_id)
        REFERENCES dbo.users(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_orders_status_date ON dbo.orders(status, created_at DESC);
CREATE INDEX IX_orders_customer_date ON dbo.orders(customer_id, created_at DESC);
CREATE INDEX IX_orders_user_date ON dbo.orders(user_id, created_at DESC);
CREATE INDEX IX_orders_channel_date ON dbo.orders(channel, created_at DESC);
GO

CREATE TABLE dbo.order_items (
    id           BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_order_items PRIMARY KEY,
    order_id     BIGINT NOT NULL,
    variant_id   INT NULL,
    product_id   INT NULL,
    product_name NVARCHAR(200) NOT NULL,
    variant_name NVARCHAR(150) NULL,
    sku          NVARCHAR(100) NULL,
    quantity     INT NOT NULL,
    unit_price   DECIMAL(12,2) NOT NULL,
    discount     DECIMAL(12,2) NOT NULL CONSTRAINT DF_order_items_discount DEFAULT 0,
    line_total   DECIMAL(15,2) NOT NULL,
    created_at   DATETIME2 NOT NULL CONSTRAINT DF_order_items_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT FK_oi_order FOREIGN KEY (order_id)
        REFERENCES dbo.orders(id) ON DELETE NO ACTION,
    CONSTRAINT FK_oi_variant FOREIGN KEY (variant_id)
        REFERENCES dbo.product_variants(id) ON DELETE NO ACTION,
    CONSTRAINT FK_oi_product FOREIGN KEY (product_id)
        REFERENCES dbo.products(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_oi_order ON dbo.order_items(order_id);
CREATE INDEX IX_oi_variant ON dbo.order_items(variant_id);
GO

CREATE TABLE dbo.payments (
    id              BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_payments PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    method          NVARCHAR(50) NOT NULL,
    transaction_ref NVARCHAR(100) NULL,
    status          NVARCHAR(20) NOT NULL CONSTRAINT DF_payments_status DEFAULT N'PENDING',
    paid_at         DATETIME2 NULL,
    created_at      DATETIME2 NOT NULL CONSTRAINT DF_payments_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT FK_payments_order FOREIGN KEY (order_id)
        REFERENCES dbo.orders(id) ON DELETE CASCADE
);
GO
CREATE INDEX IX_payments_order_date ON dbo.payments(order_id, created_at DESC);
GO

CREATE TABLE dbo.returns (
    id            BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_returns PRIMARY KEY,
    order_id      BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    quantity      INT NOT NULL,
    reason        NVARCHAR(500) NULL,
    refund_amount DECIMAL(15,2) NOT NULL CONSTRAINT DF_returns_refund_amount DEFAULT 0,
    refund_method NVARCHAR(50) NULL,
    refund_status NVARCHAR(20) NOT NULL CONSTRAINT DF_returns_refund_status DEFAULT N'PENDING',
    refunded_at   DATETIME2 NULL,
    status        NVARCHAR(20) NOT NULL CONSTRAINT DF_returns_status DEFAULT N'PENDING',
    note          NVARCHAR(MAX) NULL,
    processed_by  INT NULL,
    created_at    DATETIME2 NOT NULL CONSTRAINT DF_returns_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT FK_returns_order FOREIGN KEY (order_id)
        REFERENCES dbo.orders(id) ON DELETE NO ACTION,
    CONSTRAINT FK_returns_item FOREIGN KEY (order_item_id)
        REFERENCES dbo.order_items(id) ON DELETE NO ACTION,
    CONSTRAINT FK_returns_user FOREIGN KEY (processed_by)
        REFERENCES dbo.users(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_returns_order ON dbo.returns(order_id, created_at DESC);
CREATE INDEX IX_returns_item ON dbo.returns(order_item_id);
GO

---------------------------------------------------------------
-- 8) SYSTEM SETTINGS
---------------------------------------------------------------
CREATE TABLE dbo.system_settings (
    setting_key   NVARCHAR(50) NOT NULL CONSTRAINT PK_system_settings PRIMARY KEY,
    setting_value NVARCHAR(100) NOT NULL,
    updated_at    DATETIME2 NOT NULL CONSTRAINT DF_system_settings_updated_at DEFAULT SYSDATETIME()
);
GO

---------------------------------------------------------------
-- 9) CART ITEMS
---------------------------------------------------------------
CREATE TABLE dbo.cart_items (
    id                 INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_cart_items PRIMARY KEY,
    customer_id         INT NOT NULL,
    product_variant_id  INT NOT NULL,
    quantity            INT NOT NULL,
    created_at          DATETIME2 NOT NULL CONSTRAINT DF_cart_items_created_at DEFAULT SYSDATETIME(),
    updated_at          DATETIME2 NOT NULL CONSTRAINT DF_cart_items_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT CK_cart_items_quantity CHECK (quantity > 0),
    CONSTRAINT UQ_cart_items_customer_variant UNIQUE (customer_id, product_variant_id),
    CONSTRAINT FK_cart_items_customer FOREIGN KEY (customer_id)
        REFERENCES dbo.customers(id) ON DELETE CASCADE,
    CONSTRAINT FK_cart_items_product_variant FOREIGN KEY (product_variant_id)
        REFERENCES dbo.product_variants(id) ON DELETE NO ACTION
);
GO
CREATE INDEX IX_cart_items_customer ON dbo.cart_items(customer_id);
CREATE INDEX IX_cart_items_variant ON dbo.cart_items(product_variant_id);
GO

---------------------------------------------------------------
-- SEED (minimal)
-- (Không dùng table variable để tránh lỗi scope khi chạy theo từng batch GO)
---------------------------------------------------------------
INSERT INTO dbo.system_settings(setting_key, setting_value)
VALUES (N'DEFAULT_CURRENCY', N'VND');
GO

INSERT INTO dbo.users (username, email, password_hash, role, is_active)
VALUES
(N'admin',      N'admin@laptopstore.vn',     N'$2a$10$admin.hash.placeholder',     N'ADMIN',     1),
(N'sales01',    N'sales01@laptopstore.vn',   N'$2a$10$sales.hash.placeholder',     N'SALES',     1),
(N'inv01',      N'inv01@laptopstore.vn',     N'$2a$10$inv.hash.placeholder',       N'INVENTORY', 1),
(N'customer01', N'customer01@gmail.com',     N'$2a$10$cust1.hash.placeholder',     N'CUSTOMER',  1);
GO

INSERT INTO dbo.customers (user_id, name, email, phone, date_of_birth, customer_type, vip_tier, segments_json,
                           loyalty_points, total_spent, last_order_at, last_login_at, address, notes, is_active)
VALUES
(
    (SELECT id FROM dbo.users WHERE username = N'customer01'),
    N'Customer 01',
    N'customer01@gmail.com',
    NULL,
    NULL,
    N'REGULAR',
    NULL,
    NULL,
    0,
    0,
    NULL,
    NULL,
    NULL,
    NULL,
    1
);
GO
