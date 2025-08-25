package com.lesofn.matrixboot.deploy.service.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务类
 * 
 * @author lesofn
 */
@Service
public class RedisCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public final LoginUserCache loginUserCache = new LoginUserCache();

    /**
     * 登录用户缓存类
     */
    public class LoginUserCache {
        
        private static final String LOGIN_USER_KEY_PREFIX = "login_user:";
        private static final long LOGIN_USER_EXPIRE_TIME = 30; // 30分钟

        /**
         * 设置登录用户缓存
         * 
         * @param key 缓存key
         * @param loginUser 登录用户信息
         */
        public void set(String key, Object loginUser) {
            redisTemplate.opsForValue().set(LOGIN_USER_KEY_PREFIX + key, loginUser, LOGIN_USER_EXPIRE_TIME, TimeUnit.MINUTES);
        }

        /**
         * 根据key获取登录用户缓存
         * 
         * @param key 缓存key
         * @return 登录用户信息
         */
        public Object getObjectOnlyInCacheById(String key) {
            return redisTemplate.opsForValue().get(LOGIN_USER_KEY_PREFIX + key);
        }

        /**
         * 删除登录用户缓存
         * 
         * @param key 缓存key
         */
        public void delete(String key) {
            redisTemplate.delete(LOGIN_USER_KEY_PREFIX + key);
        }
    }
}