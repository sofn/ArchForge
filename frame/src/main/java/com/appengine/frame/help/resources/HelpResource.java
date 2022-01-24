package com.appengine.frame.help.resources;

import com.alibaba.fastjson.JSONObject;
import com.appengine.auth.annotation.ApiStatus;
import com.appengine.auth.annotation.AuthType;
import com.appengine.auth.annotation.BaseInfo;
import com.appengine.frame.context.RequestContext;
import org.javasimon.aop.Monitored;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-04-29 16:19
 */
@Monitored
@RestController
@RequestMapping("/help")
public class HelpResource {

    @BaseInfo(desc = "help-ping", status = ApiStatus.PUBLIC, needAuth = AuthType.OPTION)
    @RequestMapping(value = "/ping")
    public JSONObject ping(RequestContext rc) {
        JSONObject result = new JSONObject();
        result.put("uid", rc.getCurrentUid());
        result.put("app_id", rc.getAppId());
        result.put("remote_ip", rc.getIp());
        return result;
    }

    @PostMapping(value = "/echo")
    public JSONObject echo(@RequestParam String msg) {
        JSONObject msgJson = new JSONObject();
        msgJson.put("msg", msg);
        return msgJson;
    }
}
