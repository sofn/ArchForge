package com.lesofn.appengine.frame.help.resources;

import com.lesofn.appengine.auth.annotation.AuthType;
import com.lesofn.appengine.auth.annotation.BaseInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-04-29 16:19
 */
@Controller
@RequestMapping("/welcome")
public class WelcomeResource {

    @RequestMapping(value = "")
    @BaseInfo(desc = "welcome", needAuth = AuthType.OPTION)
    public String welcome() {
//        return "redirect:/index.jsp";
//        return "forward:/WEB-INF/jsp/welcome.jsp";
        return "welcome";
    }
}
