package com.app.musicstore.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SessionDebugController {

    @Autowired
    private SessionRegistry sessionRegistry;

    @GetMapping("/api/session-status")
    @ResponseBody
    public Map<String, Object> getSessionStatus(HttpSession session) {
        Map<String, Object> status = new HashMap<>();
        
        // Basic session info
        status.put("sessionId", session.getId());
        status.put("isNew", session.isNew());
        status.put("maxInactiveInterval", session.getMaxInactiveInterval());
        
        // Authentication info
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            status.put("authenticated", true);
            status.put("username", auth.getName());
            status.put("authorities", auth.getAuthorities().stream()
                    .map(a -> a.getAuthority()).toList());
        } else {
            status.put("authenticated", false);
        }
        
        // Session registry info
        status.put("totalActiveSessions", sessionRegistry.getAllPrincipals().size());
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}
