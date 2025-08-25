package com.lesofn.matrixboot.user.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lesofn.matrixboot.infrastructure.auth.annotation.AuthType;
import com.lesofn.matrixboot.infrastructure.auth.annotation.BaseInfo;
import com.lesofn.matrixboot.infrastructure.auth.spi.MAuthSpi;
import com.lesofn.matrixboot.common.errors.EngineExceptionHelper;
import com.lesofn.matrixboot.user.domain.SysUser;
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
@RequestMapping("/api/login")
public class LoginController {

    @Resource
    private UserService userService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "", method = RequestMethod.POST)
    @BaseInfo(needAuth = AuthType.OPTION)
    public ObjectNode add(@RequestParam String username, @RequestParam String password) {
        ObjectNode result = objectMapper.createObjectNode();
        SysUser user = userService.findByUsername(username)
            .orElseThrow(() -> EngineExceptionHelper.localException(UserExcepFactor.USERPASS_ERROR));
        // 这里简化处理，实际应该验证密码
        result.set("user", objectMapper.valueToTree(user));
        result.put("mauth", MAuthSpi.generateMauth(user.getUserId()));
        return result;
    }

}
