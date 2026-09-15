package ${packageBase}.dto;

<#list imports as imp>
import ${imp};
</#list>
<#list validatorImports as imp>
import ${imp};
</#list>
import lombok.Data;

@Data
public class ${entityName}CreateRequest {

<#list columns as col>
<#list col.validatorAnnotations as anno>
    ${anno}
</#list>
    private ${col.javaType} ${col.fieldName};
</#list>
}
