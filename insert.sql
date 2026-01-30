USE retail_management;
GO
SET NOCOUNT ON;

---------------------------------------------------------------
-- 1) USERS
---------------------------------------------------------------
DECLARE @Users TABLE (username NVARCHAR(50) PRIMARY KEY, id INT);

INSERT INTO dbo.users (username, email, password_hash, role, is_active)
OUTPUT inserted.username, inserted.id INTO @Users(username, id)
VALUES
(N'admin',      N'admin@laptopstore.vn',     N'$2a$10$admin.hash.placeholder',     N'ADMIN',     1),
(N'sales01',    N'sales01@laptopstore.vn',   N'$2a$10$sales.hash.placeholder',     N'SALES',     1),
(N'inv01',      N'inv01@laptopstore.vn',     N'$2a$10$inv.hash.placeholder',       N'INVENTORY', 1),
(N'customer01', N'customer01@gmail.com',     N'$2a$10$cust1.hash.placeholder',     N'CUSTOMER',  1),
(N'customer02', N'customer02@gmail.com',     N'$2a$10$cust2.hash.placeholder',     N'CUSTOMER',  1);

---------------------------------------------------------------
-- 1.1) USER_LOGINS (demo lịch sử đăng nhập)
---------------------------------------------------------------
INSERT INTO dbo.user_logins (user_id, username, success, ip_address, user_agent)
VALUES
((SELECT id FROM @Users WHERE username=N'admin'),   N'admin',      1, N'127.0.0.1', N'PostmanRuntime/7.36'),
((SELECT id FROM @Users WHERE username=N'sales01'), N'sales01',    1, N'127.0.0.1', N'PostmanRuntime/7.36'),
(NULL,                                             N'hacker',     0, N'10.0.0.99', N'curl/8.0');

---------------------------------------------------------------
-- 1.2) PASSWORD_RESETS (demo)
---------------------------------------------------------------
INSERT INTO dbo.password_resets (user_id, token_hash, expires_at, requested_ip)
VALUES
((SELECT id FROM @Users WHERE username=N'customer01'),
 N'sha256:9f6b0a1f...placeholder...b19c',
 DATEADD(HOUR, 2, SYSDATETIME()),
 N'127.0.0.1');

---------------------------------------------------------------
-- 1.3) AUDIT_LOGS (demo)
---------------------------------------------------------------
INSERT INTO dbo.audit_logs (user_id, module, action, target_type, target_id, details_json, ip_address)
VALUES
((SELECT id FROM @Users WHERE username=N'admin'),  N'SYSTEM',  N'SEED',   NULL, NULL, N'{"note":"Seed initial data"}', N'127.0.0.1'),
((SELECT id FROM @Users WHERE username=N'inv01'),  N'PRODUCT', N'IMPORT', NULL, NULL, N'{"note":"Initial stock import"}', N'127.0.0.1');

---------------------------------------------------------------
-- 2) CUSTOMERS
---------------------------------------------------------------
DECLARE @Customers TABLE (email NVARCHAR(100) PRIMARY KEY, id INT);

INSERT INTO dbo.customers
(name, email, phone, date_of_birth, customer_type, vip_tier, segments_json,
 loyalty_points, total_spent, last_order_at, address, notes, is_active)
OUTPUT inserted.email, inserted.id INTO @Customers(email, id)
VALUES
(N'Nguyễn Minh Anh', N'minhanh@gmail.com', N'0909000111', '1999-05-12', N'VIP',     N'GOLD',
 N'["STUDENT_DISCOUNT_ELIGIBLE","NEWSLETTER"]', 1200, 45000000, DATEADD(DAY,-10,SYSDATETIME()),
 N'Q.1, TP.HCM', N'Khách hay mua laptop gaming', 1),

(N'Trần Quốc Huy',   N'quochuy@gmail.com', N'0912000222', '2001-09-21', N'REGULAR', NULL,
 NULL, 120, 12500000, DATEADD(DAY,-40,SYSDATETIME()),
 N'Thủ Đức, TP.HCM', N'Ưu tiên máy mỏng nhẹ', 1),

(N'Lê Thu Hà',       N'thuha@gmail.com',   N'0988000333', NULL,         N'REGULAR', NULL,
 NULL, 0, 0, NULL,
 N'Hà Nội', N'Khách mới', 1);

---------------------------------------------------------------
-- 2.1) LOYALTY_LEDGER (demo cộng điểm)
---------------------------------------------------------------
INSERT INTO dbo.loyalty_ledger (customer_id, points_delta, reason, reference_type, reference_id, note, created_by)
VALUES
((SELECT id FROM @Customers WHERE email=N'minhanh@gmail.com'),  +1000, N'PURCHASE', N'orders',  NULL, N'Cộng điểm mua hàng', (SELECT id FROM @Users WHERE username=N'sales01')),
((SELECT id FROM @Customers WHERE email=N'quochuy@gmail.com'),    +120, N'PURCHASE', N'orders',  NULL, N'Cộng điểm mua hàng', (SELECT id FROM @Users WHERE username=N'sales01'));

---------------------------------------------------------------
-- 3) CATEGORIES
---------------------------------------------------------------
DECLARE @Categories TABLE (name NVARCHAR(150) PRIMARY KEY, id INT);

-- Parent: LAPTOP
INSERT INTO dbo.categories (name, description, image_url, parent_id, display_order, is_active)
OUTPUT inserted.name, inserted.id INTO @Categories(name, id)
VALUES
(N'Laptop', N'Danh mục laptop', NULL, NULL, 1, 1),
(N'Phụ kiện', N'Chuột, balo, sạc...', NULL, NULL, 2, 1);

-- Children dưới Laptop
INSERT INTO dbo.categories (name, description, parent_id, display_order, is_active)
OUTPUT inserted.name, inserted.id INTO @Categories(name, id)
VALUES
(N'Laptop Gaming',    N'RTX, hiệu năng cao', (SELECT id FROM @Categories WHERE name=N'Laptop'), 10, 1),
(N'Laptop Văn Phòng', N'Mỏng nhẹ, pin tốt',  (SELECT id FROM @Categories WHERE name=N'Laptop'), 20, 1),
(N'Laptop Đồ Hoạ',    N'Màn đẹp, GPU mạnh',  (SELECT id FROM @Categories WHERE name=N'Laptop'), 30, 1);

---------------------------------------------------------------
-- 3.1) TAGS
---------------------------------------------------------------
DECLARE @Tags TABLE (name NVARCHAR(80) PRIMARY KEY, id INT);

INSERT INTO dbo.tags (name, tag_type, is_active)
OUTPUT inserted.name, inserted.id INTO @Tags(name, id)
VALUES
(N'Bán chạy',   N'GENERAL', 1),
(N'Mới về',     N'GENERAL', 1),
(N'Giảm giá',   N'PROMO',   1),
(N'Trả góp 0%', N'CAMPAIGN',1),
(N'RTX',        N'GENERAL', 1);

---------------------------------------------------------------
-- 4) PRODUCTS
---------------------------------------------------------------
DECLARE @Products TABLE (sku NVARCHAR(100) PRIMARY KEY, id INT);

INSERT INTO dbo.products
(name, sku, description, is_visible, is_featured, attributes_json, view_count, sold_count)
OUTPUT inserted.sku, inserted.id INTO @Products(sku, id)
VALUES
(N'ASUS ROG Strix G16', N'ASUS-ROG-G16',
 N'Laptop gaming 16-inch hiệu năng cao', 1, 1,
 N'{"brand":"ASUS","series":"ROG","cpu_brand":"Intel","screen":"16\" 165Hz"}', 1200, 85),

(N'Dell Inspiron 14', N'DELL-INSP-14',
 N'Laptop văn phòng mỏng nhẹ', 1, 0,
 N'{"brand":"Dell","series":"Inspiron","cpu_brand":"Intel","screen":"14\""}', 900, 40),

(N'Lenovo Legion 5', N'LENOVO-LEGION-5',
 N'Laptop gaming cân bằng giá/hiệu năng', 1, 1,
 N'{"brand":"Lenovo","series":"Legion","cpu_brand":"AMD","screen":"15.6\" 165Hz"}', 700, 55),

(N'Apple MacBook Pro 14 M3', N'APPLE-MBP14-M3',
 N'MacBook Pro 14-inch chip M3', 1, 1,
 N'{"brand":"Apple","series":"MacBook Pro","cpu_brand":"Apple","screen":"14\" Liquid Retina XDR"}', 1500, 60);

---------------------------------------------------------------
-- 4.1) PRODUCT_CATEGORIES
---------------------------------------------------------------
INSERT INTO dbo.product_categories (product_id, category_id, is_primary)
VALUES
-- ASUS ROG G16 -> Gaming
((SELECT id FROM @Products WHERE sku=N'ASUS-ROG-G16'), (SELECT id FROM @Categories WHERE name=N'Laptop Gaming'), 1),

-- Dell Inspiron 14 -> Văn phòng
((SELECT id FROM @Products WHERE sku=N'DELL-INSP-14'), (SELECT id FROM @Categories WHERE name=N'Laptop Văn Phòng'), 1),

-- Lenovo Legion 5 -> Gaming
((SELECT id FROM @Products WHERE sku=N'LENOVO-LEGION-5'), (SELECT id FROM @Categories WHERE name=N'Laptop Gaming'), 1),

-- MacBook Pro -> Đồ hoạ (mang tính demo)
((SELECT id FROM @Products WHERE sku=N'APPLE-MBP14-M3'), (SELECT id FROM @Categories WHERE name=N'Laptop Đồ Hoạ'), 1);

---------------------------------------------------------------
-- 4.2) PRODUCT_TAGS
---------------------------------------------------------------
INSERT INTO dbo.product_tags (product_id, tag_id)
VALUES
((SELECT id FROM @Products WHERE sku=N'ASUS-ROG-G16'),       (SELECT id FROM @Tags WHERE name=N'RTX')),
((SELECT id FROM @Products WHERE sku=N'ASUS-ROG-G16'),       (SELECT id FROM @Tags WHERE name=N'Bán chạy')),
((SELECT id FROM @Products WHERE sku=N'DELL-INSP-14'),       (SELECT id FROM @Tags WHERE name=N'Trả góp 0%')),
((SELECT id FROM @Products WHERE sku=N'LENOVO-LEGION-5'),    (SELECT id FROM @Tags WHERE name=N'Giảm giá')),
((SELECT id FROM @Products WHERE sku=N'APPLE-MBP14-M3'),     (SELECT id FROM @Tags WHERE name=N'Mới về'));

---------------------------------------------------------------
-- 4.3) PRODUCT_VARIANTS
---------------------------------------------------------------
DECLARE @Variants TABLE (sku NVARCHAR(100) PRIMARY KEY, id INT);

-- ASUS ROG G16 variants
INSERT INTO dbo.product_variants
(product_id, variant_name, sku, barcode, currency_code, price, cost_price, price_tiers_json,
 stock_quantity, reserved_qty, attributes_json, is_active)
OUTPUT inserted.sku, inserted.id INTO @Variants(sku, id)
VALUES
((SELECT id FROM @Products WHERE sku=N'ASUS-ROG-G16'),
 N'i9 / 16GB / 1TB / RTX 4060', N'ASUS-ROG-G16-I9-16-1TB-4060', N'8939999000011',
 'VND', 42990000, 38000000, N'{"VIP":41990000,"REGULAR":42990000}',
 12, 0, N'{"cpu":"i9","ram":"16GB","ssd":"1TB","gpu":"RTX 4060","color":"Black"}', 1),

((SELECT id FROM @Products WHERE sku=N'ASUS-ROG-G16'),
 N'i7 / 16GB / 512GB / RTX 4050', N'ASUS-ROG-G16-I7-16-512-4050', N'8939999000012',
 'VND', 35990000, 31500000, N'{"VIP":34990000,"REGULAR":35990000}',
 18, 0, N'{"cpu":"i7","ram":"16GB","ssd":"512GB","gpu":"RTX 4050","color":"Gray"}', 1);

-- Dell Inspiron 14 variants
INSERT INTO dbo.product_variants
(product_id, variant_name, sku, barcode, currency_code, price, cost_price, price_tiers_json,
 stock_quantity, reserved_qty, attributes_json, is_active)
OUTPUT inserted.sku, inserted.id INTO @Variants(sku, id)
VALUES
((SELECT id FROM @Products WHERE sku=N'DELL-INSP-14'),
 N'i5 / 16GB / 512GB', N'DELL-INSP14-I5-16-512', N'8939999000101',
 'VND', 17990000, 15500000, N'{"VIP":17590000,"REGULAR":17990000}',
 25, 0, N'{"cpu":"i5","ram":"16GB","ssd":"512GB","gpu":"Iris Xe","color":"Silver"}', 1),

((SELECT id FROM @Products WHERE sku=N'DELL-INSP-14'),
 N'i7 / 16GB / 1TB', N'DELL-INSP14-I7-16-1TB', N'8939999000102',
 'VND', 21990000, 19000000, N'{"VIP":21490000,"REGULAR":21990000}',
 10, 0, N'{"cpu":"i7","ram":"16GB","ssd":"1TB","gpu":"Iris Xe","color":"Silver"}', 1);

-- Lenovo Legion 5 variants
INSERT INTO dbo.product_variants
(product_id, variant_name, sku, barcode, currency_code, price, cost_price, price_tiers_json,
 stock_quantity, reserved_qty, attributes_json, is_active)
OUTPUT inserted.sku, inserted.id INTO @Variants(sku, id)
VALUES
((SELECT id FROM @Products WHERE sku=N'LENOVO-LEGION-5'),
 N'R7 / 16GB / 512GB / RTX 4060', N'LEGION5-R7-16-512-4060', N'8939999000201',
 'VND', 33990000, 29500000, N'{"VIP":32990000,"REGULAR":33990000}',
 14, 0, N'{"cpu":"R7","ram":"16GB","ssd":"512GB","gpu":"RTX 4060","color":"Black"}', 1);

-- MacBook Pro 14 M3 variants
INSERT INTO dbo.product_variants
(product_id, variant_name, sku, barcode, currency_code, price, cost_price, price_tiers_json,
 stock_quantity, reserved_qty, attributes_json, is_active)
OUTPUT inserted.sku, inserted.id INTO @Variants(sku, id)
VALUES
((SELECT id FROM @Products WHERE sku=N'APPLE-MBP14-M3'),
 N'M3 / 16GB / 512GB', N'MBP14-M3-16-512', N'8939999000301',
 'VND', 44990000, 41000000, N'{"VIP":43990000,"REGULAR":44990000}',
 8, 0, N'{"cpu":"M3","ram":"16GB","ssd":"512GB","color":"Space Gray"}', 1);

---------------------------------------------------------------
-- 4.4) IMAGES (1 bảng dùng chung)
-- Lưu ý CK_images_owner: chỉ được có product_id hoặc variant_id
---------------------------------------------------------------
-- Ảnh product
INSERT INTO dbo.images (product_id, variant_id, url, is_primary, sort_order, alt_text)
VALUES
((SELECT id FROM @Products WHERE sku=N'ASUS-ROG-G16'), NULL, N'https://cdn.example.com/products/asus-rog-g16/main.jpg', 1, 1, N'ASUS ROG Strix G16'),
((SELECT id FROM @Products WHERE sku=N'DELL-INSP-14'), NULL, N'https://cdn.example.com/products/dell-insp-14/main.jpg', 1, 1, N'Dell Inspiron 14'),
((SELECT id FROM @Products WHERE sku=N'LENOVO-LEGION-5'), NULL, N'https://cdn.example.com/products/legion-5/main.jpg', 1, 1, N'Lenovo Legion 5'),
((SELECT id FROM @Products WHERE sku=N'APPLE-MBP14-M3'), NULL, N'https://cdn.example.com/products/mbp14-m3/main.jpg', 1, 1, N'MacBook Pro 14 M3');

-- Ảnh variant (demo)
INSERT INTO dbo.images (product_id, variant_id, url, is_primary, sort_order, alt_text)
VALUES
(NULL, (SELECT id FROM @Variants WHERE sku=N'ASUS-ROG-G16-I9-16-1TB-4060'), N'https://cdn.example.com/variants/asus-g16-i9-4060.jpg', 1, 1, N'G16 i9 RTX4060'),
(NULL, (SELECT id FROM @Variants WHERE sku=N'DELL-INSP14-I5-16-512'),       N'https://cdn.example.com/variants/dell-insp14-i5.jpg',      1, 1, N'Inspiron 14 i5');

---------------------------------------------------------------
-- 5) STOCK_TRANSACTIONS (import tồn ban đầu)
---------------------------------------------------------------
INSERT INTO dbo.stock_transactions (variant_id, quantity, type, reference_type, reference_id, note, created_by)
SELECT v.id,
       pv.stock_quantity,
       N'IMPORT',
       N'seed',
       NULL,
       N'Nhập tồn kho ban đầu',
       (SELECT id FROM @Users WHERE username=N'inv01')
FROM dbo.product_variants pv
JOIN @Variants v ON v.id = pv.id;

---------------------------------------------------------------
-- 6) PROMOTIONS
---------------------------------------------------------------
DECLARE @Promos TABLE (code NVARCHAR(50) PRIMARY KEY, id INT);

INSERT INTO dbo.promotions
(code, name, description, discount_type, discount_value, min_order_amount,
 applicability_json, rules_json, priority, stackable, start_date, end_date,
 usage_limit, used_count, is_active, created_by)
OUTPUT inserted.code, inserted.id INTO @Promos(code, id)
VALUES
(N'NEWYEAR10', N'Tết Sale 10%', N'Giảm 10% cho đơn từ 15 triệu',
 N'PERCENT', 10, 15000000,
 N'{"scope":"ALL"}',
 N'{"max_discount":2000000}',
 10, 0,
 DATEADD(DAY,-7,SYSDATETIME()),
 DATEADD(DAY,30,SYSDATETIME()),
 200, 0, 1,
 (SELECT id FROM @Users WHERE username=N'admin')),

(N'VIP500K', N'VIP giảm 500K', N'Giảm 500.000đ (ưu tiên VIP)',
 N'AMOUNT', 500000, 10000000,
 N'{"customer_type":"VIP","scope":"ALL"}',
 N'{"stackable":false}',
 20, 0,
 DATEADD(DAY,-3,SYSDATETIME()),
 DATEADD(DAY,60,SYSDATETIME()),
 500, 0, 1,
 (SELECT id FROM @Users WHERE username=N'admin'));

---------------------------------------------------------------
-- 6.1) PRICE_HISTORY (lịch sử giá hiện tại)
---------------------------------------------------------------
INSERT INTO dbo.price_history (variant_id, currency_code, price, cost_price, reason, effective_from, created_by)
SELECT pv.id, pv.currency_code, pv.price, pv.cost_price, N'SEED', SYSDATETIME(), (SELECT id FROM @Users WHERE username=N'admin')
FROM dbo.product_variants pv;

---------------------------------------------------------------
-- 7) ORDERS
---------------------------------------------------------------
DECLARE @Orders TABLE (order_key NVARCHAR(10) PRIMARY KEY, id BIGINT);

INSERT INTO dbo.orders
(customer_id, user_id, channel, status, payment_method, payment_status,
 shipping_address, shipping_fee, notes,
 subtotal, discount_total, tax_total, total_amount,
 applied_promotion_code, applied_promotion_json)
OUTPUT 'O1', inserted.id INTO @Orders(order_key, id)
VALUES
((SELECT id FROM @Customers WHERE email=N'minhanh@gmail.com'),
 (SELECT id FROM @Users WHERE username=N'sales01'),
 N'ONLINE', N'PENDING', N'COD', N'UNPAID',
 N'Q.1, TP.HCM', 30000, N'Giao giờ hành chính',
 0,0,0,0,
 N'NEWYEAR10', N'{"code":"NEWYEAR10","type":"PERCENT","value":10}');

INSERT INTO dbo.orders
(customer_id, user_id, channel, status, payment_method, payment_status,
 shipping_address, shipping_fee, notes,
 subtotal, discount_total, tax_total, total_amount,
 applied_promotion_code, applied_promotion_json)
OUTPUT 'O2', inserted.id INTO @Orders(order_key, id)
VALUES
((SELECT id FROM @Customers WHERE email=N'quochuy@gmail.com'),
 (SELECT id FROM @Users WHERE username=N'sales01'),
 N'OFFLINE', N'PAID', N'BANK_TRANSFER', N'PAID',
 NULL, 0, N'Mua tại cửa hàng',
 0,0,0,0,
 NULL, NULL);

---------------------------------------------------------------
-- 7.1) ORDER_ITEMS
---------------------------------------------------------------
DECLARE @OrderItems TABLE (order_key NVARCHAR(10), sku NVARCHAR(100), id BIGINT);

-- O1 items
DECLARE @O1 BIGINT = (SELECT id FROM @Orders WHERE order_key='O1');

INSERT INTO dbo.order_items
(order_id, variant_id, product_id, product_name, variant_name, sku,
 quantity, unit_price, discount, line_total)
OUTPUT 'O1', inserted.sku, inserted.id INTO @OrderItems(order_key, sku, id)
VALUES
(@O1,
 (SELECT id FROM @Variants WHERE sku=N'ASUS-ROG-G16-I9-16-1TB-4060'),
 (SELECT id FROM @Products WHERE sku=N'ASUS-ROG-G16'),
 N'ASUS ROG Strix G16', N'i9 / 16GB / 1TB / RTX 4060', N'ASUS-ROG-G16-I9-16-1TB-4060',
 1, 42990000, 2000000, 40990000),

(@O1,
 (SELECT id FROM @Variants WHERE sku=N'DELL-INSP14-I5-16-512'),
 (SELECT id FROM @Products WHERE sku=N'DELL-INSP-14'),
 N'Dell Inspiron 14', N'i5 / 16GB / 512GB', N'DELL-INSP14-I5-16-512',
 1, 17990000, 0, 17990000);

-- O2 items
DECLARE @O2 BIGINT = (SELECT id FROM @Orders WHERE order_key='O2');

INSERT INTO dbo.order_items
(order_id, variant_id, product_id, product_name, variant_name, sku,
 quantity, unit_price, discount, line_total)
OUTPUT 'O2', inserted.sku, inserted.id INTO @OrderItems(order_key, sku, id)
VALUES
(@O2,
 (SELECT id FROM @Variants WHERE sku=N'LEGION5-R7-16-512-4060'),
 (SELECT id FROM @Products WHERE sku=N'LENOVO-LEGION-5'),
 N'Lenovo Legion 5', N'R7 / 16GB / 512GB / RTX 4060', N'LEGION5-R7-16-512-4060',
 1, 33990000, 1000000, 32990000);

---------------------------------------------------------------
-- 7.2) UPDATE ORDER TOTALS (tính lại subtotal/discount/total)
---------------------------------------------------------------
UPDATE o
SET
    subtotal = x.subtotal,
    discount_total = x.discount_total,
    tax_total = 0,
    total_amount = x.subtotal - x.discount_total + o.shipping_fee,
    updated_at = SYSDATETIME()
FROM dbo.orders o
JOIN (
    SELECT
        order_id,
        SUM(quantity * unit_price) AS subtotal,
        SUM(discount) AS discount_total
    FROM dbo.order_items
    GROUP BY order_id
) x ON x.order_id = o.id;

---------------------------------------------------------------
-- 7.3) PAYMENTS (demo)
---------------------------------------------------------------
-- O2 đã PAID -> tạo payment SUCCESS
INSERT INTO dbo.payments (order_id, amount, method, transaction_ref, status, paid_at)
VALUES
(@O2,
 (SELECT total_amount FROM dbo.orders WHERE id=@O2),
 N'BANK_TRANSFER',
 N'VNPAY-TEST-0001',
 N'SUCCESS',
 SYSDATETIME());

-- O1 chưa trả (PENDING)
INSERT INTO dbo.payments (order_id, amount, method, transaction_ref, status)
VALUES
(@O1,
 1000000,
 N'COD',
 NULL,
 N'PENDING');

---------------------------------------------------------------
-- 7.4) RETURNS (demo trả hàng 1 đơn)
---------------------------------------------------------------
DECLARE @ReturnItemId BIGINT =
(
    SELECT TOP 1 id
    FROM @OrderItems
    WHERE order_key='O2'
    ORDER BY id DESC
);

INSERT INTO dbo.returns
(order_id, order_item_id, quantity, reason, refund_amount, refund_method, refund_status,
 status, note, processed_by, refunded_at)
VALUES
(@O2, @ReturnItemId, 1,
 N'Đổi ý / không phù hợp nhu cầu',
 32990000,
 N'BANK_TRANSFER',
 N'REFUNDED',
 N'DONE',
 N'Hoàn tiền sau khi kiểm tra máy',
 (SELECT id FROM @Users WHERE username=N'sales01'),
 SYSDATETIME());

---------------------------------------------------------------
-- (Tuỳ chọn) Ghi thêm audit log cho order/return
---------------------------------------------------------------
INSERT INTO dbo.audit_logs (user_id, module, action, target_type, target_id, details_json, ip_address)
VALUES
((SELECT id FROM @Users WHERE username=N'sales01'), N'ORDER',  N'CREATE', N'orders',  @O1, N'{"note":"Create order O1"}', N'127.0.0.1'),
((SELECT id FROM @Users WHERE username=N'sales01'), N'RETURN', N'CREATE', N'returns', SCOPE_IDENTITY(), N'{"note":"Create return for O2"}', N'127.0.0.1');

PRINT N'SEED DATA DONE.';