package com.app.musicstore.service;

import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service to handle user retrieval with session-based caching to prevent context mixing
 */
@Service
public class SessionUserService {
    
    private static final String SESSION_USER_KEY = "CACHED_SESSION_USER";
    private static final String SESSION_USER_EMAIL_KEY = "CACHED_USER_EMAIL";
    
    /**
     * Get authenticated user with session caching to prevent context mixing
     */
    public User getAuthenticatedUser(HttpSession session) {
        String sessionId = session.getId();
        
        // First, try to get from current authentication context
        User currentUser = getCurrentAuthenticatedUser();
        
        if (currentUser != null) {
            // Check if this is the same user as cached in session
            String cachedUserEmail = (String) session.getAttribute(SESSION_USER_EMAIL_KEY);
            
            if (cachedUserEmail != null && !cachedUserEmail.equals(currentUser.getEmail())) {
                // Different user logged in - return cached user to prevent context mixing
                User cachedUser = (User) session.getAttribute(SESSION_USER_KEY);
                if (cachedUser != null) {
                    System.out.println("🚨 SESSION MIXING PREVENTED! Session " + sessionId.substring(0, 8) + "... - Returning cached user: " + cachedUser.getEmail() 
                                     + " instead of current: " + currentUser.getEmail());
                    return cachedUser;
                }
            }
            
            // Cache the current user in session if not already cached or different
            if (cachedUserEmail == null || !cachedUserEmail.equals(currentUser.getEmail())) {
                session.setAttribute(SESSION_USER_KEY, currentUser);
                session.setAttribute(SESSION_USER_EMAIL_KEY, currentUser.getEmail());
                System.out.println("✅ Cached user in session " + sessionId.substring(0, 8) + "...: " + currentUser.getEmail());
            }
            
            return currentUser;
        }
        
        // If no current user, try to get from session cache
        User cachedUser = (User) session.getAttribute(SESSION_USER_KEY);
        if (cachedUser != null) {
            System.out.println("📋 No current authentication, returning cached user from session " + sessionId.substring(0, 8) + "...: " + cachedUser.getEmail());
            return cachedUser;
        }
        
        System.out.println("❌ No authenticated user found for session " + sessionId.substring(0, 8) + "...");
        return null;
    }
    
    /**
     * Get current authenticated user from Spring Security context
     */
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser();
        }
        
        return null;
    }
    
    /**
     * Clear cached user from session (call on logout)
     */
    public void clearCachedUser(HttpSession session) {
        String sessionId = session.getId();
        String cachedEmail = (String) session.getAttribute(SESSION_USER_EMAIL_KEY);
        
        session.removeAttribute(SESSION_USER_KEY);
        session.removeAttribute(SESSION_USER_EMAIL_KEY);
        
        System.out.println("🧹 Cleared cached user from session " + sessionId.substring(0, 8) + "...: " + 
                          (cachedEmail != null ? cachedEmail : "none"));
    }
    
    /**
     * Force refresh cached user with current authentication
     */
    public void refreshCachedUser(HttpSession session) {
        String sessionId = session.getId();
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser != null) {
            session.setAttribute(SESSION_USER_KEY, currentUser);
            session.setAttribute(SESSION_USER_EMAIL_KEY, currentUser.getEmail());
            System.out.println("🔄 Refreshed cached user in session " + sessionId.substring(0, 8) + "...: " + currentUser.getEmail());
        } else {
            System.out.println("⚠️ Could not refresh cached user - no current authentication for session " + sessionId.substring(0, 8) + "...");
        }
    }
}
