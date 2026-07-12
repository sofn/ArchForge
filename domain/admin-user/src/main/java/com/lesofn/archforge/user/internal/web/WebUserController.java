package com.lesofn.archforge.user.internal.web;

import com.lesofn.archforge.infrastructure.auth.annotation.AuthType;
import com.lesofn.archforge.infrastructure.auth.annotation.BaseInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/** Authors: sofn Version: 1.0 Created at 2015-10-18 00:09. */
@Controller
@RequestMapping("/web")
public class WebUserController {

    @BaseInfo(needAuth = AuthType.OPTION)
    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String register() {
        return "account/register";
    }

    @BaseInfo(needAuth = AuthType.OPTION)
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login() {
        return "account/login";
    }
}
