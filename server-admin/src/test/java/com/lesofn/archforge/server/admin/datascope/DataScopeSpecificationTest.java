package com.lesofn.archforge.server.admin.datascope;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.infrastructure.security.datascope.DataScopeContext;
import com.lesofn.archforge.infrastructure.user.web.DataScopeEnum;
import com.lesofn.archforge.user.api.domain.SysDept;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.service.SysDeptService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

/**
 * {@link DataScopeSpecification} 单元测试。
 *
 * @author sofn
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataScopeSpecificationTest {

    @Mock
    private SysDeptService deptService;

    @Mock
    private Specification<SysUser> baseSpec;

    @Mock
    private Root<SysUser> root;

    @SuppressWarnings("rawtypes")
    @Mock
    private CriteriaQuery query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path deptPath;

    @Mock
    private Path userPath;

    @Mock
    private Predicate basePredicate;

    @Mock
    private Predicate scopePredicate;

    @Mock
    private Predicate combinedPredicate;

    private DataScopeSpecification dataScopeSpecification;

    @BeforeEach
    void setUp() {
        dataScopeSpecification = new DataScopeSpecification(deptService);
        when(root.get("deptId")).thenReturn(deptPath);
        when(root.get("userId")).thenReturn(userPath);
    }

    @Test
    void allScope_returnsBaseSpecification() {
        DataScopeContext ctx = DataScopeContext.builder()
                .dataScope(DataScopeEnum.ALL)
                .build();

        Specification<SysUser> result = dataScopeSpecification.apply(baseSpec, ctx);

        assertSame(baseSpec, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void onlySelfScope_addsUserIdFilter() {
        DataScopeContext ctx = DataScopeContext.builder()
                .dataScope(DataScopeEnum.ONLY_SELF)
                .userId(42L)
                .build();

        when(baseSpec.toPredicate(root, query, cb)).thenReturn(basePredicate);
        when(cb.equal(userPath, 42L)).thenReturn(scopePredicate);
        when(cb.and(basePredicate, scopePredicate)).thenReturn(combinedPredicate);

        Specification<SysUser> result = dataScopeSpecification.apply(baseSpec, ctx);
        Predicate predicate = result.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(cb).equal(userPath, 42L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void singleDeptScope_addsDeptIdFilter() {
        DataScopeContext ctx = DataScopeContext.builder()
                .dataScope(DataScopeEnum.SINGLE_DEPT)
                .deptId(10L)
                .build();

        when(baseSpec.toPredicate(root, query, cb)).thenReturn(basePredicate);
        when(cb.equal(deptPath, 10L)).thenReturn(scopePredicate);
        when(cb.and(basePredicate, scopePredicate)).thenReturn(combinedPredicate);

        Specification<SysUser> result = dataScopeSpecification.apply(baseSpec, ctx);
        Predicate predicate = result.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(cb).equal(deptPath, 10L);
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Test
    void customScope_usesRoleDeptIdSet() {
        DataScopeContext ctx = DataScopeContext.builder()
                .dataScope(DataScopeEnum.CUSTOM_DEFINE)
                .customDeptIds(Set.of(10L, 11L))
                .build();

        Path inPath = mock(Path.class);
        Predicate inPredicate = mock(Predicate.class);
        when(root.get("deptId")).thenReturn(inPath);
        when(baseSpec.toPredicate(root, query, cb)).thenReturn(basePredicate);
        when(inPath.in((Collection) any(Collection.class))).thenReturn(inPredicate);
        when(cb.and(basePredicate, inPredicate)).thenReturn(combinedPredicate);

        Specification<SysUser> result = dataScopeSpecification.apply(baseSpec, ctx);
        Predicate predicate = result.toPredicate(root, query, cb);

        assertNotNull(predicate);
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Test
    void deptTreeScope_computesDescendants() {
        SysDept parent = new SysDept();
        parent.setDeptId(10L);
        parent.setParentId(0L);
        SysDept child = new SysDept();
        child.setDeptId(11L);
        child.setParentId(10L);

        when(deptService.findAllActiveDepts()).thenReturn(List.of(parent, child));

        DataScopeContext ctx = DataScopeContext.builder()
                .dataScope(DataScopeEnum.DEPT_TREE)
                .deptId(10L)
                .build();

        Path inPath = mock(Path.class);
        Predicate inPredicate = mock(Predicate.class);
        when(root.get("deptId")).thenReturn(inPath);
        when(baseSpec.toPredicate(root, query, cb)).thenReturn(basePredicate);
        when(inPath.in((Collection) any(Collection.class))).thenReturn(inPredicate);
        when(cb.and(basePredicate, inPredicate)).thenReturn(combinedPredicate);

        Specification<SysUser> result = dataScopeSpecification.apply(baseSpec, ctx);
        Predicate predicate = result.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(deptService).findAllActiveDepts();
    }
}
