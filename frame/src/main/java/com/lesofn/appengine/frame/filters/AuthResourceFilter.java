package com.lesofn.appengine.frame.filters;

import com.lesofn.appengine.auth.annotation.BaseInfo;
import com.lesofn.appengine.auth.model.AuthException;
import com.lesofn.appengine.auth.model.AuthRequest;
import com.lesofn.appengine.auth.model.AuthResponse;
import com.lesofn.appengine.auth.service.AuthService;
import com.lesofn.appengine.common.context.ClientVersion;
import com.lesofn.appengine.common.utils.GlobalConstants;
import com.lesofn.appengine.frame.context.RequestContext;
import com.lesofn.appengine.frame.context.ThreadLocalContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author sofn
 */
@Service
public class AuthResourceFilter extends RequestMappingHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResourceFilter.class);

    @Resource(name = "defaultAuthService")
    private AuthService authService;
    @Value("${profile}")
    private String profile;

    @Override
    protected ModelAndView handleInternal(HttpServletRequest request, HttpServletResponse response,
                                          HandlerMethod handlerMethod) throws Exception {
        if (StringUtils.equals(request.getRequestURI(), "/error")
                || StringUtils.startsWith(request.getRequestURI(), "/swagger-resources")
                || StringUtils.endsWithAny(request.getRequestURI(), GlobalConstants.staticResourceArray)
                || !StringUtils.equals(profile, "prod")) {
            return super.handleInternal(request, response, handlerMethod);
        }

        RequestContext context = ThreadLocalContext.getRequestContext();
        context.setOriginRequest(request);

        AuthRequest authRequest = new AuthRequest(request);

        Method method = handlerMethod.getMethod();
        BaseInfo baseInfo = null;
        if (method.isAnnotationPresent(BaseInfo.class)) {
            baseInfo = method.getAnnotation(BaseInfo.class);
        }

        AuthResponse authResponse;
        try {
            authResponse = authService.auth(authRequest, Optional.ofNullable(baseInfo));
        } catch (AuthException e) {
            LOGGER.debug("auth failed! path: " + request.getRequestURI() + " appId: " + request.getHeader(AuthService.ENGINE_APPID_HEADER)
                    + " version: " + ClientVersion.valueOf(request.getHeader(ClientVersion.VERSION_HEADER)));
            throw e;
        }
        context.setCurrentUid(authResponse.getUid());
        context.setAppId(authResponse.getAppId());
        context.setOfficialApp(authResponse.getAppId() == GlobalConstants.DEFAULT_APPID);
        context.setIp(authResponse.getIp());
        context.setPlatform(authResponse.getPlatform());
        context.setAttribute("auth_type", authResponse.getAuthedBy());
        context.setClientVersion(authResponse.getClientVersion());

        return super.handleInternal(request, response, handlerMethod);
    }
}
