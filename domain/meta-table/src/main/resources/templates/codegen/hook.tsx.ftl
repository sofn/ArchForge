import dayjs from "dayjs";
import { h, onMounted, reactive, ref, toRaw } from "vue";
import type { PaginationProps } from "@pureadmin/table";
import { message } from "@/utils/message";
import { hasPerms } from "@/utils/auth";
import { addDialog } from "@/components/ReDialog";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import ${className}Form from "../form/index.vue";
import type {
  ${className}CreateRequest,
  ${className}UpdateRequest,
  ${className}ListRequest,
  ${className}Response
} from "./types";
import { init${className}Form } from "./types";
import {
  get${className}List,
  create${className},
  update${className},
  delete${className}
} from "@/api/${tableCode}";
import Delete from "~icons/ep/delete";
import EditPen from "~icons/ep/edit-pen";
import AddFill from "~icons/ri/add-circle-line";

export function use${className}() {
  const form = reactive<${className}ListRequest>({
    currentPage: 1,
    pageSize: 10,
    keyword: ""
  });
  const formRef = ref();
  const dataList = ref<${className}Response[]>([]);
  const loading = ref(true);
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });

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
      minWidth: 140
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
    loading,
    columns,
    dataList,
    pagination,
    hasPerms,
    onSearch,
    resetForm,
    openDialog,
    handleDelete,
    handleSizeChange,
    handleCurrentChange
  };
}
