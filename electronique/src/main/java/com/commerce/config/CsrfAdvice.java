package com.commerce.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CsrfAdvice {
    
    @ModelAttribute("_csrf")
    public CsrfToken csrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        if (token == null) {
            token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        }
        return token;
    }
}