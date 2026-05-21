-- Drop unique constraints on username and email as they are now scoped by product or admin role
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_username_key;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;

-- Add a unique constraint combining username and product, allowing duplicate usernames across different products
-- We use coalesce to handle NULL products for admin users
CREATE UNIQUE INDEX idx_users_username_product ON users (username, COALESCE(product, 'ADMIN'));
CREATE UNIQUE INDEX idx_users_email_product ON users (email, COALESCE(product, 'ADMIN'));
