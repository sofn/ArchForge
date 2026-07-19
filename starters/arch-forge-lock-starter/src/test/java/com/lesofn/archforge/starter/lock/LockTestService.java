package com.lesofn.archforge.starter.lock;

import com.lesofn.archforge.starter.lock.annotation.DistributedLock;
import org.springframework.stereotype.Service;

@Service
public class LockTestService {

    @DistributedLock(name = "archforge:test:lock:aspect-#{#name}", waitTime = 3, leaseTime = 10)
    public String greet(String name) {
        return "hello " + name;
    }
}
