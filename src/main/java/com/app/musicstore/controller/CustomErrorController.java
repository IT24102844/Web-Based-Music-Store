package com.app.musicstore.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorTitle", "Page Not Found");
                model.addAttribute("errorMessage", "The page you are looking for does not exist.");
                model.addAttribute("errorCode", "404");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorTitle", "Internal Server Error");
                model.addAttribute("errorMessage", "Something went wrong on our end. Please try again later.");
                model.addAttribute("errorCode", "500");
            } else {
                model.addAttribute("errorTitle", "Error");
                model.addAttribute("errorMessage", "An error occurred. Please try again.");
                model.addAttribute("errorCode", statusCode.toString());
            }
        } else {
            model.addAttribute("errorTitle", "Error");
            model.addAttribute("errorMessage", "An unexpected error occurred.");
            model.addAttribute("errorCode", "Unknown");
        }
        
        return "error";
    }
}