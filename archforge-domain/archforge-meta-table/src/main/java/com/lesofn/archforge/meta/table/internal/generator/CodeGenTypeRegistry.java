package com.lesofn.archforge.meta.table.internal.generator;

import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.internal.generator.handler.DefaultCodeGenTypeHandler;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CodeGenTypeRegistry {

    private final Map<MetaColumnType, List<CodeGenTypeHandler>> handlers = new EnumMap<>(MetaColumnType.class);

    public CodeGenTypeRegistry() {
        registerDefaults();
    }

    public void register(MetaColumnType type, CodeGenTypeHandler handler) {
        handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(0, handler);
    }

    public CodeGenTypeHandler resolve(MetaColumnType type) {
        List<CodeGenTypeHandler> list = handlers.get(type);
        if (list == null || list.isEmpty()) {
            throw new IllegalStateException("No CodeGenTypeHandler registered for " + type);
        }
        return list.get(0);
    }

    private void registerDefaults() {
        DefaultCodeGenTypeHandler defaultHandler = new DefaultCodeGenTypeHandler();
        for (MetaColumnType type : MetaColumnType.values()) {
            register(type, defaultHandler);
        }
    }
}
