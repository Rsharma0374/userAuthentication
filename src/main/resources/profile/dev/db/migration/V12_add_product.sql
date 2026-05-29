CREATE TABLE products (
                          id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                          product_name VARCHAR(255)   NOT NULL UNIQUE,
                          description  TEXT,
                          created_at   TIMESTAMP      NOT NULL DEFAULT NOW(),
                          updated_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);