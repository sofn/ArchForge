# 任务拆分 — <需求名称>
> 拆分顺序：数据模型 → 接口协议 → 底层实现 → 上层编排 → 入口层
> 每个任务 = 可独立提交的原子变更（3-5 个文件）
> 每个任务必须精确到文件路径和函数签名

## 前置条件
- [ ] （依赖/配置等前提）

## Task 1: <任务名>
- **目标**: 一句话描述
- **涉及文件**:
    - path/to/File.java — 新增/修改，做什么
- **关键签名**:
  ```java
  public ResultDTO doSomething(Long id, String type) { }
  ```
- **验证**: `./gradlew :<module>:compileJava :<module>:checkstyleMain` + 相关测试

## Task 2: <任务名>
...
