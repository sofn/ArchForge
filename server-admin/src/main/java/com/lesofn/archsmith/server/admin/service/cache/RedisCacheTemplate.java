package com.lesofn.archsmith.server.admin.service.cache;

import com.lesofn.archsmith.infrastructure.db.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存接口实现类 三级缓存
 *
 * @author sofn
 */
@Slf4j
public class RedisCacheTemplate<T> {

    private final RedisUtil redisUtil;
    private final CacheKeyEnum redisRedisEnum;

    public RedisCacheTemplate(RedisUtil redisUtil, CacheKeyEnum redisRedisEnum) {
        this.redisUtil = redisUtil;
        this.redisRedisEnum = redisRedisEnum;
    }

    public T get(Object id) {
        T res = redisUtil.getCacheObject(generateKey(id));
        if (res == null) {
            res = getObjectFromDb(id);
            if (res != null) {
                set(id, res);
            }
        }
        return res;
    }

    public void set(Object id, T obj) {
        redisUtil.setCacheObject(
                generateKey(id), obj, redisRedisEnum.expiration(), redisRedisEnum.timeUnit());
    }

    public void delete(Object id) {
        redisUtil.deleteObject(generateKey(id));
    }

    public void refresh(Object id) {
        redisUtil.expire(generateKey(id), redisRedisEnum.expiration(), redisRedisEnum.timeUnit());
    }

    public String generateKey(Object id) {
        return redisRedisEnum.key() + id;
    }

    public T getObjectFromDb(Object id) {
        return null;
    }
}
