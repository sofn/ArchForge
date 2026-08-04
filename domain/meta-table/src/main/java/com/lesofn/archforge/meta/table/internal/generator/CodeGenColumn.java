package com.lesofn.archforge.meta.table.internal.generator;

import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class CodeGenColumn {

    private String columnCode;
    private String fieldName;
    private String columnName;
    private MetaColumnType dataType;

    private String javaType;
    private String tsType;
    private String componentType;
    private String inputType;
    private String dateType;
    private String dateValueFormat;

    private String javaDefaultValue;
    private String tsDefaultValue;

    private String searchCondition;
    private String searchPredicate;
    private String keywordPredicate;

    private List<String> jpaAnnotations = new ArrayList<>();
    private List<String> validatorAnnotations = new ArrayList<>();
    private Set<String> imports = new LinkedHashSet<>();
    private Set<String> jpaImports = new LinkedHashSet<>();
    private Set<String> validatorImports = new LinkedHashSet<>();

    private List<Map<String, Object>> options = new ArrayList<>();
    private boolean hasOptions;

    private boolean searchable;
    private boolean listVisible;
    private boolean required;
    private boolean unique;
    private boolean nullable;

    private Integer length;
    private Integer precision;
    private Integer scale;
    private String arrayElementType;

    private boolean string;
    private boolean text;
    private boolean json;
    private boolean file;
    private boolean enumType;
    private boolean integer;
    private boolean decimal;
    private boolean booleanType;
    private boolean date;
    private boolean dateTime;
    private boolean uuid;
    private boolean timestampTz;
    private boolean array;
    private boolean geo;

    private boolean likeSearch;
    private boolean keywordSearchable;

    public boolean isEnum() { return enumType; }

    public boolean isBoolean() { return booleanType; }
}
