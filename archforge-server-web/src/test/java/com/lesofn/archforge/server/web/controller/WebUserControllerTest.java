package com.lesofn.archforge.server.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lesofn.archforge.server.web.context.WebUserContext;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.service.SysUserService;
import com.lesofn.archforge.user.domain.adapter.port.PasswordEncoderPort;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class WebUserControllerTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WebUserController controller = new WebUserController(sysUserService, passwordEncoderPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        WebUserContext.set(7L, "alice");
    }

    @AfterEach
    void tearDown() {
        WebUserContext.clear();
    }

    @Test
    void profileReturnsCurrentUser() throws Exception {
        SysUser user = new SysUser();
        user.setUserId(7L);
        user.setUsername("alice");
        user.setNickname("Alice");
        user.setAvatar("avatar.png");
        when(sysUserService.findById(7L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/web/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.nickname").value("Alice"));
    }
}
