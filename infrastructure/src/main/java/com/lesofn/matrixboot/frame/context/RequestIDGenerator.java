package com.lesofn.matrixboot.frame.context;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author sofn
 */
public class RequestIDGenerator {

    private final AtomicLong requestId = new AtomicLong(1);
    private String hostName;

    static class InstanceHolder {
        static RequestIDGenerator idGenerator = new RequestIDGenerator();
    }

    private RequestIDGenerator() {
        try {
            // 取machineName
            this.hostName = java.net.InetAddress.getLocalHost().getHostName();
            int idx = this.hostName.indexOf('.');
            if (idx > 0) {
                this.hostName = this.hostName.substring(0, idx);
            }
        } catch (UnknownHostException e) {
            hostName = "localhost";
        }
    }

    public static RequestIDGenerator getInstance() {
        return InstanceHolder.idGenerator;
    }

    public String nextId() {
        return this.hostName + "-" + requestId.getAndIncrement();
    }

}
