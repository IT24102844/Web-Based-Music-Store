package com.app.musicstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files from the external uploads directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        System.out.println("🔧 WebConfig: Serving /uploads/** from file:uploads/");
    }
}