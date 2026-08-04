import { http } from "@/utils/http";
import type {
  ${className}CreateRequest,
  ${className}UpdateRequest,
  ${className}ListRequest,
  ${className}Response,
  ${className}PageResult,
  ${className}ImportResult
} from "@/views/${tableCode}/utils/types";

type Result<T = any> = {
  code: number;
  message: string;
  data?: T;
};

type ${className}Page = ${className}PageResult<${className}Response>;

export const get${className}List = (data?: ${className}ListRequest) => {
  return http.request<Result<${className}Page>>(
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

export const export${className}Data = (format = "EXCEL") => {
  return http.request<Blob>("get", "${basePath}/export?format=" + format, { responseType: "blob" });
};

export const import${className}Data = (file: File, format = "CSV") => {
  const formData = new FormData();
  formData.append("file", file);
  return http.request<Result<${className}ImportResult>>("post", "${basePath}/import?format=" + format, {
    data: formData,
    headers: { "Content-Type": "multipart/form-data" }
  });
};
