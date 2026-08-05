<script setup lang="ts">
import { ref, watch } from "vue";
import { message } from "@/utils/message";
import type { ${className}CreateRequest } from "../utils/types";
import { init${className}Form } from "../utils/types";

const props = defineProps<{ formInline: ${className}CreateRequest }>();

const formRef = ref();
const form = ref<${className}CreateRequest>({ ...init${className}Form() });

watch(
  () => props.formInline,
  val => {
    form.value = { ...init${className}Form(), ...val };
  },
  { immediate: true, deep: true }
);

const getRef = () => formRef.value;

defineExpose({ getRef });

function handleUploadSuccess(res: any, fieldName: string) {
  if (res?.code === 0 && res?.data?.fileId) {
    form.value[fieldName] = res.data.fileId;
  }
}

function handleMultiUploadSuccess(res: any, fieldName: string) {
  if (res?.code === 0 && res?.data?.fileId) {
    if (!form.value[fieldName]) {
      form.value[fieldName] = [];
    }
    (form.value[fieldName] as number[]).push(res.data.fileId);
  }
}

function singleFileList(fieldName: string, label: string) {
  const value = form.value[fieldName];
  if (value) {
    return [{ name: label, url: "/api/file/download/" + value, uid: Number(value) }];
  }
  return [];
}

function multiFileList(fieldName: string) {
  let value = form.value[fieldName];
  if (typeof value === "string" && value) {
    try {
      value = JSON.parse(value);
      form.value[fieldName] = value;
    } catch {
      return [];
    }
  }
  return (value || []).map((id: number, idx: number) => ({
    name: "图片" + (idx + 1),
    url: "/api/file/download/" + id,
    uid: id + idx
  }));
}

function beforeUpload(file: any, maxSize: number) {
  if (file.size > maxSize) {
    message("文件大小不能超过 " + maxSize + " 字节", { type: "error" });
    return false;
  }
  return true;
}
</script>

<template>
  <el-form ref="formRef" :model="form" label-width="120px">
<#list columns as col>
    <el-form-item label="${col.columnName}" prop="${col.fieldName}">
<#if col.isFile>
      <el-upload
        :file-list="singleFileList('${col.fieldName}', '${col.columnName}')"
        :action="'/api/file/upload'"
        :limit="1"
        :before-upload="(file: any) => beforeUpload(file, ${(col.fileSizeLimit!10485760)?c})"
        :on-success="(res: any) => handleUploadSuccess(res, '${col.fieldName}')"
        class="upload-demo"
      >
        <el-button type="primary">上传${col.columnName}</el-button>
      </el-upload>
<#elseif col.isImage>
      <el-upload
        :file-list="singleFileList('${col.fieldName}', '${col.columnName}')"
        :action="'/api/file/upload'"
        accept="image/*"
        :limit="1"
        list-type="picture-card"
        :before-upload="(file: any) => beforeUpload(file, ${(col.fileSizeLimit!10485760)?c})"
        :on-success="(res: any) => handleUploadSuccess(res, '${col.fieldName}')"
      >
        <span class="text-2xl">+</span>
      </el-upload>
<#elseif col.isMultiImage>
      <el-upload
        :file-list="multiFileList('${col.fieldName}')"
        :action="'/api/file/upload'"
        accept="image/*"
        multiple
        list-type="picture-card"
        :before-upload="(file: any) => beforeUpload(file, ${(col.fileSizeLimit!10485760)?c})"
        :on-success="(res: any) => handleMultiUploadSuccess(res, '${col.fieldName}')"
      >
        <span class="text-2xl">+</span>
      </el-upload>
<#elseif col.isString || col.isUuid>
      <el-input
        v-model="form.${col.fieldName}"
        placeholder="请输入${col.columnName}"
        clearable
      />
<#elseif col.isText || col.isJson || col.isGeo>
      <el-input
        v-model="form.${col.fieldName}"
        type="textarea"
        :rows="3"
        placeholder="请输入${col.columnName}"
      />
<#elseif col.isInteger || col.isDecimal>
      <el-input-number
        v-model="form.${col.fieldName}"
        :precision="<#if col.isDecimal>${col.scale?c}<#else>0</#if>"
        placeholder="请输入${col.columnName}"
        class="w-full"
      />
<#elseif col.isBoolean>
      <el-switch
        v-model="form.${col.fieldName}"
        :active-value="true"
        :inactive-value="false"
      />
<#elseif col.isDate || col.isDateTime || col.isTimestampTz>
      <el-date-picker
        v-model="form.${col.fieldName}"
        type="${col.dateType}"
        value-format="${col.dateValueFormat}"
        placeholder="请选择${col.columnName}"
        class="w-full"
      />
<#elseif col.isEnum>
      <el-select
        v-model="form.${col.fieldName}"
        placeholder="请选择${col.columnName}"
        clearable
        class="w-full"
      >
        <#list col.options as opt>
        <el-option label="${opt.label}" value="${opt.value}" />
        </#list>
      </el-select>
<#elseif col.isArray>
      <el-select
        v-model="form.${col.fieldName}"
        multiple
        allow-create
        filterable
        default-first-option
        collapse-tags
        placeholder="请输入${col.columnName}"
        clearable
        class="w-full"
      />
</#if>
    </el-form-item>
</#list>
  </el-form>
</template>
