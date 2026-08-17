package com.gedtutor.web;

import com.gedtutor.security.CspNonceFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Exposes the per-request CSP nonce to every Thymeleaf template model.
 *
 * <p>{@link CspNonceFilter} stores the nonce in the request attribute
 * {@link CspNonceFilter#NONCE_ATTRIBUTE} before the controller runs.
 * This advice reads it and adds it to every model under the key
 * {@code _cspNonce}, making it available as {@code ${_cspNonce}} in templates.
 *
 * <p>Usage in templates:
 * <pre>{@code
 * <script th:attr="nonce=${_cspNonce}">
 *   // inline JS here
 * </script>
 * }</pre>
 */
@ControllerAdvice
public class CspNonceAdvice {

    @ModelAttribute("_cspNonce")
    public String cspNonce(HttpServletRequest request) {
        Object nonce = request.getAttribute(CspNonceFilter.NONCE_ATTRIBUTE);
        return nonce != null ? nonce.toString() : "";
    }
}
