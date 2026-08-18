package ${packageBase}.dto;

<#list imports as imp>
import ${imp};
</#list>
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ${entityName}UpdateRequest {

    @NotNull(message = "ID不能为空")
    private Long id;

<#list columns as col>
    private ${col.javaType} ${col.fieldName};
</#list>
}
