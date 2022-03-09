package com.lesofn.appengine.frame.spring;

import com.alibaba.druid.support.http.StatViewServlet;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

@Configuration
public class DruidConfig implements ServletContextInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		ServletRegistration.Dynamic druidServlet = servletContext.addServlet("druid", new StatViewServlet());
		druidServlet.addMapping("/druid/*");
		druidServlet.setLoadOnStartup(1);
	}

}
