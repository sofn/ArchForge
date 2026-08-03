package ${packageBase}.dto;

<#if hasDecimal>import java.math.BigDecimal;</#if>
<#if hasDate>import java.time.LocalDate;</#if>
<#if hasDateTime>import java.time.LocalDateTime;</#if>
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ${entityName}CreateRequest {

<#list columns as col>
<#if col.required>
<#if col.isString || col.isText || col.isJson || col.isFile || col.isEnum>
    @NotBlank(message = "${col.columnName}不能为空")
<#else>
    @NotNull(message = "${col.columnName}不能为空")
</#if>
</#if>
    private ${col.javaType} ${col.fieldName};
</#list>
}
