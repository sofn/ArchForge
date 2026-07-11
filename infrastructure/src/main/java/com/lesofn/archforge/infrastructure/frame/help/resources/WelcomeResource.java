package com.lesofn.archforge.infrastructure.frame.help.resources;

import com.lesofn.archforge.infrastructure.auth.annotation.AuthType;
import com.lesofn.archforge.infrastructure.auth.annotation.BaseInfo;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-04-29 16:19
 */
@RestController
@RequestMapping("/welcome")
public class WelcomeResource {

    @RequestMapping(value = "")
    @BaseInfo(desc = "welcome", needAuth = AuthType.OPTION)
    public Map<String, Object> welcome() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Welcome to ArchForge API");
        result.put("status", "success");
        return result;
    }
}
