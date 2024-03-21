-- USE testdb;

INSERT INTO business_product (id, business_id, product_id, split_size, field_length, configuration_split_size)
SELECT 100, 'business123', 'product123', 10, 13, 10
WHERE NOT EXISTS (
    SELECT 1 FROM business_product WHERE business_id = 'business123' AND product_id = 'product123'
);

INSERT INTO business_product (id, business_id, product_id, split_size, field_length, configuration_split_size)
SELECT 101, 'business123', 'productabc', 20, 13, 10
WHERE NOT EXISTS (
    SELECT 1 FROM business_product WHERE business_id = 'business123' AND product_id = 'productabc'
);

INSERT INTO business_product (id, business_id, product_id, split_size, field_length, configuration_split_size)
SELECT 102, 'business999', 'product3', 30, 13, 10
WHERE NOT EXISTS (
    SELECT 1 FROM business_product WHERE business_id = 'business999' AND product_id = 'product3'
);