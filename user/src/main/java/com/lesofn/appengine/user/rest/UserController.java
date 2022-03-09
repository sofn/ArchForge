package com.lesofn.appengine.user.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lesofn.appengine.auth.annotation.ApiStatus;
import com.lesofn.appengine.auth.annotation.AuthType;
import com.lesofn.appengine.auth.annotation.BaseInfo;
import com.lesofn.appengine.auth.spi.CookieAuthSpi;
import com.lesofn.appengine.auth.spi.MAuthSpi;
import com.lesofn.appengine.frame.context.RequestContext;
import com.lesofn.appengine.user.domain.User;
import com.lesofn.appengine.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * Authors: sofn
 * Version: 1.0  Created at 2015-10-02 22:08.
 */
@RestController
@RequestMapping("/users")
@Tag(name = "用户接口", description = "用户相关CRUD接口")
public class UserController {

    @Resource
    private UserService userService;

    @BaseInfo(desc = "注册用户", needAuth = AuthType.OPTION)
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public boolean register(@RequestParam String username, @RequestParam String password) {
        return userService.save(new User(username, password));
    }

    @Operation(summary = "测试接口1")
    @BaseInfo(desc = "登陆", needAuth = AuthType.OPTION)
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public JSONObject login(
            HttpServletResponse response,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "false") boolean cookie
    ) {
        User user = userService.login(username, password);
        JSONObject result = (JSONObject) JSON.toJSON(user);
        if (cookie) {
            String cookieValue = CookieAuthSpi.generateCookie(user.getUid());
            Cookie authCookie = new Cookie(CookieAuthSpi.COOKIE_NAME, cookieValue);
            authCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(1));
            response.addCookie(authCookie);
            result.put("cookie", cookieValue);
        } else {
            result.put("mauth", MAuthSpi.generateMauth(user.getUid()));
        }
        return result;
    }

    @RequestMapping(value = "/show")
    @BaseInfo(desc = "显示用户信息", status = ApiStatus.PUBLIC, needAuth = AuthType.REQUIRED)
    @Operation(summary = "测试接口2")
    public User show(RequestContext rc, @RequestParam(required = false, defaultValue = "0") long uid) {
        if (uid <= 0) {
            uid = rc.getCurrentUid();
        }
        return userService.get(uid);
    }

}
