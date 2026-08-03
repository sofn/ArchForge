import { $t } from "@/plugins/i18n";

export default {
  path: "/${tableCode}",
  redirect: "/${tableCode}/index",
  meta: {
    icon: "ri/table-line",
    title: "${tableName}",
    rank: ${moduleCode?c}
  },
  children: [
    {
      path: "/${tableCode}/index",
      name: "${className}",
      component: () => import("@/views/${tableCode}/index.vue"),
      meta: {
        title: "${tableName}"
      }
    }
  ]
} satisfies RouteConfigsTable;
