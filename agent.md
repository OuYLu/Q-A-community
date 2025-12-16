# AGENTS.md — Smart-community 项目规则（给 Codex）

## 技术栈与版本
- Java 17
- Spring Boot 3.3.x
- MySQL 8.x
- Flyway 迁移：src/main/resources/db/migration

## 强制约束（必须遵守）
1) 任何数据库结构变更只能通过 Flyway 脚本完成（V{n}__desc.sql），禁止手动在 MySQL 里改表。
2) 已执行过的迁移脚本禁止修改；如需调整，新增更高版本脚本修正。
3) Controller 不写 try-catch；异常必须走全局异常处理器 GlobalExceptionHandler。
4) 所有接口统一返回 Result<T>（code/message/data），禁止直接返回裸对象或字符串（除非明确说明是临时 demo）。
5) 分层：controller / service / mapper。controller 只做参数接收与调用 service。
6) 代码命名与风格：包名 com.community...；类名驼峰；表/字段 snake_case。

## RBAC 约定
- 采用 user/role/permission + user_role/role_permission
- permission 使用字符串：例如 "admin:access"、"user:manage"、"question:create"

## 每次任务的输出格式（强制）
- 变更文件清单（新增/修改哪些文件）
- 关键代码片段（不要省略核心类/方法）
- 验收步骤：
  - 如何启动（mvn spring-boot:run 等）
  - curl/Postman 示例请求
  - 如涉及数据库：给出验证 SQL（SHOW TABLES / SELECT ...）

## 验收优先级
- 能跑起来 > 能测试通过 > 再优化结构与代码质量
