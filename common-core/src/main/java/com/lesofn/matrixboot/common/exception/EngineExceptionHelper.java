package com.lesofn.matrixboot.common.exception;

/**
 * Engine exception helper for make local or remote excetion.
 *
 * @author fishermen
 * @version V1.0 created at: 2013-2-1
 */

public class EngineExceptionHelper {

    /**
     * 为前端直接构造的异常
     *
     * @param factor
     * @return
     */
    public static EngineException localException(ExcepFactor factor) {
        return new EngineException(factor);
    }

    /**
     * 为前端直接构造的异常
     *
     * @param factor
     * @param message
     * @return
     */
    public static EngineException localException(ExcepFactor factor, Object message) {
        return new EngineException(factor, message);
    }

    public static EngineException localException(ExcepFactor factor, Throwable exception) {
        return new EngineException(factor, exception);
    }

    /**
     * 为前端直接构造的异常
     */
    public static EngineException localException(ExcepFactor factor, Object[] args) {
        return new EngineException(factor, args);
    }

    /**
     *  用来重设英文和中文提示
     */
    public static EngineException localException(ExcepFactor factor, Object[] args, Object[] argsCn) {
        return new EngineException(factor, args, argsCn);
    }
}
