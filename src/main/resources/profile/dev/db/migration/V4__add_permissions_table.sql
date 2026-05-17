-- Create permissions table for fine-grained access control

CREATE TABLE IF NOT EXISTS permissions (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_resource_action UNIQUE(resource, action)
    );

-- Create role_permissions junction table
CREATE TABLE IF NOT EXISTS role_permissions (
                                                role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
    );

-- Insert default permissions
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('user:read', 'Can read user information', 'user', 'read'),
                                                                  ('user:write', 'Can create/update user information', 'user', 'write'),
                                                                  ('user:delete', 'Can delete users', 'user', 'delete'),
                                                                  ('role:read', 'Can read role information', 'role', 'read'),
                                                                  ('role:write', 'Can create/update roles', 'role', 'write'),
                                                                  ('role:delete', 'Can delete roles', 'role', 'delete'),
                                                                  ('admin:access', 'Full administrative access', 'admin', 'access')
    ON CONFLICT (name) DO NOTHING;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_permissions_resource ON permissions(resource);
CREATE INDEX IF NOT EXISTS idx_permissions_action ON permissions(action);