-- =====================================================
-- PASSWORD CHANGE SUCCESS — Product (AI Log Analyzer)
-- =====================================================
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