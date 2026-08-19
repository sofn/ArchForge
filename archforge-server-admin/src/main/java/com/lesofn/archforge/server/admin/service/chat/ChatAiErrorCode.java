package com.lesofn.archforge.server.admin.service.chat;

import com.lesofn.archforge.common.error.ArchForgeProjectModule;
import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.manager.ErrorManager;
import lombok.Getter;

@Getter
public enum ChatAiErrorCode implements ErrorCode {
    LLM_NOT_CONFIGURED(1, "未配置 LLM：请设置 arch-forge.llm.api-key（OpenAI 或 Anthropic）"),
    LLM_REQUEST_FAILED(2, "LLM 请求失败"),
    CHAT_SESSION_NOT_FOUND(3, "会话不存在");

    private final int nodeNum;
    private final String msg;

    ChatAiErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(ArchForgeProjectModule.CHAT_AI, this);
    }
}
