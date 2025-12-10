package com.example.quizapp.controller;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController {

    private static final Logger log = LoggerFactory.getLogger(LogoutController.class);

    /**
     * Serve a simple logout confirmation page.
     * Accepts both /logout and /Logout to avoid casing issues.
     * This method is intentionally minimal to avoid runtime errors.
     */
    @GetMapping({"/logout", "/Logout"})
    public String showLogoutConfirmation(HttpSession session) {
        try {
            // Don't depend on session attributes here — keep it safe and simple.
            return "logout"; // resolves to templates/logout.html
        } catch (Exception e) {
            log.error("Error rendering logout confirmation page", e);
            // Safe fallback: redirect to login if anything goes wrong
            return "redirect:/login";
        }
    }

    /**
     * Perform the actual logout action (invalidate session).
     * Mapping accepts both /do-logout and /doLogout.
     */
    @GetMapping({"/do-logout", "/doLogout"})
    public String doLogout(HttpSession session) {
        try {
            if (session != null) session.invalidate();
        } catch (Exception e) {
            log.warn("Error invalidating session during logout", e);
        }
        return "redirect:/login";
    }
}
