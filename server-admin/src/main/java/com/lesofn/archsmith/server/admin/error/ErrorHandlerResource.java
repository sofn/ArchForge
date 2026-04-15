package com.lesofn.archsmith.server.admin.error;

import com.lesofn.archsmith.common.error.exception.IErrorCodeException;
import com.lesofn.archsmith.common.error.system.SystemException;
import com.lesofn.archsmith.common.errors.SystemErrorCode;
import com.lesofn.archsmith.common.utils.GlobalConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        MediaType mediaType =
                (MediaType)
                        request.getAttribute(
                                "org.springframework.web.servlet.View.selectedContentType");
        int status = (int) request.getAttribute("jakarta.servlet.error.status_code");

        Exception exception =
                (Exception) request.getAttribute(GlobalExceptionHandler.GlobalExceptionAttribute);
        if (exception == null) {
            exception = (Exception) request.getAttribute("jakarta.servlet.error.exception");
        }
        IErrorCodeException apiException;
        String pageError = "500 - System error.";
        if (exception instanceof IErrorCodeException) {
            apiException = (IErrorCodeException) exception;
        } else if (status == 405) {
            apiException = new SystemException(SystemErrorCode.E_METHOD_ERROR);
        } else if (status == 404) {
            pageError = "404 - Page not Found: " + errorMsg;
            apiException = new SystemException(SystemErrorCode.E_API_NOT_EXIST);
        } else if (status == 415) {
            apiException =
                    new SystemException(
                            SystemErrorCode.E_UNSUPPORT_MEDIATYPE_ERROR, new Object[] {"unknow"});
        } else if (status >= 400 && status < 500) {
            apiException = new SystemException(SystemErrorCode.E_ILLEGAL_REQUEST, errorMsg);
        } else if (status == 503) {
            apiException = new SystemException(SystemErrorCode.E_SERVICE_UNAVAILABLE);
        } else {
            apiException = new SystemException(SystemErrorCode.E_DEFAULT);
            log.error(errorMsg, exception);
        }
        if (MediaType.TEXT_HTML.equals(mediaType)
                || Strings.CS.endsWithAny(path, GlobalConstants.staticResourceArray)) {
            return "<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "<head>\n"
                    + "    <title>"
                    + pageError
                    + "</title>\n"
                    + "</head>\n"
                    + "<body>\n"
                    + "<h2>"
                    + pageError
                    + "</h2>\n"
                    + "</body>\n"
                    + "</html>";
        } else {
            return apiException.getErrorInfo().getMsg() + " " + path;
        }
    }
}
