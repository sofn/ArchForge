package com.lesofn.matrixboot.auth.provider;


import com.lesofn.matrixboot.auth.model.AuthRequest;

/**
 * @author sofn
 */
public interface UserProvider {

    /**
     * 是否为有效用户
     *
     * @param uid
     * @return
     */
    boolean isValidUser(long uid);

    /**
     * 该用户是否有权限访问
     *
     * @param request
     * @param uid
     */
    boolean checkCanAccess(AuthRequest request, long uid);


    /**
     * 通过用户名密码认证用户
     *
     * @param loginName     不能为空
     * @param password      不能为空
     * @return uid
     */
    long authUser(String loginName, String password);

}
