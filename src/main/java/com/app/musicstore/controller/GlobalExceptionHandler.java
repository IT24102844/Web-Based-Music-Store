package com.app.musicstore.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        System.err.println("File upload size exceeded: " + e.getMessage());
        
        String requestURI = request.getRequestURI();
        String errorMessage = "File size exceeds the maximum allowed limit. Please reduce file size and try again.";
        
        if (requestURI.contains("/seller/products/add")) {
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/seller/products/add";
        } else if (requestURI.contains("/seller/products/edit/")) {
            String[] parts = requestURI.split("/");
            String productId = parts[parts.length - 1];
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/seller/products/edit/" + productId;
        } else if (requestURI.contains("/seller/settings")) {
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/seller/settings";
        } else {
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/";
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleValidationException(IllegalArgumentException e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        System.err.println("Validation error: " + e.getMessage());
        
        String requestURI = request.getRequestURI();
        String errorMessage = e.getMessage();
        
        if (requestURI.contains("/seller/products/add")) {
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/seller/products/add";
        } else if (requestURI.contains("/seller/products/edit/")) {
            String[] parts = requestURI.split("/");
            String productId = parts[parts.length - 1];
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/seller/products/edit/" + productId;
        } else if (requestURI.contains("/seller/settings")) {
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/seller/settings";
        } else if (requestURI.contains("/register")) {
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/users/register";
        } else {
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/";
        }
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        System.err.println("Global exception handler caught: " + e.getMessage());
        e.printStackTrace();
        
        String requestURI = request.getRequestURI();
        
        if (requestURI.contains("/register")) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/users/register";
        } else if (requestURI.contains("/login")) {
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/users/login";
        } else if (requestURI.contains("/seller/products/add")) {
            redirectAttributes.addFlashAttribute("error", "Failed to add product: " + e.getMessage());
            return "redirect:/seller/products/add";
        } else if (requestURI.contains("/seller/products/edit/")) {
            // Extract product ID from URL for edit redirect
            String[] parts = requestURI.split("/");
            String productId = parts[parts.length - 1];
            redirectAttributes.addFlashAttribute("error", "Failed to update product: " + e.getMessage());
            return "redirect:/seller/products/edit/" + productId;
        } else if (requestURI.contains("/seller/settings")) {
            redirectAttributes.addFlashAttribute("error", "Failed to update settings: " + e.getMessage());
            return "redirect:/seller/settings";
        } else if (requestURI.contains("/seller/")) {
            redirectAttributes.addFlashAttribute("error", "Seller operation failed: " + e.getMessage());
            return "redirect:/seller/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
            return "redirect:/";
        }
    }
}
