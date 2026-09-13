# 测试策略 — <需求名称>

## 测试范围
本轮改动的风险面 → 对应验证矩阵行（见 `../README.md`）

## 测试用例
| 用例 | 类型 | 覆盖点 | 状态 |
|---|---|---|---|

## Mock / Testcontainers 策略
- 单测：mock 边界（introspector/repository）
- IT：`AbstractIntegrationTest`（真实 PG + Redis），`@Tag("slow")`

## 覆盖率目标
- 新逻辑行覆盖 ≥ 80%；分支覆盖关键路径（scope 判定/兼容矩阵类必须矩阵化）

## 跳过项与原因
