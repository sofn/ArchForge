package com.lesofn.matrixboot.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sofn
 */
public class EngineException extends RuntimeException {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineException.class);

    private final Map<String, Object> parameters = new HashMap<>();
    private ExcepFactor factor;
    private static final String ERROR_FORMAT = "{\"error\":\"%s\",\"error_zh_CN\":\"%s\",\"error_code\":%s,\"request\":\"%s\"}";
    private String errorMsgCn;

    private static final long serialVersionUID = -6705744099814945437L;

    protected EngineException(ExcepFactor factor) {
        this(factor, factor.getErrorMsg(), factor.getErrorMsgCn());
    }

    protected EngineException(ExcepFactor factor, Object message, String errorMsgCn) {
        super(message == null ? factor.getErrorMsg() : message.toString());
        this.factor = factor;
        this.errorMsgCn = errorMsgCn;
    }

    protected EngineException(ExcepFactor factor, Throwable exception, String errorMsgCn) {
        super(exception);
        this.factor = factor;
        this.errorMsgCn = errorMsgCn;
    }

    protected EngineException(ExcepFactor factor, Object[] args) {
        this(factor, factor.getErrorMsg(args), factor.getErrorMsgCn(args));
    }

    protected EngineException(ExcepFactor factor, Object[] errorArgs, Object[] errorCnArgs) {
        this(factor, factor.getErrorMsg(errorArgs), factor.getErrorMsgCn(errorCnArgs));
    }

    // FIXME 默认异常不应该输出详细错误信息？
    protected EngineException(Exception e) {
        this(ExcepFactor.E_DEFAULT, e.getMessage());
    }

    protected EngineException(String message) {
        this(ExcepFactor.E_DEFAULT, message);
    }

    protected EngineException(ExcepFactor factor, Object message) {
        this(factor, message, factor.getErrorMsgCn());
    }

    protected EngineException(ExcepFactor factor, Throwable exception) {
        this(factor, exception, factor.getErrorMsgCn());
    }

    public void setTraceHeader(String name, Object value) {
        getTraceHeaders().put(name, value);
    }

    public Map<String, Object> getTraceHeaders() {
        return parameters;
    }

    public ExcepFactor getFactor() {
        return factor;
    }

    public String getErrorMsgCn() {
        return this.errorMsgCn;
    }

    public void setErrorMsgCn(String errorMsgCn) {
        this.errorMsgCn = errorMsgCn;
    }


    /**
     * 只供构造对象时使用
     *
     * @param factor
     */
    protected void setFactor(ExcepFactor factor) {
        this.factor = factor;
    }

    public String formatException(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path argument is null");
        }
        return String.format(ERROR_FORMAT, this.getMessage(), this.getErrorMsgCn(), this.getFactor().getErrorCode(), path);
    }

    public void formatException(String path, Writer writer) {
        String result = this.formatException(path);
        try {
            writer.write(result);
        } catch (IOException e) {
            LOGGER.error("formatException", e);
        }
    }
}
