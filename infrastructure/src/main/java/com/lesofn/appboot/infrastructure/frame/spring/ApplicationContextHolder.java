package com.lesofn.appboot.infrastructure.frame.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 建议如下形式获取
 * ServletContext servletContext = request.getSession().getServletContext();
 * ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
 *
 * @author sofn
 */
@Service
@Lazy(false)
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * 将该对象中的带有Autowired annotation的属性自动注入
     */
    public static void autowireBean(Object obj) {
        if (context != null) {
            AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
            factory.autowireBean(obj);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) context.getBean(name);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        String[] names = context.getBeanNamesForType(clazz);
        if (names == null || names.length == 0) {
            return null;
        }
        return (T) context.getBean(names[0]);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getBeans(Class<T> clazz) {
        List<T> ret = new ArrayList<>();
        if (context == null)
            return ret;
        String[] names = context.getBeanNamesForType(clazz);
        if (names == null || names.length == 0) {
            return ret;
        }
        for (String name : names) {
            ret.add((T) context.getBean(name));
        }
        return ret;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        context = applicationContext;
    }
}
