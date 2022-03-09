package com.lesofn.appengine.deploy;

import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.Filter;

/**
 * Authors: sofn
 * Version: 1.0  Created at 15-6-8 23:15.
 */
@Configuration
public class SiteMeshConfig extends WebMvcConfigurerAdapter {

    @Bean
    public FilterRegistrationBean siteMeshFilterChain() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        Filter headerFilter = new ConfigurableSiteMeshFilter();
        registration.setFilter(headerFilter);
        registration.setOrder(1);
        return registration;
    }

}
