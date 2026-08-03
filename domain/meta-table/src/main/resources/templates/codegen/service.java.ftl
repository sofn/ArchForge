package ${packageBase}.service;

import ${packageBase}.dao.${entityName}Dao;
import ${packageBase}.domain.${entityName};
import ${packageBase}.dto.*;
import ${packageBase}.error.${entityName}ErrorCode;
import ${packageBase}.error.${entityName}Exception;
import jakarta.persistence.criteria.Predicate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ${entityName}Service {

    private final ${entityName}Dao ${entityName?uncap_first}Dao;

    public Optional<${entityName}> getById(Long id) {
        return ${entityName?uncap_first}Dao.findById(id)
                .filter(it -> !Boolean.TRUE.equals(it.getDeleted()));
    }

    public ${entityName} findById(Long id) {
        return getById(id).orElseThrow(() -> new ${entityName}Exception(${entityName}ErrorCode.${entityName?upper_case}_NOT_EXISTS));
    }

    public ${entityName}Response detail(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public Long create(${entityName}CreateRequest request) {
        ${entityName} entity = ${entityName}.create(request);
        ${entityName} saved = ${entityName?uncap_first}Dao.save(entity);
        return saved.getId();
    }

    @Transactional
    public Boolean update(${entityName}UpdateRequest request) {
        ${entityName} entity = findById(request.getId());
        entity.updateFrom(request);
        ${entityName?uncap_first}Dao.save(entity);
        return true;
    }

    @Transactional
    public Boolean delete(Long id) {
        ${entityName} entity = findById(id);
        entity.softDelete();
        ${entityName?uncap_first}Dao.save(entity);
        return true;
    }

    public ${entityName}PageResult<${entityName}Response> list(${entityName}ListRequest request) {
        int currentPage = request.getCurrentPage() != null && request.getCurrentPage() > 0
                ? request.getCurrentPage()
                : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0
                ? request.getPageSize()
                : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);

        Specification<${entityName}> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
<#list searchableColumns as col>
<#if col.isString || col.isText || col.isJson || col.isFile || col.isEnum>
            if (StringUtils.hasText(request.get${col.fieldName?cap_first}())) {
                predicates.add(cb.like(root.get("${col.fieldName}"), "%" + request.get${col.fieldName?cap_first}() + "%", '!'));
            }
<#else>
            if (request.get${col.fieldName?cap_first}() != null) {
                predicates.add(cb.equal(root.get("${col.fieldName}"), request.get${col.fieldName?cap_first}()));
            }
</#if>
</#list>
<#if keywordColumns?? && keywordColumns?size gt 0>
            if (StringUtils.hasText(request.getKeyword())) {
                List<Predicate> keywordPredicates = new ArrayList<>();
<#list keywordColumns as col>
                keywordPredicates.add(cb.like(root.get("${col.fieldName}"), "%" + request.getKeyword() + "%", '!'));
</#list>
                predicates.add(cb.or(keywordPredicates.toArray(new Predicate[0])));
            }
</#if>
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<${entityName}> page = ${entityName?uncap_first}Dao.findAll(spec, pageable);
        List<${entityName}Response> list = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return ${entityName}PageResult.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    private ${entityName}Response toResponse(${entityName} entity) {
        ${entityName}Response response = new ${entityName}Response();
        response.setId(entity.getId());
<#list listVisibleColumns as col>
        response.set${col.fieldName?cap_first}(entity.get${col.fieldName?cap_first}());
</#list>
        response.setCreateTime(entity.getCreateTime() == null ? null : entity.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return response;
    }
}
