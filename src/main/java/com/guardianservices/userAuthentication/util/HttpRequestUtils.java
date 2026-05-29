package com.guardianservices.userAuthentication.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Utility class for extracting the real client IP address from an
 * incoming HTTP request, with support for reverse proxies and load balancers.
 */
@Component
public class HttpRequestUtils {

    /**
     * Headers to check in order of priority.
     * Proxies and load balancers typically forward the original client IP
     * in one of these headers before falling back to RemoteAddr.
     */
    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "REMOTE_ADDR"
    };
    /**
     * Extracts the real client IP address from the given HTTP request.
     *
     * <p>This method iterates through a prioritized list of well-known proxy
     * headers (e.g., {@code X-Forwarded-For}, {@code X-Real-IP}) before
     * falling back to {@link HttpServletRequest#getRemoteAddr()}.
     *
     * <p>When the {@code X-Forwarded-For} header contains a chain of IPs
     * (e.g., {@code client, proxy1, proxy2}), only the first (leftmost) IP
     * is returned, as that represents the original client.
     *
     * <p><strong>Security note:</strong> Headers like {@code X-Forwarded-For}
     * can be spoofed by clients if your infrastructure does not strip or
     * overwrite them. Ensure your reverse proxy is configured to always
     * overwrite these headers with the verified client IP.
     *
     * @param request the incoming {@link HttpServletRequest}
     * @return the resolved client IP address as a {@link String};
     *         never {@code null}
     */
    public String getClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ipAddress = request.getHeader(header);
            if (isValidIp(ipAddress)) {
                // X-Forwarded-For can contain a chain: "client, proxy1, proxy2"
                // The first entry is always the original client IP
                return ipAddress.contains(",")
                        ? ipAddress.split(",")[0].trim()
                        : ipAddress.trim();
            }
        }
        // Final fallback — direct connection IP
        return request.getRemoteAddr();
    }
    /**
     * Validates whether the given IP string is non-null, non-empty,
     * and not the literal string {@code "unknown"} (returned by some proxies).
     *
     * @param ip the IP string candidate to validate
     * @return {@code true} if the IP value is usable; {@code false} otherwise
     */
    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
