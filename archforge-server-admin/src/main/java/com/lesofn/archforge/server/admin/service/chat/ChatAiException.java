package com.lesofn.archforge.server.admin.service.chat;

import com.lesofn.archforge.common.error.ArchForgeProjectModule;
import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.api.ProjectModule;
import com.lesofn.archforge.common.error.exception.BaseRuntimeException;
import com.lesofn.archforge.common.error.manager.ErrorInfo;

public class ChatAiException extends BaseRuntimeException {

    public ChatAiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ChatAiException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    @Override
    public ProjectModule projectModule() {
        return ArchForgeProjectModule.CHAT_AI;
    }
}
