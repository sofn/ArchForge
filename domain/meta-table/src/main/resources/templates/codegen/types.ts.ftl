export interface ${className}CreateRequest {
<#list columns as col>
  ${col.fieldName}?: ${col.tsType};
</#list>
}

export interface ${className}UpdateRequest extends ${className}CreateRequest {
  id?: number;
}

export interface ${className}ListRequest {
  currentPage?: number;
  pageSize?: number;
  keyword?: string;
<#list searchableColumns as col>
  ${col.fieldName}?: ${col.tsType};
</#list>
}

export interface ${className}Response extends ${className}CreateRequest {
  id: number;
  createTime?: number;
}

export interface ${className}PageResult<T> {
  list: T[];
  total: number;
  pageSize: number;
  currentPage: number;
}

export interface ${className}ImportResult {
  total: number;
  success: number;
  errors: string[];
}

export const init${className}Form = (): ${className}CreateRequest => ({
<#list columns as col>
  ${col.fieldName}: ${col.tsDefaultValue},
</#list>
});
