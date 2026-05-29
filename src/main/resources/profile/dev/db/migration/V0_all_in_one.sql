-- =============================================================================
-- UAM SERVICE — FRESH INSTALL SCHEMA
-- Consolidated from V1–V12. Single role + single product per user.
-- Removed: user_roles (join table), user_products (join table),
--          permissions, role_permissions (unused fine-grained access control).
-- Run once on a blank database. No prior state required.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- EXTENSIONS
-- ---------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- provides gen_random_uuid()

-- ---------------------------------------------------------------------------
-- 1. CORE TABLES
-- ---------------------------------------------------------------------------

-- Users
-- role  : single role per user stored directly (e.g. 'ADMIN', 'USER', 'SUPER_ADMIN')
-- product: single product per user (NULL for admin/super-admin accounts)
CREATE TABLE IF NOT EXISTS users
(
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id    VARCHAR(255) NOT NULL UNIQUE,
    username       VARCHAR(100) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    first_name     VARCHAR(100),
    last_name      VARCHAR(100),
    role           VARCHAR(100) NOT NULL,
    product        VARCHAR(50),                   -- NULL for admin accounts
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    mfa_enabled    BOOLEAN      NOT NULL DEFAULT FALSE,
    mfa_secret     VARCHAR(255),
    last_login     TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_username_key UNIQUE (username),
    CONSTRAINT users_email_key    UNIQUE (email)
    );

-- Products catalogue
CREATE TABLE IF NOT EXISTS products
(
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_name VARCHAR(255) NOT NULL UNIQUE,
    description  TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
    );

-- Refresh tokens for auth session management
CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    token       VARCHAR(500) NOT NULL UNIQUE,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expiry_date TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Audit trail for all user actions
CREATE TABLE IF NOT EXISTS audit_logs
(
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(50)  NOT NULL,
    user_id    UUID         REFERENCES users (id) ON DELETE SET NULL,
    username   VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    details    JSONB,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Password reset one-time passwords
CREATE TABLE IF NOT EXISTS password_reset_otps
(
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID         NOT NULL UNIQUE,
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    otp_hash   VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Email templates (product-scoped or global/admin)
CREATE TABLE IF NOT EXISTS email_templates
(
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    template_name VARCHAR(100) NOT NULL,
    product       VARCHAR(50),                   -- NULL means generic / admin template
    subject       VARCHAR(255) NOT NULL,
    body          TEXT         NOT NULL,
    from_address  VARCHAR(255) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- ---------------------------------------------------------------------------
-- 2. INDEXES
-- ---------------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_users_username
    ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email
    ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_keycloak_id
    ON users (keycloak_id);
CREATE INDEX IF NOT EXISTS idx_users_role
    ON users (role);
CREATE INDEX IF NOT EXISTS idx_users_product
    ON users (product);
CREATE INDEX IF NOT EXISTS idx_users_mfa_enabled
    ON users (mfa_enabled) WHERE mfa_enabled = TRUE;

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token
    ON refresh_tokens (token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expiry
    ON refresh_tokens (expiry_date) WHERE revoked = FALSE;

CREATE INDEX IF NOT EXISTS idx_audit_logs_event_type
    ON audit_logs (event_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id
    ON audit_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at
    ON audit_logs (created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_details
    ON audit_logs USING GIN (details);

CREATE INDEX IF NOT EXISTS idx_password_reset_otps_request_id
    ON password_reset_otps (request_id);

-- One product-specific template per (template_name, product) when product IS NOT NULL
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_template_per_product
    ON email_templates (template_name, product) WHERE product IS NOT NULL;
-- One global/fallback template per template_name when product IS NULL
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_generic_template
    ON email_templates (template_name) WHERE product IS NULL;

-- ---------------------------------------------------------------------------
-- 3. UTILITY FUNCTIONS
-- ---------------------------------------------------------------------------

-- Purge expired / revoked refresh tokens (schedule via pg_cron or external cron)
CREATE OR REPLACE FUNCTION cleanup_expired_tokens()
RETURNS void AS $$
BEGIN
DELETE FROM refresh_tokens
WHERE expiry_date < NOW() OR revoked = TRUE;
END;
$$ LANGUAGE plpgsql;

-- Create a monthly partition table for audit_logs (call once per month or at startup)
CREATE OR REPLACE FUNCTION create_audit_logs_partition()
RETURNS void AS $$
DECLARE
start_date     DATE;
    end_date       DATE;
    partition_name TEXT;
BEGIN
    start_date     := DATE_TRUNC('month', NOW())::DATE;
    end_date       := start_date + INTERVAL '1 month';
    partition_name := 'audit_logs_' || TO_CHAR(start_date, 'YYYY_MM');

EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF audit_logs
         FOR VALUES FROM (%L) TO (%L)',
        partition_name, start_date, end_date
        );
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------------
-- 4. EMAIL TEMPLATES (SEED DATA)
-- ---------------------------------------------------------------------------
-- ---------------------------------------------------------------------------
INSERT INTO email_templates (template_name, product, subject, body, from_address, is_active)
VALUES ('PASSWORD_RESET_OTP',
        'AI_LOG_ANALYZER',
        'Your Password Reset Request – AI Log Analyzer',
        '<!DOCTYPE html>
    <html>
    <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:Arial,sans-serif;">
      <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:40px 0;">
        <tr>
          <td align="center">
            <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

              <!-- Header -->
              <tr>
                <td style="background-color:#0f172a;padding:28px 40px;text-align:center;">
                  <h1 style="margin:0;color:#ffffff;font-size:20px;font-weight:600;letter-spacing:0.5px;">AI Log Analyzer</h1>
                  <p style="margin:4px 0 0;color:#94a3b8;font-size:12px;letter-spacing:1px;text-transform:uppercase;">Security Notification</p>
                </td>
              </tr>

              <!-- Body -->
              <tr>
                <td style="padding:40px 40px 20px;">
                  <p style="margin:0 0 12px;color:#374151;font-size:15px;">Hello <strong>{{username}}</strong>,</p>
                  <p style="margin:0 0 24px;color:#6b7280;font-size:14px;line-height:1.7;">
                    We received a request to reset the password associated with your AI Log Analyzer account.
                    Use the One-Time Password (OTP) below to proceed. This code is valid for <strong>15 minutes</strong>.
                  </p>

                  <!-- OTP Box -->
                  <table width="100%" cellpadding="0" cellspacing="0">
                    <tr>
                      <td align="center" style="padding:24px 0;">
                        <div style="display:inline-block;background-color:#f0f4ff;border:1px dashed #4f46e5;border-radius:8px;padding:18px 48px;">
                          <p style="margin:0 0 4px;color:#6b7280;font-size:11px;letter-spacing:2px;text-transform:uppercase;">Your OTP Code</p>
                          <p style="margin:0;color:#0f172a;font-size:36px;font-weight:700;letter-spacing:8px;">{{otp}}</p>
                        </div>
                      </td>
                    </tr>
                  </table>

                  <!-- Warning -->
                  <table width="100%" cellpadding="0" cellspacing="0" style="margin:8px 0 28px;">
                    <tr>
                      <td style="background-color:#fff7ed;border-left:4px solid #f97316;border-radius:4px;padding:14px 16px;">
                        <p style="margin:0;color:#92400e;font-size:13px;line-height:1.6;">
                          ⚠️ &nbsp;Do not share this OTP with anyone. Our team will <strong>never</strong> ask for your OTP.
                          If you did not request this, please secure your account immediately.
                        </p>
                      </td>
                    </tr>
                  </table>

                  <p style="margin:0 0 8px;color:#6b7280;font-size:13px;line-height:1.7;">
                    This OTP is single-use only and will expire at <strong>{{expiresAt}}</strong>.
                    If you did not initiate this request, you can safely ignore this email.
                  </p>
                </td>
              </tr>

              <!-- Footer -->
              <tr>
                <td style="background-color:#f8fafc;padding:24px 40px;border-top:1px solid #e5e7eb;text-align:center;">
                  <p style="margin:0 0 4px;color:#9ca3af;font-size:12px;">This is an automated message — please do not reply.</p>
                  <p style="margin:0;color:#9ca3af;font-size:12px;">
                    Need help? Contact us at
                    <a href="mailto:support@guardianservices.in" style="color:#4f46e5;text-decoration:none;">support@guardianservices.in</a>
                  </p>
                  <p style="margin:12px 0 0;color:#d1d5db;font-size:11px;">© 2026 Guardian Services. All rights reserved.</p>
                </td>
              </tr>

            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>',
        'no-reply@guardianservices.in',
        TRUE);


INSERT INTO email_templates (template_name, product, subject, body, from_address, is_active)
VALUES ('PASSWORD_RESET_OTP',
        NULL,
        'Administrator Password Reset Request – Guardian Services',
        '<!DOCTYPE html>
    <html>
    <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:Arial,sans-serif;">
      <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:40px 0;">
        <tr>
          <td align="center">
            <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
              <!-- Header -->
              <tr>
                <td style="background-color:#0a0f1e;padding:28px 40px;text-align:center;">
                  <h1 style="margin:0;color:#ffffff;font-size:20px;font-weight:600;letter-spacing:0.5px;">Guardian Services</h1>
                  <p style="margin:6px 0 0;color:#64748b;font-size:11px;letter-spacing:2px;text-transform:uppercase;">Enterprise Administration Portal</p>
                  <div style="margin:14px auto 0;width:48px;height:2px;background:linear-gradient(to right,#4f46e5,#7c3aed);border-radius:2px;"></div>
                </td>
              </tr>
              <!-- Admin Badge -->
              <tr>
                <td style="background-color:#0f172a;padding:10px 40px 16px;text-align:center;">
                  <span style="display:inline-block;background-color:#1e293b;border:1px solid #334155;border-radius:20px;padding:4px 16px;">
                    <p style="margin:0;color:#94a3b8;font-size:11px;letter-spacing:1.5px;text-transform:uppercase;">🔐 &nbsp;Privileged Account — Confidential</p>
                  </span>
                </td>
              </tr>
              <!-- Body -->
              <tr>
                <td style="padding:40px 40px 20px;">
                  <p style="margin:0 0 12px;color:#374151;font-size:15px;">Hello <strong>{{username}}</strong>,</p>
                  <p style="margin:0 0 8px;color:#6b7280;font-size:14px;line-height:1.7;">
                    A password reset request has been initiated for your
                    <strong style="color:#0f172a;">{{role}}</strong> account on the Guardian Services Enterprise Portal.
                  </p>
                  <p style="margin:0 0 24px;color:#6b7280;font-size:14px;line-height:1.7;">
                    Use the One-Time Password (OTP) below to verify your identity and proceed with the reset.
                    This code is strictly time-bound and valid for <strong>15 minutes</strong>.
                  </p>
                  <!-- OTP Box -->
                  <table width="100%" cellpadding="0" cellspacing="0">
                    <tr>
                      <td align="center" style="padding:24px 0;">
                        <div style="display:inline-block;background-color:#f5f3ff;border:1px dashed #7c3aed;border-radius:8px;padding:18px 48px;">
                          <p style="margin:0 0 4px;color:#7c3aed;font-size:11px;letter-spacing:2px;text-transform:uppercase;">One-Time Password</p>
                          <p style="margin:0;color:#0f172a;font-size:36px;font-weight:700;letter-spacing:8px;">{{otp}}</p>
                          <p style="margin:6px 0 0;color:#9ca3af;font-size:11px;">Expires at &nbsp;<strong>{{expiresAt}}</strong></p>
                        </div>
                      </td>
                    </tr>
                  </table>
                  <!-- High Privilege Warning -->
                  <table width="100%" cellpadding="0" cellspacing="0" style="margin:8px 0 16px;">
                    <tr>
                      <td style="background-color:#fef2f2;border-left:4px solid #dc2626;border-radius:4px;padding:14px 16px;">
                        <p style="margin:0 0 6px;color:#991b1b;font-size:13px;font-weight:600;">🚨 &nbsp;High-Privilege Account Alert</p>
                        <p style="margin:0;color:#b91c1c;font-size:13px;line-height:1.7;">
                          This account holds elevated administrative access across the Guardian Services platform.
                          Never share this OTP with anyone — including Guardian Services staff.
                          If you did not request this reset, <strong>immediately contact your security team</strong>
                          and revoke access if necessary.
                        </p>
                      </td>
                    </tr>
                  </table>
                  <!-- Security Tip -->
                  <table width="100%" cellpadding="0" cellspacing="0" style="margin:8px 0 28px;">
                    <tr>
                      <td style="background-color:#f0fdf4;border-left:4px solid #16a34a;border-radius:4px;padding:14px 16px;">
                        <p style="margin:0;color:#166534;font-size:13px;line-height:1.7;">
                          ✅ &nbsp;<strong>Security Reminder:</strong> Always reset your password from a trusted, secured device.
                          Ensure you are on a private network and no unauthorized personnel can view your screen.
                        </p>
                      </td>
                    </tr>
                  </table>
                  <p style="margin:0 0 8px;color:#6b7280;font-size:13px;line-height:1.7;">
                    This OTP is single-use only. Once used or expired, it becomes permanently invalid.
                    A new reset request will need to be initiated if needed.
                  </p>
                </td>
              </tr>
              <!-- Divider -->
              <tr>
                <td style="padding:0 40px;">
                  <hr style="border:none;border-top:1px solid #e5e7eb;margin:0;" />
                </td>
              </tr>
              <!-- Footer -->
              <tr>
                <td style="background-color:#f8fafc;padding:24px 40px;text-align:center;">
                  <p style="margin:0 0 4px;color:#9ca3af;font-size:12px;">This message was intended for an authorized Guardian Services administrator.</p>
                  <p style="margin:0 0 4px;color:#9ca3af;font-size:12px;">This is an automated message — please do not reply directly to this email.</p>
                  <p style="margin:0;color:#9ca3af;font-size:12px;">
                    Security concerns? Reach us at
                    <a href="mailto:security@guardianservices.in" style="color:#7c3aed;text-decoration:none;">security@guardianservices.in</a>
                  </p>
                  <p style="margin:12px 0 0;color:#d1d5db;font-size:11px;">© 2026 Guardian Services. All rights reserved. &nbsp;|&nbsp; Enterprise Portal</p>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>',
        'noreply@guardianservices.in',
        TRUE);

-- Insert a generic template for successful password resets

-- =====================================================
-- PASSWORD RESET SUCCESS — Product (AI Log Analyzer)
-- =====================================================
INSERT INTO email_templates (template_name, product, subject, body, from_address, is_active)
VALUES ('PASSWORD_RESET_SUCCESS',
        'AI_LOG_ANALYZER',
        'Your AI Log Analyzer Password Has Been Successfully Reset',
        '<!DOCTYPE html>
<html>
<body style="margin:0;padding:0;background-color:#f4f6f9;font-family:Arial,sans-serif;">
<table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:40px 0;">
 <tr>
   <td align="center">
     <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
       <!-- Header -->
       <tr>
         <td style="background-color:#0f172a;padding:28px 40px;text-align:center;">
           <h1 style="margin:0;color:#ffffff;font-size:20px;font-weight:600;letter-spacing:0.5px;">AI Log Analyzer</h1>
           <p style="margin:4px 0 0;color:#94a3b8;font-size:12px;letter-spacing:1px;text-transform:uppercase;">Security Notification</p>
         </td>
       </tr>
       <!-- Success Banner -->
       <tr>
         <td style="background-color:#f0fdf4;padding:20px 40px;text-align:center;border-bottom:1px solid #bbf7d0;">
           <p style="margin:0;font-size:32px;">✅</p>
           <p style="margin:6px 0 0;color:#15803d;font-size:16px;font-weight:600;">Password Reset Successful</p>
           <p style="margin:4px 0 0;color:#6b7280;font-size:12px;">{{resetAt}}</p>
         </td>
       </tr>
       <!-- Body -->
       <tr>
         <td style="padding:36px 40px 20px;">
           <p style="margin:0 0 12px;color:#374151;font-size:15px;">Hello <strong>{{username}}</strong>,</p>
           <p style="margin:0 0 24px;color:#6b7280;font-size:14px;line-height:1.7;">
             This is a confirmation that the password for your <strong style="color:#0f172a;">AI Log Analyzer</strong> account
             was successfully reset. Below are your new login credentials.
           </p>
           <!-- New Password Card -->
           <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
             <tr>
               <td align="center" style="padding:4px 0 20px;">
                 <div style="display:inline-block;background-color:#f0f4ff;border:1px dashed #4f46e5;border-radius:8px;padding:18px 48px;text-align:center;">
                   <p style="margin:0 0 4px;color:#6b7280;font-size:11px;letter-spacing:2px;text-transform:uppercase;">Your New Password</p>
                   <p style="margin:0;color:#0f172a;font-size:24px;font-weight:700;letter-spacing:4px;">{{newPassword}}</p>
                   <p style="margin:6px 0 0;color:#9ca3af;font-size:11px;">Please change this after your first login</p>
                 </div>
               </td>
             </tr>
           </table>
           <!-- Reset Details Card -->
           <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
             <tr>
               <td style="background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px 24px;">
                 <p style="margin:0 0 12px;color:#374151;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;">Reset Summary</p>
                 <table width="100%" cellpadding="0" cellspacing="0">
                   <tr>
                     <td style="color:#6b7280;font-size:13px;padding:4px 0;">Account</td>
                     <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{email}}</td>
                   </tr>
                   <tr>
                     <td style="color:#6b7280;font-size:13px;padding:4px 0;">Reset Time</td>
                     <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{resetAt}}</td>
                   </tr>
                   <tr>
                     <td style="color:#6b7280;font-size:13px;padding:4px 0;">IP Address</td>
                     <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{ipAddress}}</td>
                   </tr>
                 </table>
               </td>
             </tr>
           </table>
           <!-- Security Tip -->
           <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:16px;">
             <tr>
               <td style="background-color:#f0fdf4;border-left:4px solid #16a34a;border-radius:4px;padding:14px 16px;">
                 <p style="margin:0;color:#166534;font-size:13px;line-height:1.7;">
                   ✅ &nbsp;<strong>Recommended:</strong> Log in immediately and change this temporary password
                   to a strong password of your choice from your account settings.
                 </p>
               </td>
             </tr>
           </table>
           <!-- Warning Strip -->
           <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
             <tr>
               <td style="background-color:#fef2f2;border-left:4px solid #dc2626;border-radius:4px;padding:14px 16px;">
                 <p style="margin:0 0 4px;color:#991b1b;font-size:13px;font-weight:600;">🚨 &nbsp;Was this not you?</p>
                 <p style="margin:0;color:#b91c1c;font-size:13px;line-height:1.7;">
                   If you did not perform this action, your account may be compromised.
                   Please contact our support team immediately at
                   <a href="mailto:support@guardianservices.in" style="color:#dc2626;font-weight:600;">support@guardianservices.in</a>
                   and change your password right away.
                 </p>
               </td>
             </tr>
           </table>
           <p style="margin:0 0 8px;color:#6b7280;font-size:13px;line-height:1.7;">
             For your security, all active sessions have been invalidated.
             Please log in again using your new credentials.
           </p>
         </td>
       </tr>
       <!-- Divider -->
       <tr>
         <td style="padding:0 40px;">
           <hr style="border:none;border-top:1px solid #e5e7eb;margin:0;" />
         </td>
       </tr>
       <!-- Footer -->
       <tr>
         <td style="background-color:#f8fafc;padding:24px 40px;text-align:center;">
           <p style="margin:0 0 4px;color:#9ca3af;font-size:12px;">This is an automated message — please do not reply.</p>
           <p style="margin:0;color:#9ca3af;font-size:12px;">
             Need help? Contact us at
             <a href="mailto:support@guardianservices.in" style="color:#4f46e5;text-decoration:none;">support@guardianservices.in</a>
           </p>
           <p style="margin:12px 0 0;color:#d1d5db;font-size:11px;">© 2026 Guardian Services. All rights reserved.</p>
         </td>
       </tr>
     </table>
   </td>
 </tr>
</table>
</body>
</html>',
        'security@guardianservices.in',
        TRUE);


-- =====================================================
-- PASSWORD RESET SUCCESS — NULL Product (Admin / Super Admin)
-- =====================================================
INSERT INTO email_templates (template_name, product, subject, body, from_address, is_active)
VALUES ('PASSWORD_RESET_SUCCESS',
        NULL,
        'Administrator Password Successfully Reset – Guardian Services',
        '<!DOCTYPE html>
<html>
<body style="margin:0;padding:0;background-color:#f4f6f9;font-family:Arial,sans-serif;">
<table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:40px 0;">
 <tr>
   <td align="center">
     <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
       <!-- Header -->
       <tr>
         <td style="background-color:#0a0f1e;padding:28px 40px;text-align:center;">
           <h1 style="margin:0;color:#ffffff;font-size:20px;font-weight:600;letter-spacing:0.5px;">Guardian Services</h1>
           <p style="margin:6px 0 0;color:#64748b;font-size:11px;letter-spacing:2px;text-transform:uppercase;">Enterprise Administration Portal</p>
           <div style="margin:14px auto 0;width:48px;height:2px;background:linear-gradient(to right,#4f46e5,#7c3aed);border-radius:2px;"></div>
         </td>
       </tr>
       <!-- Admin Badge -->
       <tr>
         <td style="background-color:#0f172a;padding:10px 40px 16px;text-align:center;">
           <span style="display:inline-block;background-color:#1e293b;border:1px solid #334155;border-radius:20px;padding:4px 16px;">
             <p style="margin:0;color:#94a3b8;font-size:11px;letter-spacing:1.5px;text-transform:uppercase;">🔐 &nbsp;Privileged Account — Confidential</p>
           </span>
         </td>
       </tr>
       <!-- Success Banner -->
       <tr>
         <td style="background-color:#f0fdf4;padding:20px 40px;text-align:center;border-bottom:1px solid #bbf7d0;">
           <p style="margin:0;font-size:32px;">✅</p>
           <p style="margin:6px 0 0;color:#15803d;font-size:16px;font-weight:600;">Administrator Password Reset Confirmed</p>
           <p style="margin:4px 0 0;color:#6b7280;font-size:12px;">{{resetAt}}</p>
         </td>
       </tr>
       <!-- Body -->
       <tr>
         <td style="padding:36px 40px 20px;">
           <p style="margin:0 0 12px;color:#374151;font-size:15px;">Hello <strong>{{username}}</strong>,</p>
           <p style="margin:0 0 8px;color:#6b7280;font-size:14px;line-height:1.7;">
             This is a secure confirmation that the password for your
             <strong style="color:#0f172a;">{{role}}</strong> account on the
             Guardian Services Enterprise Portal has been successfully reset.
             Below are your new temporary login credentials.
           </p>
           <p style="margin:0 0 24px;color:#6b7280;font-size:14px;line-height:1.7;">
             All previously active sessions have been terminated as a security precaution.
           </p>
           <!-- New Password Card -->
           <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
             <tr>
               <td align="center" style="padding:4px 0 20px;">
                 <div style="display:inline-block;background-color:#f5f3ff;border:1px dashed #7c3aed;border-radius:8px;padding:18px 48px;text-align:center;">
                   <p style="margin:0 0 4px;color:#7c3aed;font-size:11px;letter-spacing:2px;text-transform:uppercase;">Your New Password</p>
                   <p style="margin:0;color:#0f172a;font-size:24px;font-weight:700;letter-spacing:4px;">{{newPassword}}</p>
                   <p style="margin:6px 0 0;color:#9ca3af;font-size:11px;">Change this immediately after your first login</p>
                 </div>
               </td>
             </tr>
           </table>
           <!-- Reset Details Card -->
           <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
             <tr>
               <td style="background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px 24px;">
                 <p style="margin:0 0 12px;color:#374151;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;">Reset Summary</p>
                 <table width="100%" cellpadding="0" cellspacing="0">
                   <tr>
                     <td style="color:#6b7280;font-size:13px;padding:4px 0;">Account</td>
                     <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{email}}</td>
                   </tr>
                   <tr>
                     <td style="color:#6b7280;font-size:13px;padding:4px 0;">Role</td>
                     <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{role}}</td>
                   </tr>
                   <tr>
                     <td style="color:#6b7280;font-size:13px;padding:4px 0;">Reset Time</td>
                     <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{resetAt}}</td>
                   </tr>
                   <tr>
                     <td style="color:#6b7280;font-size:13px;padding:4px 0;">IP Address</td>
                     <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{ipAddress}}</td>
                   </tr>
                 </table>
               </td>
             </tr>
           </table>
           <!-- High Privilege Warning -->
           <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:16px;">
             <tr>
               <td style="background-color:#fef2f2;border-left:4px solid #dc2626;border-radius:4px;padding:14px 16px;">
                 <p style="margin:0 0 6px;color:#991b1b;font-size:13px;font-weight:600;">🚨 &nbsp;High-Privilege Account — Immediate Action Required if Unrecognized</p>
                 <p style="margin:0;color:#b91c1c;font-size:13px;line-height:1.7;">
                   If you did not initiate this password reset, your privileged account may be under
                   unauthorized access. Contact the Guardian Services security team immediately at
                   <a href="mailto:security@guardianservices.in" style="color:#dc2626;font-weight:600;">security@guardianservices.in</a>
                   and request an emergency account suspension.
                 </p>
               </td>
             </tr>
           </table>
           <!-- Security Reminder -->
           <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:16px;">
             <tr>
               <td style="background-color:#f0fdf4;border-left:4px solid #16a34a;border-radius:4px;padding:14px 16px;">
                 <p style="margin:0;color:#166534;font-size:13px;line-height:1.7;">
                   ✅ &nbsp;<strong>Recommended:</strong> Log in immediately and update this temporary password
                   to a strong password of your choice. Enable MFA if not already active and
                   never reuse passwords across platforms.
                 </p>
               </td>
             </tr>
           </table>
           <p style="margin:0 0 8px;color:#6b7280;font-size:13px;line-height:1.7;">
             This event has been logged in the Guardian Services audit trail and is associated with
             Request ID: <strong style="color:#0f172a;">{{requestId}}</strong>.
           </p>
         </td>
       </tr>
       <!-- Divider -->
       <tr>
         <td style="padding:0 40px;">
           <hr style="border:none;border-top:1px solid #e5e7eb;margin:0;" />
         </td>
       </tr>
       <!-- Footer -->
       <tr>
         <td style="background-color:#f8fafc;padding:24px 40px;text-align:center;">
           <p style="margin:0 0 4px;color:#9ca3af;font-size:12px;">This message was intended for an authorized Guardian Services administrator.</p>
           <p style="margin:0 0 4px;color:#9ca3af;font-size:12px;">This is an automated message — please do not reply directly to this email.</p>
           <p style="margin:0;color:#9ca3af;font-size:12px;">
             Security concerns? Reach us at
             <a href="mailto:security@guardianservices.in" style="color:#7c3aed;text-decoration:none;">security@guardianservices.in</a>
           </p>
           <p style="margin:12px 0 0;color:#d1d5db;font-size:11px;">© 2026 Guardian Services. All rights reserved. &nbsp;|&nbsp; Enterprise Portal</p>
         </td>
       </tr>
     </table>
   </td>
 </tr>
</table>
</body>
</html>',
        'security@guardianservices.in',
        TRUE);
-- (V12) Password change success templates
INSERT INTO email_templates (template_name, product, subject, body, from_address, is_active)
VALUES (
           'PASSWORD_CHANGE_SUCCESS',
           'AI_LOG_ANALYZER',
           'Your AI Log Analyzer Password Has Been Successfully Changed',
           '<!DOCTYPE html>
       <html>
       <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:Arial,sans-serif;">
         <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:40px 0;">
           <tr>
             <td align="center">
               <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                 <!-- Header -->
                 <tr>
                   <td style="background-color:#0f172a;padding:28px 40px;text-align:center;">
                     <h1 style="margin:0;color:#ffffff;font-size:20px;font-weight:600;letter-spacing:0.5px;">AI Log Analyzer</h1>
                     <p style="margin:4px 0 0;color:#94a3b8;font-size:12px;letter-spacing:1px;text-transform:uppercase;">Security Notification</p>
                   </td>
                 </tr>

                 <!-- Success Banner -->
                 <tr>
                   <td style="background-color:#f0fdf4;padding:20px 40px;text-align:center;border-bottom:1px solid #bbf7d0;">
                     <p style="margin:0;font-size:32px;">🔒</p>
                     <p style="margin:6px 0 0;color:#15803d;font-size:16px;font-weight:600;">Password Changed Successfully</p>
                     <p style="margin:4px 0 0;color:#6b7280;font-size:12px;">{{changedAt}}</p>
                   </td>
                 </tr>

                 <!-- Body -->
                 <tr>
                   <td style="padding:36px 40px 20px;">
                     <p style="margin:0 0 12px;color:#374151;font-size:15px;">Hello <strong>{{username}}</strong>,</p>
                     <p style="margin:0 0 24px;color:#6b7280;font-size:14px;line-height:1.7;">
                       This is a confirmation that the password for your <strong style="color:#0f172a;">AI Log Analyzer</strong>
                       account was successfully updated. This change was made by you from your account settings.
                     </p>

                     <!-- Change Details Card -->
                     <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
                       <tr>
                         <td style="background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px 24px;">
                           <p style="margin:0 0 12px;color:#374151;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;">Change Summary</p>
                           <table width="100%" cellpadding="0" cellspacing="0">
                             <tr>
                               <td style="color:#6b7280;font-size:13px;padding:4px 0;">Account</td>
                               <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{email}}</td>
                             </tr>
                             <tr>
                               <td style="color:#6b7280;font-size:13px;padding:4px 0;">Changed At</td>
                               <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{changedAt}}</td>
                             </tr>
                             <tr>
                               <td style="color:#6b7280;font-size:13px;padding:4px 0;">IP Address</td>
                               <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{ipAddress}}</td>
                             </tr>
                           </table>
                         </td>
                       </tr>
                     </table>

                     <!-- Security Tip -->
                     <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:16px;">
                       <tr>
                         <td style="background-color:#f0fdf4;border-left:4px solid #16a34a;border-radius:4px;padding:14px 16px;">
                           <p style="margin:0;color:#166534;font-size:13px;line-height:1.7;">
                             ✅ &nbsp;<strong>Security Tip:</strong> For your protection, all other active sessions
                             have been invalidated. Please log in again using your new password on all your devices.
                           </p>
                         </td>
                       </tr>
                     </table>

                     <!-- Warning Strip -->
                     <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
                       <tr>
                         <td style="background-color:#fef2f2;border-left:4px solid #dc2626;border-radius:4px;padding:14px 16px;">
                           <p style="margin:0 0 4px;color:#991b1b;font-size:13px;font-weight:600;">🚨 &nbsp;Was this not you?</p>
                           <p style="margin:0;color:#b91c1c;font-size:13px;line-height:1.7;">
                             If you did not make this change, your account may have been compromised.
                             Please contact our support team immediately at
                             <a href="mailto:support@guardianservices.in" style="color:#dc2626;font-weight:600;">support@guardianservices.in</a>
                             to secure your account right away.
                           </p>
                         </td>
                       </tr>
                     </table>

                     <p style="margin:0 0 8px;color:#6b7280;font-size:13px;line-height:1.7;">
                       If you have any concerns about the security of your account, do not hesitate to reach out to us.
                       We are here to help.
                     </p>
                   </td>
                 </tr>

                 <!-- Divider -->
                 <tr>
                   <td style="padding:0 40px;">
                     <hr style="border:none;border-top:1px solid #e5e7eb;margin:0;" />
                   </td>
                 </tr>

                 <!-- Footer -->
                 <tr>
                   <td style="background-color:#f8fafc;padding:24px 40px;text-align:center;">
                     <p style="margin:0 0 4px;color:#9ca3af;font-size:12px;">This is an automated message — please do not reply.</p>
                     <p style="margin:0;color:#9ca3af;font-size:12px;">
                       Need help? Contact us at
                       <a href="mailto:support@guardianservices.in" style="color:#4f46e5;text-decoration:none;">support@guardianservices.in</a>
                     </p>
                     <p style="margin:12px 0 0;color:#d1d5db;font-size:11px;">© 2026 Guardian Services. All rights reserved.</p>
                   </td>
                 </tr>

               </table>
             </td>
           </tr>
         </table>
       </body>
       </html>',
           'security@guardianservices.in',
           TRUE
       );


-- =====================================================
-- PASSWORD CHANGE SUCCESS — NULL Product (Admin / Super Admin)
-- =====================================================
INSERT INTO email_templates (template_name, product, subject, body, from_address, is_active)
VALUES (
           'PASSWORD_CHANGE_SUCCESS',
           NULL,
           'Administrator Password Successfully Changed – Guardian Services',
           '<!DOCTYPE html>
       <html>
       <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:Arial,sans-serif;">
         <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:40px 0;">
           <tr>
             <td align="center">
               <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                 <!-- Header -->
                 <tr>
                   <td style="background-color:#0a0f1e;padding:28px 40px;text-align:center;">
                     <h1 style="margin:0;color:#ffffff;font-size:20px;font-weight:600;letter-spacing:0.5px;">Guardian Services</h1>
                     <p style="margin:6px 0 0;color:#64748b;font-size:11px;letter-spacing:2px;text-transform:uppercase;">Enterprise Administration Portal</p>
                     <div style="margin:14px auto 0;width:48px;height:2px;background:linear-gradient(to right,#4f46e5,#7c3aed);border-radius:2px;"></div>
                   </td>
                 </tr>

                 <!-- Admin Badge -->
                 <tr>
                   <td style="background-color:#0f172a;padding:10px 40px 16px;text-align:center;">
                     <span style="display:inline-block;background-color:#1e293b;border:1px solid #334155;border-radius:20px;padding:4px 16px;">
                       <p style="margin:0;color:#94a3b8;font-size:11px;letter-spacing:1.5px;text-transform:uppercase;">🔐 &nbsp;Privileged Account — Confidential</p>
                     </span>
                   </td>
                 </tr>

                 <!-- Success Banner -->
                 <tr>
                   <td style="background-color:#f0fdf4;padding:20px 40px;text-align:center;border-bottom:1px solid #bbf7d0;">
                     <p style="margin:0;font-size:32px;">🔒</p>
                     <p style="margin:6px 0 0;color:#15803d;font-size:16px;font-weight:600;">Administrator Password Changed Successfully</p>
                     <p style="margin:4px 0 0;color:#6b7280;font-size:12px;">{{changedAt}}</p>
                   </td>
                 </tr>

                 <!-- Body -->
                 <tr>
                   <td style="padding:36px 40px 20px;">
                     <p style="margin:0 0 12px;color:#374151;font-size:15px;">Hello <strong>{{username}}</strong>,</p>
                     <p style="margin:0 0 8px;color:#6b7280;font-size:14px;line-height:1.7;">
                       This is a secure confirmation that the password for your
                       <strong style="color:#0f172a;">{{role}}</strong> account on the
                       Guardian Services Enterprise Portal was successfully changed.
                       This action was initiated directly from your account settings.
                     </p>
                     <p style="margin:0 0 24px;color:#6b7280;font-size:14px;line-height:1.7;">
                       All previously active sessions across all devices have been terminated
                       as a precautionary security measure.
                     </p>

                     <!-- Change Details Card -->
                     <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
                       <tr>
                         <td style="background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px 24px;">
                           <p style="margin:0 0 12px;color:#374151;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;">Change Summary</p>
                           <table width="100%" cellpadding="0" cellspacing="0">
                             <tr>
                               <td style="color:#6b7280;font-size:13px;padding:4px 0;">Account</td>
                               <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{email}}</td>
                             </tr>
                             <tr>
                               <td style="color:#6b7280;font-size:13px;padding:4px 0;">Role</td>
                               <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{role}}</td>
                             </tr>
                             <tr>
                               <td style="color:#6b7280;font-size:13px;padding:4px 0;">Changed At</td>
                               <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{changedAt}}</td>
                             </tr>
                             <tr>
                               <td style="color:#6b7280;font-size:13px;padding:4px 0;">IP Address</td>
                               <td style="color:#0f172a;font-size:13px;font-weight:600;text-align:right;">{{ipAddress}}</td>
                             </tr>
                           </table>
                         </td>
                       </tr>
                     </table>

                     <!-- High Privilege Warning -->
                     <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:16px;">
                       <tr>
                         <td style="background-color:#fef2f2;border-left:4px solid #dc2626;border-radius:4px;padding:14px 16px;">
                           <p style="margin:0 0 6px;color:#991b1b;font-size:13px;font-weight:600;">🚨 &nbsp;High-Privilege Account — Immediate Action Required if Unrecognized</p>
                           <p style="margin:0;color:#b91c1c;font-size:13px;line-height:1.7;">
                             If you did not initiate this password change, your privileged account may be under
                             unauthorized access. Contact the Guardian Services security team immediately at
                             <a href="mailto:security@guardianservices.in" style="color:#dc2626;font-weight:600;">security@guardianservices.in</a>
                             and request an emergency account suspension without delay.
                           </p>
                         </td>
                       </tr>
                     </table>

                     <!-- Security Reminder -->
                     <table width="100%" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
                       <tr>
                         <td style="background-color:#f0fdf4;border-left:4px solid #16a34a;border-radius:4px;padding:14px 16px;">
                           <p style="margin:0;color:#166534;font-size:13px;line-height:1.7;">
                             ✅ &nbsp;<strong>Best Practice Reminder:</strong> Ensure your new password is unique,
                             at least 12 characters long, and not reused from any other platform.
                             Enable MFA if not already active to further secure this privileged account.
                           </p>
                         </td>
                       </tr>
                     </table>

                     <p style="margin:0 0 8px;color:#6b7280;font-size:13px;line-height:1.7;">
                       This event has been recorded in the Guardian Services audit trail and is associated with
                       Request ID: <strong style="color:#0f172a;">{{requestId}}</strong>.
                     </p>
                   </td>
                 </tr>

                 <!-- Divider -->
                 <tr>
                   <td style="padding:0 40px;">
                     <hr style="border:none;border-top:1px solid #e5e7eb;margin:0;" />
                   </td>
                 </tr>

                 <!-- Footer -->
                 <tr>
                   <td style="background-color:#f8fafc;padding:24px 40px;text-align:center;">
                     <p style="margin:0 0 4px;color:#9ca3af;font-size:12px;">This message was intended for an authorized Guardian Services administrator.</p>
                     <p style="margin:0 0 4px;color:#9ca3af;font-size:12px;">This is an automated message — please do not reply directly to this email.</p>
                     <p style="margin:0;color:#9ca3af;font-size:12px;">
                       Security concerns? Reach us at
                       <a href="mailto:security@guardianservices.in" style="color:#7c3aed;text-decoration:none;">security@guardianservices.in</a>
                     </p>
                     <p style="margin:12px 0 0;color:#d1d5db;font-size:11px;">© 2026 Guardian Services. All rights reserved. &nbsp;|&nbsp; Enterprise Portal</p>
                   </td>
                 </tr>

               </table>
             </td>
           </tr>
         </table>
       </body>
       </html>',
           'security@guardianservices.in',
           TRUE
       );
-- End of fresh install schema