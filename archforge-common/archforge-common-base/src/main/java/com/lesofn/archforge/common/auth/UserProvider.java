package com.lesofn.archforge.common.auth;

/**
 * 用户信息提供者 SPI。
 *
 * @author sofn
 */
public interface UserProvider {

    /**
     * 是否为有效用户
     *
     * @param uid 用户ID
     * @return true 表示该用户有效
     */
    boolean isValidUser(long uid);

    /**
     * 该用户是否有权限访问
     *
     * @param request 当前请求
     * @param uid 用户ID
     */
    boolean checkCanAccess(AuthRequest request, long uid);

    /**
     * 通过用户名密码认证用户
     *
     * @param loginName 不能为空
     * @param password 不能为空
     * @return uid
     */
    long authUser(String loginName, String password);
}
