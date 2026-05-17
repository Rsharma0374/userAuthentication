-- Insert default roles (these will be created in Keycloak programmatically)
INSERT INTO user_roles (user_id, role)
SELECT id, 'USER' FROM users WHERE NOT EXISTS (
    SELECT 1 FROM user_roles WHERE role = 'USER'
);