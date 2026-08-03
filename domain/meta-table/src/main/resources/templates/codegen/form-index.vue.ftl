<script setup lang="ts">
import { ref, watch } from "vue";
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
</script>

<template>
  <el-form ref="formRef" :model="form" label-width="120px">
<#list columns as col>
    <el-form-item label="${col.columnName}" prop="${col.fieldName}">
<#if col.isString>
      <el-input
        v-model="form.${col.fieldName}"
        placeholder="请输入${col.columnName}"
        clearable
      />
<#elseif col.isText || col.isJson>
      <el-input
        v-model="form.${col.fieldName}"
        type="textarea"
        :rows="3"
        placeholder="请输入${col.columnName}"
      />
<#elseif col.isFile>
      <el-input
        v-model="form.${col.fieldName}"
        placeholder="请输入${col.columnName}"
        clearable
      />
<#elseif col.isInteger || col.isDecimal>
      <el-input-number
        v-model="form.${col.fieldName}"
        :precision="<#if col.isDecimal>${col.scale}<#else>0</#if>"
        placeholder="请输入${col.columnName}"
        class="w-full"
      />
<#elseif col.isBoolean>
      <el-switch
        v-model="form.${col.fieldName}"
        :active-value="true"
        :inactive-value="false"
      />
<#elseif col.isDate || col.isDateTime>
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
</#if>
    </el-form-item>
</#list>
  </el-form>
</template>
