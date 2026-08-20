package com.lesofn.archforge.server.admin.controller.chat;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lesofn.archforge.infrastructure.annotation.RateLimit;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import com.lesofn.archforge.server.admin.service.chat.ChatAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "ChatAI")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/chat")
public class ChatAiController {

    private final ChatAiService chatAiService;

    @Operation(summary = "LLM 配置状态（不含密钥）")
    @GetMapping("/config")
    public Map<String, Object> config() {
        return chatAiService.configStatus();
    }

    @Operation(summary = "会话列表")
    @GetMapping("/sessions")
    public List<Map<String, Object>> sessions() {
        return chatAiService.listSessions();
    }

    @Operation(summary = "新建会话")
    @SaCheckPermission(value = "chatai:use", type = StpAdminUtil.TYPE)
    @PostMapping("/sessions")
    public Map<String, Object> createSession() {
        return chatAiService.createSession();
    }

    @Operation(summary = "会话消息")
    @GetMapping("/sessions/{id}/messages")
    public List<Map<String, String>> messages(@PathVariable String id) {
        return chatAiService.messages(id);
    }

    @Operation(summary = "删除会话")
    @SaCheckPermission(value = "chatai:use", type = StpAdminUtil.TYPE)
    @DeleteMapping("/sessions/{id}")
    public void delete(@PathVariable String id) {
        chatAiService.deleteSession(id);
    }

    @Operation(summary = "发送消息（SSE：delta / done / error）")
    @SaCheckPermission(value = "chatai:use", type = StpAdminUtil.TYPE)
    @RateLimit(key = "admin-chat", time = 60, maxCount = 20, limitType = RateLimit.LimitType.USER)
    @PostMapping(value = "/sessions/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter send(@PathVariable String id, @RequestBody @Valid ChatMessageRequest request) {
        return chatAiService.stream(id, request.getContent());
    }

    @Data
    public static class ChatMessageRequest {
        @NotBlank
        private String content;
    }
}
