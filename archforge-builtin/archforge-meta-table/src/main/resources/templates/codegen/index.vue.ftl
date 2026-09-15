<script setup lang="ts">
import { use${className} } from "./utils/hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { ref } from "vue";
import Delete from "~icons/ep/delete";
import EditPen from "~icons/ep/edit-pen";
import AddFill from "~icons/ri/add-circle-line";
import Refresh from "~icons/ep/refresh";

defineOptions({
  name: "${className}"
});

const formRef = ref();

const {
  form,
  loading,
  columns,
  dataList,
  pagination,
  hasPerms,
  importInput,
  onSearch,
  resetForm,
  openDialog,
  handleDelete,
  handleExport,
  handleImport,
  handleSizeChange,
  handleCurrentChange
} = use${className}();
</script>

<template>
  <div class="main">
    <el-form
      ref="formRef"
      :inline="true"
      :model="form"
      class="search-form bg-bg_color w-full pl-8 pt-3 overflow-auto"
    >
      <el-form-item label="搜索：" prop="keyword">
        <el-input
          v-model="form.keyword"
          placeholder="关键词"
          clearable
          class="w-45!"
        />
      </el-form-item>
<#list searchableColumns as col>
      <el-form-item label="${col.columnName}" prop="${col.fieldName}">
<#if col.isFile || col.isImage>
        <el-input-number
          v-model="form.${col.fieldName}"
          placeholder="${col.columnName} fileId"
          class="w-30!"
        />
<#elseif col.rangeSearch>
        <div class="flex gap-2">
<#if col.isDate || col.isDateTime || col.isTimestampTz>
          <el-date-picker
            v-model="form.${col.fieldName}Start"
            type="${col.dateType}"
            value-format="${col.dateValueFormat}"
            placeholder="开始"
            class="w-30!"
          />
          <el-date-picker
            v-model="form.${col.fieldName}End"
            type="${col.dateType}"
            value-format="${col.dateValueFormat}"
            placeholder="结束"
            class="w-30!"
          />
<#elseif col.isInteger || col.isDecimal || col.isReference>
          <el-input-number
            v-model="form.${col.fieldName}Start"
            :precision="<#if col.isDecimal>${col.scale?c}<#else>0</#if>"
            placeholder="开始"
            class="w-30!"
          />
          <el-input-number
            v-model="form.${col.fieldName}End"
            :precision="<#if col.isDecimal>${col.scale?c}<#else>0</#if>"
            placeholder="结束"
            class="w-30!"
          />
<#else>
          <el-input
            v-model="form.${col.fieldName}Start"
            placeholder="开始"
            clearable
            class="w-30!"
          />
          <el-input
            v-model="form.${col.fieldName}End"
            placeholder="结束"
            clearable
            class="w-30!"
          />
</#if>
        </div>
<#elseif col.isString || col.isText || col.isJson || col.isUuid || col.isGeo>
        <el-input
          v-model="form.${col.fieldName}"
          placeholder="${col.columnName}"
          clearable
          class="w-30!"
        />
<#elseif col.isInteger || col.isDecimal || col.isReference>
        <el-input-number
          v-model="form.${col.fieldName}"
          :precision="<#if col.isDecimal>${col.scale?c}<#else>0</#if>"
          placeholder="${col.columnName}"
          class="w-30!"
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
          placeholder="${col.columnName}"
          class="w-30!"
        />
<#elseif col.isEnum>
        <el-select
          v-model="form.${col.fieldName}"
          placeholder="${col.columnName}"
          clearable
          class="w-30!"
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
          placeholder="${col.columnName}"
          clearable
          class="w-30!"
        />
</#if>
      </el-form-item>
</#list>
      <el-form-item>
        <el-button
          type="primary"
          :icon="useRenderIcon('ri/search-line')"
          :loading="loading"
          @click="onSearch"
        >
          搜索
        </el-button>
        <el-button :icon="useRenderIcon(Refresh)" @click="resetForm(formRef)">
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <PureTableBar title="${tableName}" :columns="columns" @refresh="onSearch">
      <template #buttons>
        <el-button
          v-if="hasPerms(['${tableCode}:add'])"
          type="primary"
          :icon="useRenderIcon(AddFill)"
          @click="openDialog()"
        >
          新增
        </el-button>
        <el-button
          v-if="hasPerms(['${tableCode}:export'])"
          type="primary"
          :icon="useRenderIcon('ri/download-line')"
          @click="handleExport('EXCEL')"
        >
          导出
        </el-button>
        <el-button
          v-if="hasPerms(['${tableCode}:import'])"
          type="primary"
          :icon="useRenderIcon('ri/upload-line')"
          @click="importInput?.click()"
        >
          导入
        </el-button>
        <input
          ref="importInput"
          type="file"
          accept=".xlsx,.csv,.json"
          style="display: none"
          @change="handleImport"
        />
      </template>
      <template v-slot="{ size, dynamicColumns }">
        <pure-table
          align-whole="center"
          showOverflowTooltip
          table-layout="auto"
          :loading="loading"
          :size="size"
          adaptive
          :adaptiveConfig="{ offsetBottom: 108 }"
          :data="dataList"
          :columns="dynamicColumns"
          :pagination="{ ...pagination, size }"
          :header-cell-style="{
            background: 'var(--el-fill-color-light)',
            color: 'var(--el-text-color-primary)'
          }"
          @page-size-change="handleSizeChange"
          @page-current-change="handleCurrentChange"
        >
          <template #operation="{ row }">
            <el-button
              v-if="hasPerms(['${tableCode}:edit'])"
              class="reset-margin"
              link
              type="primary"
              :size="size"
              :icon="useRenderIcon(EditPen)"
              @click="openDialog('修改', row)"
            >
              修改
            </el-button>
            <el-popconfirm
              v-if="hasPerms(['${tableCode}:remove'])"
              title="是否确认删除？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button
                  class="reset-margin"
                  link
                  type="primary"
                  :size="size"
                  :icon="useRenderIcon(Delete)"
                >
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </pure-table>
      </template>
    </PureTableBar>
  </div>
</template>

<style lang="scss" scoped>
.search-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}
</style>
