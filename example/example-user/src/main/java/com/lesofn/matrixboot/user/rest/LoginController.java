package com.lesofn.matrixboot.user.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lesofn.matrixboot.auth.annotation.AuthType;
import com.lesofn.matrixboot.auth.annotation.BaseInfo;
import com.lesofn.matrixboot.auth.spi.MAuthSpi;
import com.lesofn.matrixboot.common.exception.EngineExceptionHelper;
import com.lesofn.matrixboot.user.domain.User;
import com.lesofn.matrixboot.user.service.UserService;
import com.lesofn.matrixboot.user.utils.UserExcepFactor;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authors: sofn
 * Version: 1.0  Created at 2015-10-02 22:08.
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    private UserService userService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "", method = RequestMethod.POST)
    @BaseInfo(needAuth = AuthType.OPTION)
    public ObjectNode add(@RequestParam String username, @RequestParam String password) {
        ObjectNode result = objectMapper.createObjectNode();
        User user = userService.login(username, password);
        if (user == null) {
            throw EngineExceptionHelper.localException(UserExcepFactor.USERPASS_ERROR);
        }
        result.set("user", objectMapper.valueToTree(user));
        result.put("mauth", MAuthSpi.generateMauth(user.getUid()));
        return result;
    }

}
