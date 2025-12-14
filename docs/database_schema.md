# 数据库选型与表设计

## 1. 选型概述
- **核心交易/关系数据：MySQL 8.0**
  - 支持事务、外键与 JSON，适合高一致性要求的用户、权限、问答、审核等结构化数据。
- **搜索与语义检索：Elasticsearch 8.x**
  - 用于问答全文索引、相似问题召回与联想词，配合自定义分词词典（同义词/停用词）。
- **缓存与会话：Redis 7.x**
  - 存储登录会话、热点问答、推荐候选、限流计数与配置热更新。
- **对象存储（可选）：OSS/S3 兼容**
  - 存放图片/证照等非结构化数据，表中仅保存 URL 与元数据。

## 2. 模块化表设计（MySQL）
下表对应 `sql/schema.sql` 中的建表脚本，可按需拆分库/分库分表。

### 2.1 用户与权限
- `user`：用户基础信息与登录凭据（含老年友好字段如字号偏好）。
- `role` / `permission`：角色、权限资源定义。
- `user_role` / `role_permission`：多对多关联。
- `expert_profile`：专家资质与认证状态。
- `privacy_setting`：用户隐私展示开关。

### 2.2 问答互动
- `question`：提问主体，含标签计数、状态与采纳信息。
- `answer`：答案内容、引用来源、采纳与点赞统计。
- `comment`：评论/追问；`comment` 以 `parent_id` 支持楼中楼。
- `topic_tag` / `question_tag`：标签字典与问题-标签关联。
- `answer_citation`：答案引用来源列表。
- `engagement`：点赞、收藏等互动记录。
- `report`：举报记录。
- `audit_log`：审核流转记录。

### 2.3 知识库与来源
- `knowledge_entry`：结构化健康条目。
- `reference_source`：权威来源元数据。
- `entry_version`：条目版本历史。

### 2.4 搜索配置与日志
- `synonym_dict` / `stopword_dict`：搜索词典维护。
- `search_weight`：多字段权重与阈值配置。
- `search_log`：搜索行为日志（用于分析与推荐）。

### 2.5 推荐与画像
- `user_profile`：画像特征（年龄段、慢病标签、兴趣偏好）。
- `behavior_log`：行为埋点，用于推荐与风控。

### 2.6 运营与风控
- `rate_limit_rule`：频率限制配置。
- `sanction`：账号处置记录（禁言/拉黑等）。
- `announcement`：公告与运营位配置。

### 2.7 消息通知
- `notification`：通知消息表。
- `follow`：关注关系表。

## 3. 建表脚本
详见 `sql/schema.sql`，包含字段、约束、索引及关键默认值。
