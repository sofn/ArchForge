package com.lesofn.archforge.infrastructure.frame.interceptor;

import com.lesofn.archforge.common.error.SystemErrorCode;
import com.lesofn.archforge.common.utils.i18n.MessageUtils;
import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import com.lesofn.archforge.infrastructure.annotation.RepeatSubmit;
import com.lesofn.archforge.infrastructure.config.ArchForgeConfig;
import com.lesofn.archforge.infrastructure.db.redis.RedisUtil;
import com.lesofn.archforge.infrastructure.frame.filter.RepeatableRequestWrapper;
import com.lesofn.archforge.infrastructure.frame.response.model.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepeatSubmitInterceptor implements HandlerInterceptor {

    private static final String REPEAT_PARAMS = "repeatParams";
    private static final String REPEAT_TIME = "repeatTime";
    private static final String REPEAT_SUBMIT_KEY = "repeat:submit:";

    private final RedisUtil redisUtil;
    private final ArchForgeConfig archForgeConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            RepeatSubmit annotation = handlerMethod.getMethodAnnotation(RepeatSubmit.class);
            if (annotation != null && isRepeatSubmit(request, annotation)) {
                String message = MessageUtils.messageOrDefault(annotation.message(), annotation.message());
                renderJson(response, ResponseResult.error(SystemErrorCode.E_DUPLICATE_REQUEST.getCode(), message));
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation) throws IOException {
        String nowParams;
        if (request instanceof RepeatableRequestWrapper) {
            nowParams = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } else {
            nowParams = JsonUtil.to(request.getParameterMap());
        }

        Map<String, Object> nowDataMap = new HashMap<>();
        nowDataMap.put(REPEAT_PARAMS, nowParams);
        nowDataMap.put(REPEAT_TIME, System.currentTimeMillis());

        String url = request.getRequestURI();
        String submitKey = StringUtils.trimToEmpty(request.getHeader(archForgeConfig.getToken().getHeader()));
        String cacheRepeatKey = REPEAT_SUBMIT_KEY + url + submitKey;

        Object cacheObject = redisUtil.getCacheObject(cacheRepeatKey);
        if (cacheObject instanceof Map<?, ?> sessionMap) {
            Object pre = sessionMap.get(url);
            if (pre instanceof Map<?, ?>) {
                Map<String, Object> preDataMap = (Map<String, Object>) pre;
                if (compareParams(nowDataMap, preDataMap) && compareTime(nowDataMap, preDataMap, annotation.interval())) {
                    return true;
                }
            }
        }

        Map<String, Object> cacheMap = new HashMap<>();
        cacheMap.put(url, nowDataMap);
        redisUtil.setCacheObject(cacheRepeatKey, cacheMap, annotation.interval(), TimeUnit.MILLISECONDS);
        return false;
    }

    private boolean compareParams(Map<String, Object> nowMap, Map<String, Object> preMap) {
        String nowParams = (String) nowMap.get(REPEAT_PARAMS);
        String preParams = (String) preMap.get(REPEAT_PARAMS);
        return nowParams.equals(preParams);
    }

    private boolean compareTime(Map<String, Object> nowMap, Map<String, Object> preMap, int interval) {
        long time1 = ((Number) nowMap.get(REPEAT_TIME)).longValue();
        long time2 = ((Number) preMap.get(REPEAT_TIME)).longValue();
        return (time1 - time2) < interval;
    }

    private void renderJson(HttpServletResponse response, ResponseResult<?> result) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JsonUtil.to(result));
    }
}
