package ${packageBase}.dto;

<#list imports as imp>
import ${imp};
</#list>
import lombok.Data;

@Data
public class ${entityName}ListRequest {

    private Integer currentPage = 1;

    private Integer pageSize = 10;

    private String keyword;

<#list searchableColumns as col>
    <#if col.rangeSearch>
    private ${col.javaType} ${col.fieldName}Start;

    private ${col.javaType} ${col.fieldName}End;
    <#else>
    private ${col.javaType} ${col.fieldName};
    </#if>
</#list>
}
