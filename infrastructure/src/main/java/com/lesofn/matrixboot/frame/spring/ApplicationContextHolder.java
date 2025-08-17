package com.lesofn.matrixboot.frame.spring;

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

    private static ApplicationContext _context;

    public static ApplicationContext getApplicatioinContext() {
        return _context;
    }

    /**
     * 将该对象中的带有Autowired annotation的属性自动注入
     */
    public static void autowireBean(Object obj) {
        if (_context != null) {
            AutowireCapableBeanFactory factory = _context.getAutowireCapableBeanFactory();
            factory.autowireBean(obj);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) _context.getBean(name);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        String[] names = _context.getBeanNamesForType(clazz);
        if (names == null || names.length == 0) {
            return null;
        }
        return (T) _context.getBean(names[0]);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getBeans(Class<T> clazz) {
        List<T> ret = new ArrayList<>();
        if (_context == null)
            return ret;
        String[] names = _context.getBeanNamesForType(clazz);
        if (names == null || names.length == 0) {
            return ret;
        }
        for (String name : names) {
            ret.add((T) _context.getBean(name));
        }
        return ret;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        _context = applicationContext;
    }
}
