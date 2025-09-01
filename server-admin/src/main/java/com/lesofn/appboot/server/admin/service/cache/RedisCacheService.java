package com.lesofn.appboot.server.admin.service.cache;


import com.lesofn.appboot.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.appboot.infrastructure.db.redis.RedisUtil;
import com.lesofn.appboot.user.domain.SysRole;
import com.lesofn.appboot.user.domain.SysUser;
import com.lesofn.appboot.user.service.SysRoleService;
import com.lesofn.appboot.user.service.SysUserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author sofn
 */
@Component
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisUtil redisUtil;
    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;

    public RedisCacheTemplate<String> captchaCache;
    public RedisCacheTemplate<SystemLoginUser> loginUserCache;
    public RedisCacheTemplate<SysUser> userCache;
    public RedisCacheTemplate<SysRole> roleCache;

    @PostConstruct
    public void init() {

        captchaCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.CAPTCHAT);

        loginUserCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.LOGIN_USER_KEY);

        userCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.USER_ENTITY_KEY) {
            @Override
            public SysUser getObjectFromDb(Object id) {
                return sysUserService.findById((Long) id).orElse(null);
            }
        };

        roleCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.ROLE_ENTITY_KEY) {
            @Override
            public SysRole getObjectFromDb(Object id) {
                return sysRoleService.findById((Long) id).orElse(null);
            }
        };
    }
}