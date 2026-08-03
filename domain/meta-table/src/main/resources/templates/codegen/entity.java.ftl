package ${packageBase}.domain;

import com.lesofn.archforge.common.repository.BaseEntity;
import ${packageBase}.dto.${entityName}CreateRequest;
import ${packageBase}.dto.${entityName}UpdateRequest;
<#if hasDecimal>import java.math.BigDecimal;</#if>
<#if hasDate>import java.time.LocalDate;</#if>
<#if hasDateTime>import java.time.LocalDateTime;</#if>
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@ToString
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "${tableCode}")
public class ${entityName} extends BaseEntity<${entityName}> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<#list columns as col>
<#assign attrs = []>
<#if !col.nullable><#assign attrs = attrs + ["nullable = false"]></#if>
<#if (col.isString || col.isText || col.isJson || col.isFile || col.isEnum) && (col.length > 0)><#assign attrs = attrs + ["length = ${col.length}"]></#if>
<#if col.isDecimal><#assign attrs = attrs + ["precision = ${col.precision}", "scale = ${col.scale}"]></#if>
<#if col.unique><#assign attrs = attrs + ["unique = true"]></#if>
<#if attrs?size gt 0>    @Column(${attrs?join(", ")})
</#if>    private ${col.javaType} ${col.fieldName};
</#list>

    public static ${entityName} create(${entityName}CreateRequest request) {
        ${entityName} entity = new ${entityName}();
        entity.updateFrom(request);
        entity.setDeleted(false);
        return entity;
    }

    public void updateFrom(${entityName}CreateRequest request) {
<#list columns as col>
        if (request.get${col.fieldName?cap_first}() != null) {
            this.${col.fieldName} = request.get${col.fieldName?cap_first}();
        }
</#list>
    }

    public void updateFrom(${entityName}UpdateRequest request) {
<#list columns as col>
        if (request.get${col.fieldName?cap_first}() != null) {
            this.${col.fieldName} = request.get${col.fieldName?cap_first}();
        }
</#list>
    }

    public void softDelete() {
        setDeleted(true);
    }
}
