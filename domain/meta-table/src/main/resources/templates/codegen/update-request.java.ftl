package ${packageBase}.dto;

<#if hasDecimal>import java.math.BigDecimal;</#if>
<#if hasDate>import java.time.LocalDate;</#if>
<#if hasDateTime>import java.time.LocalDateTime;</#if>
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
