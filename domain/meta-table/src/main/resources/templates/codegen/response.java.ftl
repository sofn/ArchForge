package ${packageBase}.dto;

<#list imports as imp>
import ${imp};
</#list>
import lombok.Data;

@Data
public class ${entityName}Response {

    private Long id;

<#list listVisibleColumns as col>
    private ${col.javaType} ${col.fieldName};
</#list>

    private Long createTime;
}
