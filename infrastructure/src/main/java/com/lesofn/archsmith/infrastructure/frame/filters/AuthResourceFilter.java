package com.lesofn.archsmith.infrastructure.frame.filters;

import com.lesofn.archsmith.common.context.ClientVersion;
import com.lesofn.archsmith.common.utils.GlobalConstants;
import com.lesofn.archsmith.infrastructure.auth.annotation.BaseInfo;
import com.lesofn.archsmith.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archsmith.infrastructure.auth.model.AuthRequest;
import com.lesofn.archsmith.infrastructure.auth.model.AuthResponse;
import com.lesofn.archsmith.infrastructure.auth.service.AuthService;
import com.lesofn.archsmith.infrastructure.frame.context.RequestContext;
import com.lesofn.archsmith.infrastructure.frame.context.ThreadLocalContext;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Optional;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * @author sofn
 */
// @Service
public class AuthResourceFilter extends RequestMappingHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResourceFilter.class);

    @Resource(name = "defaultAuthService")
    private AuthService authService;

    @Value("${spring.profiles.active}")
    private String profile;

    @Override
    protected ModelAndView handleInternal(
            HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod)
            throws Exception {
        if (Strings.CS.equals(request.getRequestURI(), "/error")
                || Strings.CS.startsWith(request.getRequestURI(), "/swagger-resources")
                || Strings.CS.startsWith(request.getRequestURI(), "/v3/api-docs")
                || Strings.CS.startsWith(request.getRequestURI(), "/swagger-ui")
                || Strings.CS.equals(request.getRequestURI(), "/swagger-ui.html")
                || Strings.CS.endsWithAny(
                        request.getRequestURI(), GlobalConstants.staticResourceArray)
                || !Strings.CS.equals(profile, "prod")) {
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
        } catch (AdminAuthException e) {
            LOGGER.debug(
                    "auth failed! path: "
                            + request.getRequestURI()
                            + " appId: "
                            + request.getHeader(AuthService.ENGINE_APPID_HEADER)
                            + " version: "
                            + ClientVersion.valueOf(
                                    request.getHeader(ClientVersion.VERSION_HEADER)));
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
