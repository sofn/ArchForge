package com.lesofn.archforge.meta.table.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.meta.table.api.dao.MetaColumnRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.service.MetaDefinitionRegistry.TableSnapshot;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pass-through before {@code pin()}, frozen immutable view after (P3-4-3 F3). */
class MetaDefinitionRegistryImplTest {

    private MetaTableRepository tableRepository;
    private MetaColumnRepository columnRepository;
    private MetaDefinitionRegistryImpl registry;

    @BeforeEach
    void setUp() {
        tableRepository = mock(MetaTableRepository.class);
        columnRepository = mock(MetaColumnRepository.class);
        registry = new MetaDefinitionRegistryImpl(tableRepository, columnRepository);
        MetaTable orders = table(1L, "orders");
        when(tableRepository.findByTableCodeAndDeletedFalse("orders")).thenReturn(Optional.of(orders));
        when(tableRepository.findAllByDeletedFalse()).thenReturn(List.of(orders));
        when(tableRepository.countByDeletedFalse()).thenReturn(1L);
        when(columnRepository.findByTableIdAndDeletedFalseOrderBySortAsc(1L)).thenReturn(List.of(column("name")));
    }

    @Test
    void unpinnedLookupsReadTheRepositoriesEveryTime() {
        TableSnapshot snapshot = registry.find("orders").orElseThrow();
        registry.find("orders");

        assertEquals("orders", snapshot.table().getTableCode());
        assertEquals("name", snapshot.columns().get(0).getColumnCode());
        verify(tableRepository, times(2)).findByTableCodeAndDeletedFalse("orders");
        assertEquals(1L, registry.count());
        assertTrue(registry.find("missing").isEmpty());
    }

    @Test
    void pinnedViewIsFrozenAndNeverTouchesTheRepositories() {
        registry.pin();
        // The mirror changes after pinning — the pinned instance must not see it.
        when(tableRepository.findByTableCodeAndDeletedFalse("orders")).thenReturn(Optional.empty());
        when(tableRepository.countByDeletedFalse()).thenReturn(0L);

        TableSnapshot snapshot = registry.find("orders").orElseThrow();

        assertEquals(List.of("name"), snapshot.columns().stream().map(MetaColumn::getColumnCode).toList());
        assertEquals(1L, registry.count());
        assertTrue(registry.find("missing").isEmpty());
        verify(tableRepository, never()).findByTableCodeAndDeletedFalse("orders");
        assertThrows(UnsupportedOperationException.class, () -> snapshot.columns().add(column("x")));
    }

    private static MetaTable table(long id, String code) {
        MetaTable table = new MetaTable();
        table.setId(id);
        table.setTableCode(code);
        table.setTableName(code);
        return table;
    }

    private static MetaColumn column(String code) {
        MetaColumn column = new MetaColumn();
        column.setColumnCode(code);
        column.setColumnName(code);
        column.setDataType(MetaColumnType.STRING);
        return column;
    }
}
