USE retail_management_v2;
GO

SET NOCOUNT ON;

BEGIN TRY
    BEGIN TRAN;

    ------------------------------------------------------------
    -- 1) USERS
    ------------------------------------------------------------
    DECLARE @Users TABLE (username NVARCHAR(50) PRIMARY KEY, user_id INT);

    INSERT dbo.users (username, email, password_hash, role, is_active)
    OUTPUT inserted.username, inserted.id INTO @Users(username, user_id)
    VALUES
      (N'admin',      N'admin@demo.local',      N'$2a$10$demo_admin_hash',      N'ADMIN',     1),
      (N'sales01',    N'sales01@demo.local',    N'$2a$10$demo_sales_hash',      N'SALES',     1),
      (N'inv01',      N'inv01@demo.local',      N'$2a$10$demo_inventory_hash',  N'INVENTORY', 1),
      (N'customer01', N'customer01@demo.local', N'$2a$10$demo_customer_hash',   N'CUSTOMER',  1);

    DECLARE @AdminId INT = (SELECT user_id FROM @Users WHERE username = N'admin');
    DECLARE @SalesId INT = (SELECT user_id FROM @Users WHERE username = N'sales01');
    DECLARE @InvId   INT = (SELECT user_id FROM @Users WHERE username = N'inv01');

    ------------------------------------------------------------
    -- 2) CUSTOMERS
    ------------------------------------------------------------
    DECLARE @Customers TABLE (email NVARCHAR(100) PRIMARY KEY, customer_id INT);

    INSERT dbo.customers (name, email, phone, date_of_birth, customer_type, vip_tier, address, notes, is_active)
    OUTPUT inserted.email, inserted.id INTO @Customers(email, customer_id)
    VALUES
      (N'Nguyễn Văn A', N'a@demo.local', N'0900000001', '1996-05-12', N'REGULAR', NULL,    N'HCM', N'Khách demo thường', 1),
      (N'Trần Thị B',   N'b@demo.local', N'0900000002', '1994-09-20', N'VIP',     N'GOLD', N'HN',  N'Khách VIP demo',   1);

    DECLARE @CustRegularId INT = (SELECT customer_id FROM @Customers WHERE email = N'a@demo.local');
    DECLARE @CustVipId     INT = (SELECT customer_id FROM @Customers WHERE email = N'b@demo.local');

    ------------------------------------------------------------
    -- 3) CATEGORIES
    ------------------------------------------------------------
    DECLARE @Cats TABLE (cat_key NVARCHAR(50) PRIMARY KEY, category_id INT);

    INSERT dbo.categories (name, description, image_url, parent_id, display_order, is_active)
    OUTPUT inserted.name, inserted.id INTO @Cats(cat_key, category_id)
    VALUES
      (N'Electronics', N'Điện tử', NULL, NULL, 1, 1),
      (N'Accessories', N'Phụ kiện', NULL, NULL, 2, 1);

    DECLARE @CatElectronicsId INT = (SELECT category_id FROM @Cats WHERE cat_key = N'Electronics');
    DECLARE @CatAccessoriesId INT = (SELECT category_id FROM @Cats WHERE cat_key = N'Accessories');

    -- Sub-category dưới Electronics (SỬA LỖI: insert đúng vào @Cats)
    INSERT dbo.categories (name, description, image_url, parent_id, display_order, is_active)
    OUTPUT inserted.name, inserted.id INTO @Cats(cat_key, category_id)
    VALUES
      (N'Smartphones', N'Điện thoại', NULL, @CatElectronicsId, 10, 1);

    DECLARE @SmartphonesId INT = (SELECT category_id FROM @Cats WHERE cat_key = N'Smartphones');

    ------------------------------------------------------------
    -- 4) TAGS
    ------------------------------------------------------------
    DECLARE @Tags TABLE (tag_name NVARCHAR(80) PRIMARY KEY, tag_id INT);

    INSERT dbo.tags (name, tag_type, is_active)
    OUTPUT inserted.name, inserted.id INTO @Tags(tag_name, tag_id)
    VALUES
      (N'NEW',        N'GENERAL', 1),
      (N'BESTSELLER', N'GENERAL', 1),
      (N'PROMO',      N'PROMO',   1);

    DECLARE @TagNewId   INT = (SELECT tag_id FROM @Tags WHERE tag_name = N'NEW');
    DECLARE @TagBestId  INT = (SELECT tag_id FROM @Tags WHERE tag_name = N'BESTSELLER');
    DECLARE @TagPromoId INT = (SELECT tag_id FROM @Tags WHERE tag_name = N'PROMO');

    ------------------------------------------------------------
    -- 5) PRODUCTS + VARIANTS
    ------------------------------------------------------------
    DECLARE @Products TABLE (sku NVARCHAR(100) PRIMARY KEY, product_id INT);
    DECLARE @Variants TABLE (sku NVARCHAR(100) PRIMARY KEY, variant_id INT);

    INSERT dbo.products (name, sku, description, is_visible, is_featured, attributes_json)
    OUTPUT inserted.sku, inserted.id INTO @Products(sku, product_id)
    VALUES
      (N'Phone X', N'PROD-PHONEX', N'Điện thoại demo', 1, 1, N'{"brand":"Demo","type":"phone"}');

    DECLARE @PhoneProductId INT = (SELECT product_id FROM @Products WHERE sku = N'PROD-PHONEX');

    INSERT dbo.product_variants
      (product_id, variant_name, sku, barcode, currency_code, price, cost_price, price_tiers_json, stock_quantity, reserved_qty, attributes_json, is_active)
    OUTPUT inserted.sku, inserted.id INTO @Variants(sku, variant_id)
    VALUES
      (@PhoneProductId, N'Phone X 128GB Black', N'VAR-PHONEX-128-BLK', N'893000000001', 'VND', 12000000, 9000000,  N'{"VIP":11500000,"REGULAR":12000000}', 50, 0, N'{"storage":"128GB","color":"Black"}', 1),
      (@PhoneProductId, N'Phone X 256GB Black', N'VAR-PHONEX-256-BLK', N'893000000002', 'VND', 14000000, 10500000, N'{"VIP":13500000,"REGULAR":14000000}', 30, 0, N'{"storage":"256GB","color":"Black"}', 1);

    INSERT dbo.products (name, sku, description, is_visible, is_featured, attributes_json)
    OUTPUT inserted.sku, inserted.id INTO @Products(sku, product_id)
    VALUES
      (N'USB-C Cable 1m', N'PROD-CABLE-USBC-1M', N'Cáp sạc demo', 1, 0, N'{"brand":"Demo","type":"cable"}');

    DECLARE @CableProductId INT = (SELECT product_id FROM @Products WHERE sku = N'PROD-CABLE-USBC-1M');

    INSERT dbo.product_variants
      (product_id, variant_name, sku, barcode, currency_code, price, cost_price, price_tiers_json, stock_quantity, reserved_qty, attributes_json, is_active)
    OUTPUT inserted.sku, inserted.id INTO @Variants(sku, variant_id)
    VALUES
      (@CableProductId, N'USB-C Cable 1m White', N'VAR-CABLE-USBC-1M-WHT', N'893000000010', 'VND', 99000, 45000, N'{"VIP":90000,"REGULAR":99000}', 200, 0, N'{"length":"1m","color":"White"}', 1);

    DECLARE @VarPhone128Id INT = (SELECT variant_id FROM @Variants WHERE sku = N'VAR-PHONEX-128-BLK');
    DECLARE @VarPhone256Id INT = (SELECT variant_id FROM @Variants WHERE sku = N'VAR-PHONEX-256-BLK');
    DECLARE @VarCableId    INT = (SELECT variant_id FROM @Variants WHERE sku = N'VAR-CABLE-USBC-1M-WHT');

    ------------------------------------------------------------
    -- 6) PRODUCT_CATEGORIES + PRODUCT_TAGS
    ------------------------------------------------------------
    INSERT dbo.product_categories (product_id, category_id, is_primary)
    VALUES
      (@PhoneProductId, @SmartphonesId, 1),
      (@CableProductId, @CatAccessoriesId, 1);

    INSERT dbo.product_tags (product_id, tag_id)
    VALUES
      (@PhoneProductId, @TagNewId),
      (@PhoneProductId, @TagBestId),
      (@CableProductId, @TagPromoId);

    ------------------------------------------------------------
    -- 7) IMAGES
    ------------------------------------------------------------
    INSERT dbo.images (product_id, url, is_primary, sort_order, alt_text)
    VALUES
      (@PhoneProductId, N'https://example.com/images/phonex-main.jpg', 1, 1, N'Phone X main'),
      (@PhoneProductId, N'https://example.com/images/phonex-2.jpg',    0, 2, N'Phone X 2'),
      (@CableProductId, N'https://example.com/images/cable-main.jpg',  1, 1, N'Cable main');

    INSERT dbo.images (variant_id, url, is_primary, sort_order, alt_text)
    VALUES
      (@VarPhone128Id, N'https://example.com/images/phonex-128.jpg', 1, 1, N'Phone X 128');

    ------------------------------------------------------------
    -- 8) PRICE HISTORY
    ------------------------------------------------------------
    INSERT dbo.price_history (variant_id, currency_code, price, cost_price, reason, created_by)
    VALUES
      (@VarPhone128Id, 'VND', 12000000,  9000000,  N'MANUAL', @AdminId),
      (@VarPhone256Id, 'VND', 14000000, 10500000,  N'MANUAL', @AdminId),
      (@VarCableId,    'VND',    99000,    45000,  N'MANUAL', @AdminId);

    ------------------------------------------------------------
    -- 9) STOCK TRANSACTIONS (IMPORT)
    ------------------------------------------------------------
    INSERT dbo.stock_transactions (variant_id, quantity, type, reference_type, reference_id, note, created_by)
    VALUES
      (@VarPhone128Id,  50, N'IMPORT', N'manual', NULL, N'Nhập kho ban đầu', @InvId),
      (@VarPhone256Id,  30, N'IMPORT', N'manual', NULL, N'Nhập kho ban đầu', @InvId),
      (@VarCableId,    200, N'IMPORT', N'manual', NULL, N'Nhập kho ban đầu', @InvId);

    ------------------------------------------------------------
    -- 10) PROMOTIONS
    ------------------------------------------------------------
    DECLARE @PromoId INT;

    INSERT dbo.promotions
      (code, name, description, discount_type, discount_value, min_order_amount, applicability_json, rules_json,
       priority, stackable, start_date, end_date, usage_limit, used_count, is_active, created_by)
    VALUES
      (N'WELCOME10', N'Giảm 10%', N'Giảm 10% cho đơn từ 500k', N'PERCENT', 10, 500000,
       N'{"scope":"ALL"}', NULL, 1, 0, DATEADD(DAY,-30,SYSDATETIME()), DATEADD(DAY,365,SYSDATETIME()), NULL, 0, 1, @AdminId);

    SET @PromoId = (SELECT TOP 1 id FROM dbo.promotions WHERE code = N'WELCOME10' ORDER BY id DESC);

    ------------------------------------------------------------
    -- 11) ORDERS + ITEMS + PAYMENTS + LOYALTY
    ------------------------------------------------------------
    DECLARE @OrderPaidId BIGINT;
    DECLARE @OrderPendingId BIGINT;

    DECLARE @SubTotalPaid DECIMAL(15,2) = 12000000 + 99000;
    DECLARE @DiscountPaid DECIMAL(15,2) = CAST(@SubTotalPaid * 0.10 AS DECIMAL(15,2));
    DECLARE @ShipFeePaid  DECIMAL(12,2) = 30000;
    DECLARE @NetPaid      DECIMAL(15,2) = @SubTotalPaid - @DiscountPaid; -- không tính ship
    DECLARE @TotalPaid    DECIMAL(15,2) = @NetPaid + @ShipFeePaid;

    INSERT dbo.orders
      (customer_id, user_id, channel, status, payment_method, payment_status,
       shipping_address, shipping_fee, subtotal, discount_total, tax_total, total_amount,
       applied_promotion_code, applied_promotion_json, notes, paid_at)
    VALUES
      (@CustVipId, @SalesId, N'ONLINE', N'PAID', N'CASH', N'PAID',
       N'HN - VIP Address', @ShipFeePaid,
       @SubTotalPaid, @DiscountPaid, 0, @TotalPaid,
       N'WELCOME10', N'{"code":"WELCOME10","type":"PERCENT","value":10}', N'Đơn demo PAID', SYSDATETIME());

    SET @OrderPaidId = SCOPE_IDENTITY();

    INSERT dbo.order_items
      (order_id, variant_id, product_id, product_name, variant_name, sku, quantity, unit_price, discount, line_total)
    VALUES
      (@OrderPaidId, @VarPhone128Id, @PhoneProductId, N'Phone X', N'Phone X 128GB Black', N'VAR-PHONEX-128-BLK', 1, 12000000, 12000000*0.10, 12000000 - 12000000*0.10),
      (@OrderPaidId, @VarCableId,    @CableProductId, N'USB-C Cable 1m', N'USB-C Cable 1m White', N'VAR-CABLE-USBC-1M-WHT', 1, 99000,    99000*0.10,    99000 - 99000*0.10);

    INSERT dbo.payments (order_id, amount, method, transaction_ref, status, paid_at)
    VALUES
      (@OrderPaidId, @TotalPaid, N'CASH', NULL, N'SUCCESS', SYSDATETIME());

    INSERT dbo.stock_transactions (variant_id, quantity, type, reference_type, reference_id, note, created_by)
    VALUES
      (@VarPhone128Id, -1, N'EXPORT', N'orders', @OrderPaidId, N'Xuất kho theo đơn PAID', @SalesId),
      (@VarCableId,    -1, N'EXPORT', N'orders', @OrderPaidId, N'Xuất kho theo đơn PAID', @SalesId);

    DECLARE @EarnPoints INT = CAST(@NetPaid / 10000 AS INT);

    INSERT dbo.loyalty_ledger (customer_id, points_delta, reason, reference_type, reference_id, note, created_by)
    VALUES
      (@CustVipId, @EarnPoints, N'PURCHASE', N'orders', @OrderPaidId, N'Cộng điểm cho đơn PAID', @SalesId);

    UPDATE dbo.customers
    SET loyalty_points = loyalty_points + @EarnPoints,
        total_spent    = total_spent + @NetPaid,
        last_order_at  = SYSDATETIME(),
        updated_at     = SYSDATETIME()
    WHERE id = @CustVipId;

    INSERT dbo.orders
      (customer_id, user_id, channel, status, payment_method, payment_status,
       shipping_address, shipping_fee, subtotal, discount_total, tax_total, total_amount, notes)
    VALUES
      (@CustRegularId, @SalesId, N'OFFLINE', N'PENDING', NULL, N'UNPAID',
       N'HCM - Regular Address', 0,
       14000000, 0, 0, 14000000,
       N'Đơn demo PENDING');

    SET @OrderPendingId = SCOPE_IDENTITY();

    INSERT dbo.order_items
      (order_id, variant_id, product_id, product_name, variant_name, sku, quantity, unit_price, discount, line_total)
    VALUES
      (@OrderPendingId, @VarPhone256Id, @PhoneProductId, N'Phone X', N'Phone X 256GB Black', N'VAR-PHONEX-256-BLK', 1, 14000000, 0, 14000000);

    ------------------------------------------------------------
    -- 12) AUDIT LOG + USER LOGINS
    ------------------------------------------------------------
    INSERT dbo.audit_logs (user_id, module, action, target_type, target_id, details_json, ip_address)
    VALUES
      (@AdminId, N'SYSTEM', N'SEED', N'orders', @OrderPaidId, N'{"note":"seed order paid created"}', N'127.0.0.1');

    INSERT dbo.user_logins (user_id, username, success, ip_address, user_agent, created_at, updated_at)
    VALUES
      (@AdminId, N'admin', 1, N'127.0.0.1', N'SeedScript', SYSDATETIME(), SYSDATETIME()),
      (NULL,     N'unknown_user', 0, N'127.0.0.1', N'SeedScript', SYSDATETIME(), SYSDATETIME());

    COMMIT;

    SELECT
        (SELECT COUNT(*) FROM dbo.users)              AS users_count,
        (SELECT COUNT(*) FROM dbo.customers)          AS customers_count,
        (SELECT COUNT(*) FROM dbo.categories)         AS categories_count,
        (SELECT COUNT(*) FROM dbo.tags)               AS tags_count,
        (SELECT COUNT(*) FROM dbo.products)           AS products_count,
        (SELECT COUNT(*) FROM dbo.product_variants)   AS variants_count,
        (SELECT COUNT(*) FROM dbo.orders)             AS orders_count,
        (SELECT COUNT(*) FROM dbo.order_items)        AS order_items_count,
        (SELECT COUNT(*) FROM dbo.payments)           AS payments_count,
        (SELECT COUNT(*) FROM dbo.loyalty_ledger)     AS loyalty_ledger_count,
        (SELECT COUNT(*) FROM dbo.stock_transactions) AS stock_transactions_count,
        (SELECT COUNT(*) FROM dbo.promotions)         AS promotions_count;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;

    DECLARE @Err NVARCHAR(4000) = ERROR_MESSAGE();
    DECLARE @Line INT = ERROR_LINE();
    RAISERROR(N'Seed failed at line %d: %s', 16, 1, @Line, @Err);
END CATCH;
GO
