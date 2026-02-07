/* ==========================================================

 * 智慧医养健康问答社区（语义搜索与智能推荐）- MySQL 8.0
 * 说明：
 * 1) 已允许修正拼写，并补齐所有字段/状态注释
 * 2) 统一字符集：utf8mb4，存储引擎：InnoDB
 * 3) 统一时间字段：created_at / updated_at（自动维护）
 * 4) 增加必要的唯一约束与索引（避免重复点赞/关注/收藏/标签绑定）
 * ========================================================== */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================================
-- 0. RBAC（不改变原有语义，仅做命名规范与注释补齐）
-- ==========================================================

DROP TABLE IF EXISTS `role_permission`;
DROP TABLE IF EXISTS `user_role`;
DROP TABLE IF EXISTS `permission`;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
                        `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
                        `username` VARCHAR(64) NOT NULL COMMENT '用户名（登录名，可用于后台账号）',
                        `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号（可为空；可加唯一约束）',
                        `password` VARCHAR(100) NOT NULL COMMENT '密码哈希（不可明文）',
                        `status` TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态：1-启用，0-禁用',
                        `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称（前台展示）',

                        `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
                        `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',

                        `following_count` INT NOT NULL DEFAULT 0 COMMENT '关注数（冗余统计，可由user_follow聚合/定时写入）',
                        `follower_count` INT NOT NULL DEFAULT 0 COMMENT '粉丝数（冗余统计）',
                        `like_received_count` INT NOT NULL DEFAULT 0 COMMENT '获赞数（冗余统计，来自内容点赞累计）',

                        `expert_status` TINYINT NOT NULL DEFAULT 1 COMMENT '专家状态：1-未认证，2-审核中，3-已认证，4-驳回，5-停用/取消认证',
                        `expert_verified_at` DATETIME DEFAULT NULL COMMENT '专家认证通过时间（expert_status=3时写入）',

                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_user_username` (`username`),
                        KEY `idx_user_phone` (`phone`),
                        KEY `idx_user_expert_status` (`expert_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表（含普通用户/专家/后台账号）';

CREATE TABLE `role` (
                        `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID（主键）',
                        `code` VARCHAR(50) NOT NULL COMMENT '角色编码（如：admin/ops/user/expert）',
                        `name` VARCHAR(64) NOT NULL COMMENT '角色名称（展示用）',
                        `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述/备注',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_role_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE `permission` (
                              `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID（主键）',
                              `code` VARCHAR(100) NOT NULL COMMENT '权限编码（后端鉴权标识）',
                              `name` VARCHAR(128) NOT NULL COMMENT '权限名称（展示用）',
                              `type` VARCHAR(16) NOT NULL COMMENT '权限类型（建议：menu/button/api）',
                              `path_or_api` VARCHAR(255) DEFAULT NULL COMMENT '菜单路由或API路径（与type相关）',
                              `method` VARCHAR(16) DEFAULT NULL COMMENT 'HTTP方法（type=api时使用，如GET/POST/PUT/DELETE）',
                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_permission_code` (`code`),
                              KEY `idx_permission_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表（菜单/按钮/API）';

CREATE TABLE `user_role` (
                             `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
                             `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
                             `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             PRIMARY KEY (`user_id`, `role_id`),
                             KEY `idx_user_role_role` (`role_id`),
                             CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                             CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

CREATE TABLE `role_permission` (
                                   `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
                                   `permission_id` BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
                                   `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`role_id`, `permission_id`),
                                   KEY `idx_role_perm_perm` (`permission_id`),
                                   CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role`(`id`) ON DELETE CASCADE,
                                   CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- ==========================================================
-- 1. 问答：分类/标签/问题/回答/评论/点赞/收藏
-- ==========================================================

DROP TABLE IF EXISTS `qa_vote`;
DROP TABLE IF EXISTS `qa_favorite`;
DROP TABLE IF EXISTS `qa_comment`;
DROP TABLE IF EXISTS `qa_answer`;
DROP TABLE IF EXISTS `qa_question_tag`;
DROP TABLE IF EXISTS `qa_tag`;
DROP TABLE IF EXISTS `qa_question`;
DROP TABLE IF EXISTS `qa_category`;

CREATE TABLE `qa_category` (
                               `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID（主键）',
                               `name` VARCHAR(50) NOT NULL COMMENT '分类名称（如：疾病问答/养生保健）',
                               `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父分类ID（用于二级分类）',
                               `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标URL',
                               `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
                               `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
                               `sort` INT NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
                               `delete_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-删除',
                               `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_qa_category_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答分类表';

CREATE TABLE `qa_tag` (
                          `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID（主键）',
                          `name` VARCHAR(50) NOT NULL COMMENT '标签名称（如：失眠/压力调节）',
                          `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
                          `use_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数（冗余统计）',
                          `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_qa_tag_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表（问答/知识库可复用）';

CREATE TABLE `qa_question` (
                               `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '问题ID（主键）',
                               `user_id` BIGINT UNSIGNED NOT NULL COMMENT '提问用户ID → user.id',
                               `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID → qa_category.id',
                               `topic_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '专题ID → qa_topic.id（可为空）',
                               `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
                               `content` TEXT DEFAULT NULL COMMENT '问题详情（可选）',

                               `status` TINYINT NOT NULL DEFAULT 2 COMMENT '内容状态：1-已发布，2-待审核，3-驳回，4-下架',
                               `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因（status=3时填写）',

                               `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量（冗余统计）',
                               `answer_count` INT NOT NULL DEFAULT 0 COMMENT '回答数（冗余统计）',
                               `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数（冗余统计）',
                               `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数（冗余统计）',

                               `accepted_answer_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '采纳回答ID（可选，不做采纳可忽略）',
                               `accepted_at` DATETIME DEFAULT NULL COMMENT '采纳时间',
                               `last_active_at` DATETIME DEFAULT NULL COMMENT '最后活跃时间（用于“最新/最热”排序）',

                               `delete_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-删除',
                               `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                               PRIMARY KEY (`id`),
                               KEY `idx_qa_question_user` (`user_id`),
                               KEY `idx_qa_question_category` (`category_id`),
                               KEY `idx_qa_question_status` (`status`),
                               KEY `idx_qa_question_last_active` (`last_active_at`),
                               CONSTRAINT `fk_qa_question_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题表';

CREATE TABLE `qa_question_tag` (
                                   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `question_id` BIGINT UNSIGNED NOT NULL COMMENT '问题ID → qa_question.id',
                                   `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID → qa_tag.id',
                                   `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_question_tag` (`question_id`, `tag_id`),
                                   KEY `idx_question_tag_tag` (`tag_id`),
                                   CONSTRAINT `fk_question_tag_question` FOREIGN KEY (`question_id`) REFERENCES `qa_question`(`id`) ON DELETE CASCADE,
                                   CONSTRAINT `fk_question_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `qa_tag`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题-标签关联表';

CREATE TABLE `qa_answer` (
                             `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '回答ID（主键）',
                             `question_id` BIGINT UNSIGNED NOT NULL COMMENT '问题ID → qa_question.id',
                             `user_id` BIGINT UNSIGNED NOT NULL COMMENT '回答用户ID → user.id',
                             `content` TEXT NOT NULL COMMENT '回答内容',

                             `status` TINYINT NOT NULL DEFAULT 2 COMMENT '内容状态：1-已发布，2-待审核，3-驳回，4-下架',
                             `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因（status=3时填写）',

                             `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数（冗余统计）',
                             `is_anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名发布：1-匿名，0-不匿名',

                             `delete_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-删除',
                             `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                             PRIMARY KEY (`id`),
                             KEY `idx_qa_answer_question` (`question_id`),
                             KEY `idx_qa_answer_user` (`user_id`),
                             KEY `idx_qa_answer_status` (`status`),
                             CONSTRAINT `fk_qa_answer_question` FOREIGN KEY (`question_id`) REFERENCES `qa_question`(`id`) ON DELETE CASCADE,
                             CONSTRAINT `fk_qa_answer_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回答表';

CREATE TABLE `qa_comment` (
                              `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID（主键）',
                              `biz_type` TINYINT NOT NULL COMMENT '目标类型：1-问题，2-回答',
                              `biz_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID（对应qa_question/qa_answer）',
                              `user_id` BIGINT UNSIGNED NOT NULL COMMENT '评论用户ID → user.id',
                              `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID（楼中楼；为空表示一级评论）',
                              `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',

                              `status` TINYINT NOT NULL DEFAULT 2 COMMENT '内容状态：1-已发布，2-待审核，3-驳回，4-下架',
                              `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因（status=3时填写）',

                              `delete_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-删除',
                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                              PRIMARY KEY (`id`),
                              KEY `idx_qa_comment_biz` (`biz_type`, `biz_id`),
                              KEY `idx_qa_comment_user` (`user_id`),
                              KEY `idx_qa_comment_parent` (`parent_id`),
                              KEY `idx_qa_comment_status` (`status`),
                              CONSTRAINT `fk_qa_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表（楼中楼，多态：问题/回答）';

CREATE TABLE `qa_favorite` (
                               `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID（主键）',
                               `question_id` BIGINT UNSIGNED NOT NULL COMMENT '问题ID → qa_question.id',
                               `user_id` BIGINT UNSIGNED NOT NULL COMMENT '收藏用户ID → user.id',
                               `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_fav_question_user` (`question_id`, `user_id`),
                               KEY `idx_fav_user` (`user_id`),
                               CONSTRAINT `fk_fav_question` FOREIGN KEY (`question_id`) REFERENCES `qa_question`(`id`) ON DELETE CASCADE,
                               CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题收藏表';

CREATE TABLE `qa_vote` (
                           `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞ID（主键）',
                           `biz_type` TINYINT NOT NULL COMMENT '目标类型：1-问题，2-回答，3-评论',
                           `biz_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID（对应qa_question/qa_answer/qa_comment）',
                           `user_id` BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID → user.id',
                           `vote_type` TINYINT NOT NULL DEFAULT 1 COMMENT '投票类型：1-点赞（后续可扩展2-点踩）',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uk_vote_unique` (`biz_type`, `biz_id`, `user_id`),
                           KEY `idx_vote_user` (`user_id`),
                           KEY `idx_vote_biz` (`biz_type`, `biz_id`),
                           CONSTRAINT `fk_vote_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表（多态：问题/回答/评论）';

-- ==========================================================
-- 2. 专题：qa_topic / 关联分类 / 关注
-- ==========================================================

DROP TABLE IF EXISTS `qa_topic_follow`;
DROP TABLE IF EXISTS `qa_topic_category`;
DROP TABLE IF EXISTS `qa_topic`;

CREATE TABLE `qa_topic` (
                            `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '专题ID（主键）',
                            `title` VARCHAR(100) NOT NULL COMMENT '专题标题',
                            `subtitle` VARCHAR(200) DEFAULT NULL COMMENT '专题副标题（原subtutle已修正）',
                            `cover_img` VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
                            `intro` TEXT DEFAULT NULL COMMENT '专题介绍',

                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '内容状态：1-已发布，2-待审核，3-驳回，4-下架',

                            `follow_count` INT NOT NULL DEFAULT 0 COMMENT '关注数（冗余统计）',
                            `question_count` INT NOT NULL DEFAULT 0 COMMENT '专题内问题数（冗余统计）',
                            `today_new_count` INT NOT NULL DEFAULT 0 COMMENT '今日新增数量（可选冗余）',

                            `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID → user.id（运营/管理员）',
                            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                            PRIMARY KEY (`id`),
                            KEY `idx_topic_status` (`status`),
                            KEY `idx_topic_created_by` (`created_by`),
                            CONSTRAINT `fk_topic_created_by` FOREIGN KEY (`created_by`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题表';

CREATE TABLE `qa_topic_category` (
                                     `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                     `topic_id` BIGINT UNSIGNED NOT NULL COMMENT '专题ID → qa_topic.id',
                                     `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID → qa_category.id',
                                     `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_topic_category` (`topic_id`, `category_id`),
                                     KEY `idx_topic_category_category` (`category_id`),
                                     CONSTRAINT `fk_topic_category_topic` FOREIGN KEY (`topic_id`) REFERENCES `qa_topic`(`id`) ON DELETE CASCADE,
                                     CONSTRAINT `fk_topic_category_category` FOREIGN KEY (`category_id`) REFERENCES `qa_category`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题-分类关联表';

CREATE TABLE `qa_topic_follow` (
                                   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `topic_id` BIGINT UNSIGNED NOT NULL COMMENT '专题ID → qa_topic.id',
                                   `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关注用户ID → user.id',
                                   `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_topic_follow` (`topic_id`, `user_id`),
                                   KEY `idx_topic_follow_user` (`user_id`),
                                   CONSTRAINT `fk_topic_follow_topic` FOREIGN KEY (`topic_id`) REFERENCES `qa_topic`(`id`) ON DELETE CASCADE,
                                   CONSTRAINT `fk_topic_follow_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题关注表';

-- ==========================================================
-- 3. 关注/粉丝、浏览历史、搜索日志、通知
-- ==========================================================

DROP TABLE IF EXISTS `notify_message`;
DROP TABLE IF EXISTS `search_query_log`;
DROP TABLE IF EXISTS `user_browse_history`;
DROP TABLE IF EXISTS `user_follow`;
DROP TABLE IF EXISTS `user_stat`;

CREATE TABLE `user_follow` (
                               `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `follower_id` BIGINT UNSIGNED NOT NULL COMMENT '关注者ID → user.id',
                               `followee_id` BIGINT UNSIGNED NOT NULL COMMENT '被关注者ID → user.id',
                               `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_user_follow` (`follower_id`, `followee_id`),
                               KEY `idx_follow_followee` (`followee_id`),
                               CONSTRAINT `fk_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                               CONSTRAINT `fk_follow_followee` FOREIGN KEY (`followee_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表（关注/粉丝）';

CREATE TABLE `user_browse_history` (
                                       `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                       `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID → user.id',
                                       `biz_type` TINYINT NOT NULL COMMENT '目标类型：1-问题，2-知识库，3-专题',
                                       `biz_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID（对应实体ID）',
                                       `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_browse_user_time` (`user_id`, `created_at`),
                                       KEY `idx_browse_biz` (`biz_type`, `biz_id`),
                                       CONSTRAINT `fk_browse_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览历史表';

CREATE TABLE `search_query_log` (
                                    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID（可为空表示匿名搜索）',
                                    `query_text` VARCHAR(300) NOT NULL COMMENT '搜索词',
                                    `search_type` TINYINT NOT NULL DEFAULT 1 COMMENT '搜索类型：1-混合，2-仅问题，3-仅知识库，4-仅专题（可选）',
                                    `hit_count` INT NOT NULL DEFAULT 0 COMMENT '命中数量（结果条数）',
                                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_search_user_time` (`user_id`, `created_at`),
                                    KEY `idx_search_query` (`query_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索日志表（用于搜索历史与推荐）';

CREATE TABLE `notify_message` (
                                  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID（主键）',
                                  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID → user.id',
                                  `type` TINYINT NOT NULL COMMENT '通知类型：1-系统，2-点赞，3-收藏，4-关注，5-评论，6-回答，7-审核结果',
                                  `biz_type` TINYINT DEFAULT NULL COMMENT '业务类型：1问题2回答3评论4知识库5专题6专家认证',
                                  `biz_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务ID（对应实体ID）',
                                  `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
                                  `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容/摘要',
                                  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
                                  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `read_at` DATETIME DEFAULT NULL COMMENT '阅读时间',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_notify_receiver_read` (`receiver_id`, `is_read`, `created_at`),
                                  CONSTRAINT `fk_notify_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知表';

CREATE TABLE `user_stat` (
                             `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（主键） → user.id',
                             `question_count` INT NOT NULL DEFAULT 0 COMMENT '提问数',
                             `answer_count` INT NOT NULL DEFAULT 0 COMMENT '回答数',
                             `like_received_count` INT NOT NULL DEFAULT 0 COMMENT '获赞数（累计）',
                             `follower_count` INT NOT NULL DEFAULT 0 COMMENT '粉丝数',
                             `following_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
                             `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`user_id`),
                             CONSTRAINT `fk_user_stat_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户统计汇总表（可选，便于展示“我的”页面数据）';

-- ==========================================================
-- 4. 知识库：分类/条目 + 条目标签（复用qa_tag）
-- ==========================================================

DROP TABLE IF EXISTS `kb_entry_tag`;
DROP TABLE IF EXISTS `kb_entry`;
DROP TABLE IF EXISTS `kb_category`;

CREATE TABLE `kb_category` (
                               `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '知识库分类ID（主键）',
                               `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父分类ID（支持二级分类）',
                               `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
                               `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
                               `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标/图片URL',
                               `sort` INT NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
                               `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
                               `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_kb_category_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库分类表';

CREATE TABLE `kb_entry` (
                            `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '知识库条目ID（主键）',
                            `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID → kb_category.id',
                            `title` VARCHAR(200) NOT NULL COMMENT '标题',
                            `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
                            `content` MEDIUMTEXT NOT NULL COMMENT '正文（用于ES全文索引）',
                            `content_ref` VARCHAR(64) DEFAULT NULL COMMENT '外部正文引用（为后期MongoDB/对象存储预留）',
                            `source` VARCHAR(200) DEFAULT NULL COMMENT '来源/引用',
                            `author_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '作者/创建人ID → user.id',

                            `status` TINYINT NOT NULL DEFAULT 2 COMMENT '内容状态：1-已发布，2-待审核，3-驳回，4-下架',

                            `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数（冗余统计）',
                            `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数（冗余统计）',
                            `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数（冗余统计）',

                            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                            PRIMARY KEY (`id`),
                            KEY `idx_kb_entry_category` (`category_id`),
                            KEY `idx_kb_entry_status` (`status`),
                            KEY `idx_kb_entry_author` (`author_user_id`),
                            CONSTRAINT `fk_kb_entry_category` FOREIGN KEY (`category_id`) REFERENCES `kb_category`(`id`) ON DELETE SET NULL,
                            CONSTRAINT `fk_kb_entry_author` FOREIGN KEY (`author_user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库条目表（科普/常见误区/FAQ等）';

CREATE TABLE `kb_entry_tag` (
                                `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `entry_id` BIGINT UNSIGNED NOT NULL COMMENT '知识库条目ID → kb_entry.id',
                                `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID → qa_tag.id（复用标签体系）',
                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_kb_entry_tag` (`entry_id`, `tag_id`),
                                KEY `idx_kb_entry_tag_tag` (`tag_id`),
                                CONSTRAINT `fk_kb_entry_tag_entry` FOREIGN KEY (`entry_id`) REFERENCES `kb_entry`(`id`) ON DELETE CASCADE,
                                CONSTRAINT `fk_kb_entry_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `qa_tag`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库条目-标签关联表（用于筛选/推荐/混合搜索增强）';

-- ==========================================================
-- 5. 专家体系：资料 + 认证申请
-- ==========================================================

DROP TABLE IF EXISTS `expert_apply`;
DROP TABLE IF EXISTS `expert_profile`;

CREATE TABLE `expert_profile` (
                                  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID → user.id（专家本人）',
                                  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
                                  `organization` VARCHAR(100) DEFAULT NULL COMMENT '机构/医院/学校',
                                  `title` VARCHAR(100) DEFAULT NULL COMMENT '职称/头衔',
                                  `expertise` VARCHAR(255) DEFAULT NULL COMMENT '擅长领域（可用于搜索/推荐）',
                                  `verified_at` DATETIME DEFAULT NULL COMMENT '认证通过时间',
                                  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_expert_profile_user` (`user_id`),
                                  CONSTRAINT `fk_expert_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家资料表（通过认证后展示用）';

CREATE TABLE `expert_apply` (
                                `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '专家认证申请ID（主键）',
                                `user_id` BIGINT UNSIGNED NOT NULL COMMENT '申请人用户ID → user.id',
                                `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
                                `organization` VARCHAR(100) DEFAULT NULL COMMENT '机构/医院/学校',
                                `title` VARCHAR(100) DEFAULT NULL COMMENT '职称/头衔',
                                `expertise` VARCHAR(255) DEFAULT NULL COMMENT '擅长领域',
                                `proof_urls` JSON DEFAULT NULL COMMENT '证明材料URL列表（JSON数组，如["url1","url2"]）',

                                `status` TINYINT NOT NULL DEFAULT 1 COMMENT '申请状态：1-待审，2-通过，3-驳回',
                                `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因（status=3时填写）',
                                `reviewer_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID → user.id（运营/管理员）',
                                `review_at` DATETIME DEFAULT NULL COMMENT '审核时间',

                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
                                `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                PRIMARY KEY (`id`),
                                KEY `idx_expert_apply_user` (`user_id`),
                                KEY `idx_expert_apply_status` (`status`),
                                CONSTRAINT `fk_expert_apply_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                                CONSTRAINT `fk_expert_apply_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家认证申请表（提交资质→审核→通过/驳回）';

-- ==========================================================
-- 6. 审核与治理：敏感词 / 审核单 / 举报单
-- ==========================================================

DROP TABLE IF EXISTS `cms_audit`;
DROP TABLE IF EXISTS `cms_report`;
DROP TABLE IF EXISTS `cms_sensitive_word`;

CREATE TABLE `cms_sensitive_word` (
                                      `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '敏感词ID（主键）',
                                      `word` VARCHAR(100) NOT NULL COMMENT '敏感词文本',
                                      `level` TINYINT NOT NULL COMMENT '敏感级别：1-疑似需复审，2-强拦截（直接拒绝发布）',
                                      `category` VARCHAR(50) DEFAULT NULL COMMENT '敏感词分类（广告/辱骂/涉政/涉黄等）',
                                      `hit_action_desc` VARCHAR(200) DEFAULT NULL COMMENT '命中提示文案（前端提示/运营说明）',
                                      `reason_template` VARCHAR(255) DEFAULT NULL COMMENT '拦截原因模板（强拦截时返回给用户）',
                                      `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
                                      `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_sensitive_word` (`word`),
                                      KEY `idx_sensitive_level` (`level`),
                                      KEY `idx_sensitive_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词库（强敏感自动拦截，疑似进入人工审核）';

CREATE TABLE `cms_audit` (
                             `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审核单ID（主键）',
                             `biz_type` TINYINT NOT NULL COMMENT '业务类型：1-问题，2-回答，3-评论，4-知识库，5-专家认证',
                             `biz_id` BIGINT UNSIGNED NOT NULL COMMENT '业务ID（对应实体ID）',
                             `trigger_source` TINYINT NOT NULL COMMENT '触发来源：1-机器疑似，2-举报触发',
                             `audit_type` TINYINT NOT NULL COMMENT '审核类型：1-机器审核，2-人工审核',
                             `audit_status` TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态：1-待审，2-通过，3-驳回',
                             `action` VARCHAR(20) DEFAULT NULL COMMENT '处理动作（pass/reject/block等）',
                             `model_label` VARCHAR(50) DEFAULT NULL COMMENT '模型标签（spam/abuse/medical等，可空）',
                             `model_score` DECIMAL(6,4) DEFAULT NULL COMMENT '模型置信度（0~1）',
                             `hit_detail` JSON DEFAULT NULL COMMENT '命中详情（JSON，记录命中词/规则/分数等）',
                             `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因（audit_status=3时填写）',
                             `submit_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '内容提交者/作者ID → user.id',
                             `auditor_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID → user.id（人工审核时）',
                             `audited_at` DATETIME DEFAULT NULL COMMENT '审核时间',
                             `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`),
                             KEY `idx_audit_biz` (`biz_type`, `biz_id`),
                             KEY `idx_audit_status` (`audit_status`),
                             KEY `idx_audit_created` (`created_at`),
                             CONSTRAINT `fk_audit_submit_user` FOREIGN KEY (`submit_user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
                             CONSTRAINT `fk_audit_auditor` FOREIGN KEY (`auditor_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核单（只处理“机器疑似/举报触发”的人工治理流程）';

CREATE TABLE `cms_report` (
                              `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '举报ID（主键）',
                              `biz_type` TINYINT NOT NULL COMMENT '业务类型：1-问题，2-回答，3-评论，4-知识库',
                              `biz_id` BIGINT UNSIGNED NOT NULL COMMENT '业务ID（对应实体ID）',
                              `reason_type` TINYINT DEFAULT NULL COMMENT '举报原因类型（枚举，可选：广告/辱骂/不实信息等）',
                              `reporter_id` BIGINT UNSIGNED NOT NULL COMMENT '举报人ID → user.id',
                              `reason_code` VARCHAR(50) DEFAULT NULL COMMENT '举报原因编码（可选，用于运营统计）',
                              `reason_detail` VARCHAR(500) DEFAULT NULL COMMENT '举报补充说明（用户输入）',

                              `status` TINYINT NOT NULL DEFAULT 1 COMMENT '处理状态：1-待处理，2-已处理，3-驳回/不成立',
                              `handler_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人ID → user.id（运营/管理员）',
                              `handle_action` TINYINT DEFAULT NULL COMMENT '处理动作：1-下架内容，2-警告用户，3-封禁/禁言，4-不处理',
                              `handle_result` VARCHAR(255) DEFAULT NULL COMMENT '处理说明（处理结论/备注）',
                              `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',

                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                              PRIMARY KEY (`id`),
                              KEY `idx_report_biz` (`biz_type`, `biz_id`),
                              KEY `idx_report_status` (`status`),
                              KEY `idx_report_reporter` (`reporter_id`),
                              CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                              CONSTRAINT `fk_report_handler` FOREIGN KEY (`handler_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报单（被举报内容进入人工治理流程）';

-- ==========================================================
-- 7. 推荐：行为埋点 / 兴趣画像 / 日统计
-- ==========================================================

DROP TABLE IF EXISTS `stat_daily`;
DROP TABLE IF EXISTS `rec_user_interest`;
DROP TABLE IF EXISTS `rec_user_behavior`;

CREATE TABLE `rec_user_behavior` (
                                     `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                     `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID → user.id',
                                     `action_type` TINYINT NOT NULL COMMENT '行为类型：1-浏览，2-点赞，3-收藏，4-关注，5-搜索，6-点击',
                                     `target_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID（对应内容ID）',
                                     `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID（问答分类，用于兴趣建模）',
                                     `weight` DECIMAL(6,2) NOT NULL DEFAULT 1.00 COMMENT '行为权重（用于推荐打分）',
                                     `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_behavior_user_time` (`user_id`, `created_at`),
                                     KEY `idx_behavior_action` (`action_type`),
                                     CONSTRAINT `fk_behavior_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐行为埋点表';

CREATE TABLE `rec_user_interest` (
                                     `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                     `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID → user.id',
                                     `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID → qa_category.id（兴趣维度）',
                                     `score` DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '兴趣分数（越大越偏好）',
                                     `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_interest_user_category` (`user_id`, `category_id`),
                                     CONSTRAINT `fk_interest_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                                     CONSTRAINT `fk_interest_category` FOREIGN KEY (`category_id`) REFERENCES `qa_category`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户兴趣画像表（推荐用）';

CREATE TABLE `stat_daily` (
                              `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                              `stat_date` DATE NOT NULL COMMENT '统计日期',
                              `question_cnt` INT NOT NULL DEFAULT 0 COMMENT '新增问题数',
                              `answer_cnt` INT NOT NULL DEFAULT 0 COMMENT '新增回答数',
                              `comment_cnt` INT NOT NULL DEFAULT 0 COMMENT '新增评论数',
                              `active_user_cnt` INT NOT NULL DEFAULT 0 COMMENT '活跃用户数',
                              `search_cnt` INT NOT NULL DEFAULT 0 COMMENT '搜索次数',
                              `pending_audit_cnt` INT NOT NULL DEFAULT 0 COMMENT '待审核数量（人工队列）',
                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日统计表（后台看板）';

SET FOREIGN_KEY_CHECKS = 1;