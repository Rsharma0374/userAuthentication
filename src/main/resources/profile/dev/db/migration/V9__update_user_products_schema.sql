-- Drop the single product column
ALTER TABLE users DROP COLUMN IF EXISTS product;

-- Create a new join table for user products
CREATE TABLE IF NOT EXISTS user_products
(
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    product VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, product)
);
