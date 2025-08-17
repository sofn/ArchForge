package com.lesofn.matrixboot.frame.help.resources;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.matrixboot.auth.annotation.AuthType;
import com.lesofn.matrixboot.auth.annotation.BaseInfo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-04-29 16:19
 */
@RestController
@RequestMapping("/welcome")
public class WelcomeResource {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "")
    @BaseInfo(desc = "welcome", needAuth = AuthType.OPTION)
    public ObjectNode welcome() {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("message", "Welcome to MatrixBoot API");
        result.put("status", "success");
        return result;
    }
}
