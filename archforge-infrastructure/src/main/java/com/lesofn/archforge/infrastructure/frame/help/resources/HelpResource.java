package com.lesofn.archforge.infrastructure.frame.help.resources;

import com.lesofn.archforge.infrastructure.auth.annotation.ApiStatus;
import com.lesofn.archforge.infrastructure.auth.annotation.AuthType;
import com.lesofn.archforge.infrastructure.auth.annotation.BaseInfo;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-04-29 16:19
 */
@RestController
@RequestMapping("/help")
public class HelpResource {

    @BaseInfo(desc = "help-ping", status = ApiStatus.PUBLIC, needAuth = AuthType.OPTION)
    @RequestMapping(value = "/ping")
    public Map<String, Object> ping(RequestContext rc) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("uid", rc.getCurrentUid());
        result.put("app_id", rc.getAppId());
        result.put("remote_ip", rc.getIp());
        return result;
    }

    @PostMapping(value = "/echo")
    public Map<String, Object> echo(@RequestParam String msg) {
        Map<String, Object> msgJson = new LinkedHashMap<>();
        msgJson.put("msg", msg);
        return msgJson;
    }
}
