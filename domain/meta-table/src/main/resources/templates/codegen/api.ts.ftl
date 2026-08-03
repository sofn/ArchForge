import { http } from "@/utils/http";
import type {
  ${className}CreateRequest,
  ${className}UpdateRequest,
  ${className}ListRequest,
  ${className}Response,
  ${className}PageResult
} from "@/views/${tableCode}/utils/types";

type Result<T = any> = {
  code: number;
  message: string;
  data?: T;
};

type ResultTable<T = any> = {
  code: number;
  message: string;
  data?: {
    list: T[];
    total?: number;
    pageSize?: number;
    currentPage?: number;
  };
};

export const get${className}List = (data?: ${className}ListRequest) => {
  return http.request<ResultTable<${className}PageResult<${className}Response>>>(
    "post",
    "${basePath}",
    { data }
  );
};

export const create${className} = (data?: ${className}CreateRequest) => {
  return http.request<Result<number>>("post", "${basePath}/create", { data });
};

export const update${className} = (id: number, data?: ${className}UpdateRequest) => {
  return http.request<Result<boolean>>("put", `${basePath}/${r"${id}"}`, { data });
};

export const delete${className} = (id: number) => {
  return http.request<Result<boolean>>("delete", `${basePath}/${r"${id}"}`);
};

export const get${className}Detail = (id: number) => {
  return http.request<Result<${className}Response>>("get", `${basePath}/${r"${id}"}`);
};
