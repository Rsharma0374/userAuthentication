-- Drop old unique indexes that reference the dropped product column
DROP INDEX IF EXISTS idx_users_username_product;
DROP INDEX IF EXISTS idx_users_email_product;

-- Add unique constraints on username and email (globally unique for multi-product)
ALTER TABLE users ADD CONSTRAINT users_username_key UNIQUE (username);
ALTER TABLE users ADD CONSTRAINT users_email_key UNIQUE (email);
