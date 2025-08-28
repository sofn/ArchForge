package com.lesofn.matrixboot.deploy.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;
import java.io.File;

/**
 * Multipart configuration for handling file uploads
 */
@Configuration
public class MultipartConfig {

    /**
     * Configure the multipart resolver for handling file uploads
     */
    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        return resolver;
    }

    /**
     * Configure multipart settings with proper upload limits
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Set maximum file size (10MB)
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        // Set maximum request size (20MB)
        factory.setMaxRequestSize(DataSize.ofMegabytes(20));
        
        // Set upload temp directory
        String tempDir = System.getProperty("java.io.tmpdir");
        File uploadDirectory = new File(tempDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
        factory.setLocation(tempDir);
        
        return factory.createMultipartConfig();
    }
}
