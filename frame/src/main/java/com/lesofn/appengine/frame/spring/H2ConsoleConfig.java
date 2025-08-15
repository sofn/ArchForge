package com.lesofn.appengine.frame.spring;

import jakarta.servlet.Servlet;
import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class H2ConsoleConfig   {

    @Bean
    public ServletRegistrationBean<Servlet> h2ConsoleServlet() {
        JakartaWebServlet servlet = new JakartaWebServlet();
        ServletRegistrationBean<Servlet> registrationBean = new ServletRegistrationBean<>(servlet, "/h2/*");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }

}
