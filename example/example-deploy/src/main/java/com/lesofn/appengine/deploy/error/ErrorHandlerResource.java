package com.lesofn.appengine.deploy.error;

import com.lesofn.appengine.common.exception.EngineException;
import com.lesofn.appengine.common.exception.EngineExceptionHelper;
import com.lesofn.appengine.common.exception.ExcepFactor;
import com.lesofn.appengine.common.utils.GlobalConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 404处理
 *
 * @author sofn
 * @version 1.0 Created at: 2015-04-29 16:19
 */
@RestController
public class ErrorHandlerResource implements ErrorController {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandlerResource.class);

    public static final String ERROR_PATH = "/error";

    @RequestMapping(value = ERROR_PATH)
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public String error(HttpServletRequest request) {
        String path = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        String errorMsg = (String) request.getAttribute("jakarta.servlet.error.message");
        MediaType mediaType = (MediaType) request.getAttribute("org.springframework.web.servlet.View.selectedContentType");
        int status = (int) request.getAttribute("jakarta.servlet.error.status_code");

        Exception exception = (Exception) request.getAttribute(GlobalExceptionHandler.GlobalExceptionAttribute);
        if (exception == null) {
            exception = (Exception) request.getAttribute("jakarta.servlet.error.exception");
        }
        EngineException apiException;
        String pageError = "500 - System error.";
        if (exception != null && exception instanceof EngineException) {
            apiException = (EngineException) exception;
        } else if (status == 405) {
            apiException = EngineExceptionHelper.localException(ExcepFactor.E_METHOD_ERROR);
        } else if (status == 404) {
            pageError = "404 - Page not Found: " + errorMsg;
            apiException = EngineExceptionHelper.localException(ExcepFactor.E_API_NOT_EXIST);
        } else if (status == 415) {
            apiException = EngineExceptionHelper.localException(ExcepFactor.E_UNSUPPORT_MEDIATYPE_ERROR, new Object[]{"unknow"});
        } else if (status >= 400 && status < 500) {
            apiException = EngineExceptionHelper.localException(ExcepFactor.E_ILLEGAL_REQUEST, errorMsg);
        } else if (status == 503) {
            apiException = EngineExceptionHelper.localException(ExcepFactor.E_SERVICE_UNAVAILABLE);
        } else {
            apiException = EngineExceptionHelper.localException(ExcepFactor.E_DEFAULT);
            log.error(errorMsg, exception);
        }
        if (MediaType.TEXT_HTML.equals(mediaType) || StringUtils.endsWithAny(path, GlobalConstants.staticResourceArray)) {
            return "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>" + pageError + "</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h2>" + pageError + "</h2>\n" +
                    "</body>\n" +
                    "</html>";
        } else {
            return apiException.formatException(path);
        }
    }
}
