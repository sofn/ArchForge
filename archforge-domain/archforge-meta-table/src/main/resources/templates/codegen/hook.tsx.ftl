import dayjs from "dayjs";
import { h, onMounted, reactive, ref, toRaw } from "vue";
import type { PaginationProps } from "@pureadmin/table";
import { message } from "@/utils/message";
import { hasPerms } from "@/utils/auth";
import { addDialog } from "@/components/ReDialog";
import ${className}Form from "../form/index.vue";
import type {
  ${className}UpdateRequest,
  ${className}ListRequest,
  ${className}Response
} from "./types";
import { init${className}Form } from "./types";
import {
  get${className}List,
  create${className},
  update${className},
  delete${className},
  export${className}Data,
  import${className}Data
} from "@/api/${tableCode}";

export function use${className}() {
  const form = reactive<${className}ListRequest>({
    currentPage: 1,
    pageSize: 10,
    keyword: ""
  });
  const formRef = ref();
  const importInput = ref<HTMLInputElement | null>(null);
  const dataList = ref<${className}Response[]>([]);
  const loading = ref(true);
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });

<#list listVisibleColumns as col>
<#if col.isEnum && col.hasOptions>
  const ${col.fieldName}Options = [
    <#list col.options as opt>{ label: "${opt.label}", value: "${opt.value}" }<#if opt_has_next>,</#if></#list>
  ];
</#if>
</#list>

  const columns: TableColumnList = [
    {
      label: "ID",
      prop: "id",
      width: 80
    },
<#list listVisibleColumns as col>
    {
      label: "${col.columnName}",
      prop: "${col.fieldName}",
      minWidth: 140<#if col.isDate || col.isDateTime || col.isTimestampTz>,
      formatter: ({ ${col.fieldName} }) => ${col.fieldName} ? dayjs(${col.fieldName}).format("${col.dateValueFormat}") : ""</#if><#if col.isArray || col.isMultiImage>,
      formatter: ({ ${col.fieldName} }) => ${col.fieldName} ? JSON.stringify(${col.fieldName}) : ""</#if><#if col.isEnum && col.hasOptions>,
      formatter: ({ ${col.fieldName} }) => ${col.fieldName}Options.find(o => String(o.value) === String(${col.fieldName}))?.label ?? ${col.fieldName}</#if><#if col.isReference>,
      formatter: ({ ${col.fieldName}, ${col.fieldName}_display }) => ${col.fieldName}_display ?? ${col.fieldName}</#if>
    },
</#list>
    {
      label: "创建时间",
      prop: "createTime",
      minWidth: 160,
      formatter: ({ createTime }) =>
        createTime ? dayjs(createTime).format("YYYY-MM-DD HH:mm:ss") : ""
    },
    {
      label: "操作",
      fixed: "right",
      width: 200,
      slot: "operation"
    }
  ];

  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    onSearch();
  }

  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    onSearch();
  }

  function resetForm(formEl) {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  }

  async function handleExport(format = "EXCEL") {
    try {
      const blob = await export${className}Data(format);
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      const suffix = format.toLowerCase() === "csv" ? ".csv" : format.toLowerCase() === "json" ? ".json" : ".xlsx";
      link.download = "${tableCode}_${tableId?c}" + suffix;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(link.href);
      message("导出成功", { type: "success" });
    } catch {
      message("导出失败", { type: "error" });
    }
  }

  async function handleImport(event: Event) {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (!file) return;
    try {
      const { data, code, message: msg } = await import${className}Data(file, "CSV");
      if (code === 0) {
<#noparse>        message(`导入完成：总记录 ${data?.total ?? 0}，成功 ${data?.success ?? 0}`, {
          type: "success"
        });</#noparse>
        onSearch();
      } else {
        message(msg || "导入失败", { type: "error" });
      }
    } catch {
      message("导入失败", { type: "error" });
    } finally {
      target.value = "";
    }
  }

  async function onSearch() {
    loading.value = true;
    const { code, data } = await get${className}List({
      ...toRaw(form),
      currentPage: pagination.currentPage,
      pageSize: pagination.pageSize
    });
    if (code === 0) {
      dataList.value = data.list;
      pagination.total = data.total;
      pagination.pageSize = data.pageSize;
      pagination.currentPage = data.currentPage;
    }
    setTimeout(() => {
      loading.value = false;
    }, 300);
  }

  async function openDialog(title = "新增", row?: ${className}Response) {
    let formInline: ${className}UpdateRequest = init${className}Form() as ${className}UpdateRequest;
    if (title === "修改" && row?.id) {
      formInline = { ...row };
    }

    addDialog({
      title: `${r"${title}"}${tableName}`,
      props: {
        formInline
      },
      width: "60%",
      draggable: true,
      fullscreen: false,
      closeOnClickModal: false,
      contentRenderer: () => h(${className}Form, { ref: formRef, formInline }),
      beforeSure: (done, { options }) => {
        const FormRef = formRef.value.getRef();
        const curData = options.props.formInline as ${className}UpdateRequest;
        FormRef.validate(async valid => {
          if (valid) {
            if (title === "新增") {
              await create${className}(curData);
              message(`新增${tableName}成功`, { type: "success" });
            } else {
              const updateData: ${className}UpdateRequest = { ...curData, id: row.id };
              await update${className}(row.id, updateData);
              message(`修改${tableName}成功`, { type: "success" });
            }
            done();
            onSearch();
          }
        });
      }
    });
  }

  async function handleDelete(row: ${className}Response) {
    await delete${className}(row.id);
    message(`删除${tableName}成功`, { type: "success" });
    onSearch();
  }

  onMounted(() => {
    onSearch();
  });

  return {
    form,
    formRef,
    importInput,
    loading,
    columns,
    dataList,
    pagination,
    hasPerms,
    onSearch,
    resetForm,
    openDialog,
    handleDelete,
    handleExport,
    handleImport,
    handleSizeChange,
    handleCurrentChange
  };
}
