package com.lesofn.matrixboot.frame.database;

import com.lesofn.matrixboot.frame.context.RequestContext;
import com.lesofn.matrixboot.frame.context.ThreadLocalContext;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-10-13 15:27
 */
@Aspect
@Service
public class DataBaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(DataBaseAdvisor.class);

    private String[] writeMethodPrefixs = new String[]{"add", "insert", "delete", "remove", "save", "update", "change", "modify"};
    private String[] queryMethodPrefixs = new String[]{"query", "select", "get", "list", "find", "exists", "count"};

    @Before("execution(* com.lesofn.matrixboot..*Dao.*(..))")
    public void beforMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        RequestContext rc = ThreadLocalContext.getRequestContext();

        if (StringUtils.startsWithAny(methodName, writeMethodPrefixs)) {
            rc.setShouldReadMasterDB(true);
        } else if (StringUtils.startsWithAny(methodName, queryMethodPrefixs)) {
            rc.setShouldReadMasterDB(false);
        } else {
            log.warn("cannot found handle db method for methodName is: " + methodName);
            rc.setShouldReadMasterDB(true);
        }
    }
}
