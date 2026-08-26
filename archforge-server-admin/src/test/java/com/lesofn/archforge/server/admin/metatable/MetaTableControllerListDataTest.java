package com.lesofn.archforge.server.admin.metatable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.meta.table.api.dto.MetaDataQuery;
import com.lesofn.archforge.meta.table.api.dto.MetaPageResponse;
import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.api.service.MetaTableCrudService;
import com.lesofn.archforge.meta.table.internal.generator.MetaTableCodeGenerator;
import com.lesofn.archforge.meta.table.internal.service.MetaTableMigrationService;
import com.lesofn.archforge.server.admin.config.CodeGenWorkspaceResolver;
import com.lesofn.archforge.server.admin.controller.metatable.MetaTableController;
import com.lesofn.archforge.server.admin.dto.request.MetaDataListRequest;
import com.lesofn.archforge.user.api.service.SysUserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Unit coverage for data-list request handling: pageSize cap and sort/skip-count passthrough. */
class MetaTableControllerListDataTest {

    private MetaTableCrudService metaTableCrudService;
    private MetaTableController controller;

    @BeforeEach
    void setUp() {
        metaTableCrudService = mock(MetaTableCrudService.class);
        controller = new MetaTableController(mock(MetaTableAdminService.class), metaTableCrudService, mock(
                MetaTableCodeGenerator.class), mock(CodeGenWorkspaceResolver.class), mock(
                        MetaTableMigrationService.class), mock(
                                com.lesofn.archforge.meta.table.internal.service.MetaTableMigrationExporter.class), mock(
                                        SysUserService.class), new ArchForgeProperties());
        when(metaTableCrudService.list(any(), any()))
                .thenReturn(MetaPageResponse.of(List.of(), 0, 10, 1));
    }

    @Test
    void pageSizeAboveCapIsClampedToConfiguredMax() {
        MetaDataListRequest request = new MetaDataListRequest();
        request.setPageSize(100_000);

        controller.listData(1L, request);

        ArgumentCaptor<MetaDataQuery> captor = ArgumentCaptor.forClass(MetaDataQuery.class);
        verify(metaTableCrudService).list(eq(1L), captor.capture());
        assertEquals(200, captor.getValue().pageSize());
    }

    @Test
    void orderByAndOrderDirAndSkipCountPassThrough() {
        MetaDataListRequest request = new MetaDataListRequest();
        request.setOrderBy("create_time");
        request.setOrderDir("asc");
        request.setSkipCount(true);

        controller.listData(1L, request);

        ArgumentCaptor<MetaDataQuery> captor = ArgumentCaptor.forClass(MetaDataQuery.class);
        verify(metaTableCrudService).list(eq(1L), captor.capture());
        MetaDataQuery query = captor.getValue();
        assertEquals("create_time", query.orderBy());
        assertEquals("asc", query.orderDir());
        assertTrue(query.skipCount());
    }

    @Test
    void businessErrorFromSortValidationPropagatesAsModuleException() {
        when(metaTableCrudService.list(any(), any()))
                .thenThrow(new MetaTableException(MetaTableErrorCode.META_QUERY_PARAM_INVALID, "排序字段不允许: x"));
        MetaDataListRequest request = new MetaDataListRequest();
        request.setOrderBy("x");

        assertThrows(MetaTableException.class, () -> controller.listData(1L, request));
    }
}
