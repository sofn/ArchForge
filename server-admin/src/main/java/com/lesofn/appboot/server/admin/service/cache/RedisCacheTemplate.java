package com.lesofn.appboot.server.admin.service.cache;

import com.lesofn.appboot.infrastructure.db.redis.RedisUtil;
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

    /**
     * 从缓存中获取对象   如果获取不到的话  从DB层面获取
     *
     * @param id id
     */
    public T getObjectById(Object id) {
        T objectFromDb = getObjectFromDb(id);
        set(id, objectFromDb);
        return objectFromDb;
    }


    public void set(Object id, T obj) {
        redisUtil.setCacheObject(generateKey(id), obj, redisRedisEnum.expiration(), redisRedisEnum.timeUnit());
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
