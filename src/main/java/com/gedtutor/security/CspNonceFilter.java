package com.gedtutor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates a fresh cryptographic nonce for every HTTP request and writes a
 * Content-Security-Policy header that includes that nonce.
 *
 * <p>The nonce is stored as a request attribute ({@value #NONCE_ATTRIBUTE}) so
 * that {@code CspNonceAdvice} can expose it to every Thymeleaf model under the
 * key {@code _cspNonce}.  Templates reference it with
 * {@code th:attr="nonce=${_cspNonce}"} on every inline {@code <script>} block.
 *
 * <p>External scripts loaded via {@code src=} do not need a nonce — they are
 * allowed by the origin allowlist in {@code script-src}.
 */
@Component
public class CspNonceFilter extends OncePerRequestFilter {

    /** Request attribute key — read by CspNonceAdvice and templates. */
    public static final String NONCE_ATTRIBUTE = "_cspNonce";

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String nonce = generateNonce();
        request.setAttribute(NONCE_ATTRIBUTE, nonce);

        // Set the CSP header before the response body is written.
        // Keeping it here (pre-chain) ensures it is present even on error pages.
        response.setHeader("Content-Security-Policy", buildCsp(nonce));

        chain.doFilter(request, response);
    }

    private static String generateNonce() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String buildCsp(String nonce) {
        return  "default-src 'self'; " +
                // Inline scripts are only allowed when they carry the matching nonce.
                // External scripts from our CDN (KaTeX) are allowed by origin.
                "script-src 'self' 'nonce-" + nonce + "' https://cdn.jsdelivr.net; " +
                // 'unsafe-inline' for styles is required by KaTeX's runtime rendering.
                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                "font-src 'self' https://cdn.jsdelivr.net; " +
                "img-src 'self' data:; " +
                // Allow YouTube and Vimeo iframes for the video player.
                "frame-src https://www.youtube.com https://player.vimeo.com; " +
                // Prevent this app from being framed by other origins (clickjacking).
                "frame-ancestors 'self'; " +
                "object-src 'none';";
    }
}
