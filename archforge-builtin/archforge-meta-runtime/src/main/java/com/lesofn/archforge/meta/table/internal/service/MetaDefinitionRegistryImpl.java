package com.lesofn.archforge.meta.table.internal.service;

import com.lesofn.archforge.meta.table.internal.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.internal.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.service.MetaDefinitionRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link MetaDefinitionRegistry}: repository pass-through until pinned, then an immutable snapshot. */
@Service
@RequiredArgsConstructor
public class MetaDefinitionRegistryImpl implements MetaDefinitionRegistry {

    private final MetaTableRepository tableRepository;
    private final MetaColumnRepository columnRepository;

    /** Null until {@link #pin()}; then replaced wholesale, never mutated. */
    private volatile @Nullable Map<String, TableSnapshot> pinned;

    @Override
    public Optional<TableSnapshot> find(String tableCode) {
        Map<String, TableSnapshot> view = pinned;
        if (view != null) {
            return Optional.ofNullable(view.get(tableCode));
        }
        return tableRepository.findByTableCodeAndDeletedFalse(tableCode).map(this::snapshot);
    }

    @Override
    public long count() {
        Map<String, TableSnapshot> view = pinned;
        return view != null ? view.size() : tableRepository.countByDeletedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public void pin() {
        Map<String, TableSnapshot> view = new HashMap<>();
        for (MetaTable table : tableRepository.findAllByDeletedFalse()) {
            view.put(table.getTableCode(), snapshot(table));
        }
        pinned = Map.copyOf(view);
    }

    private TableSnapshot snapshot(MetaTable table) {
        return new TableSnapshot(table, List.copyOf(
                columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(Objects.requireNonNull(table.getId()))));
    }
}
