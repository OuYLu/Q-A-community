CREATE DATABASE IF NOT EXISTS qa_health DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE qa_health;

-- 1. 用户与权限
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    nickname VARCHAR(50),
    age TINYINT,
    gender ENUM('male','female','other') DEFAULT 'other',
    elderly_font BOOLEAN DEFAULT FALSE,
    high_contrast BOOLEAN DEFAULT FALSE,
    status ENUM('active','banned','deleted') DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type ENUM('menu','api') DEFAULT 'api',
    route VARCHAR(200),
    description VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB;

CREATE TABLE role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id),
    FOREIGN KEY (permission_id) REFERENCES permission(id)
) ENGINE=InnoDB;

CREATE TABLE expert_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    license_no VARCHAR(100),
    organization VARCHAR(100),
    department VARCHAR(100),
    specialty_tags VARCHAR(255),
    verified BOOLEAN DEFAULT FALSE,
    badge_visible BOOLEAN DEFAULT TRUE,
    daily_invite_quota INT DEFAULT 10,
    weekly_invite_quota INT DEFAULT 50,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB;

CREATE TABLE privacy_setting (
    user_id BIGINT PRIMARY KEY,
    show_all BOOLEAN DEFAULT TRUE,
    show_age BOOLEAN DEFAULT TRUE,
    show_history BOOLEAN DEFAULT TRUE,
    show_statistics BOOLEAN DEFAULT TRUE,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB;

-- 2. 问答互动
CREATE TABLE question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status ENUM('pending','published','closed','deleted') DEFAULT 'pending',
    is_anonymous BOOLEAN DEFAULT FALSE,
    answer_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    accepted_answer_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB;

CREATE TABLE answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content MEDIUMTEXT NOT NULL,
    status ENUM('pending','published','deleted') DEFAULT 'pending',
    is_authoritative BOOLEAN DEFAULT FALSE,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    citation_count INT DEFAULT 0,
    adopted BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES question(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB;

CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    answer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT,
    content TEXT NOT NULL,
    status ENUM('pending','published','deleted') DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (answer_id) REFERENCES answer(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (parent_id) REFERENCES comment(id)
) ENGINE=InnoDB;

CREATE TABLE topic_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    category ENUM('disease','symptom','drug','behavior','other') DEFAULT 'other',
    parent_id BIGINT,
    UNIQUE KEY uniq_tag (name, category),
    FOREIGN KEY (parent_id) REFERENCES topic_tag(id)
) ENGINE=InnoDB;

CREATE TABLE question_tag (
    question_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (question_id, tag_id),
    FOREIGN KEY (question_id) REFERENCES question(id),
    FOREIGN KEY (tag_id) REFERENCES topic_tag(id)
) ENGINE=InnoDB;

CREATE TABLE answer_citation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    answer_id BIGINT NOT NULL,
    source_title VARCHAR(200) NOT NULL,
    source_type ENUM('guideline','paper','news','other') DEFAULT 'other',
    source_year VARCHAR(10),
    source_link VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (answer_id) REFERENCES answer(id)
) ENGINE=InnoDB;

CREATE TABLE engagement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type ENUM('question','answer','comment') NOT NULL,
    target_id BIGINT NOT NULL,
    action ENUM('like','favorite') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uniq_engagement (user_id, target_type, target_id, action),
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB;

CREATE TABLE report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    target_type ENUM('question','answer','comment') NOT NULL,
    target_id BIGINT NOT NULL,
    category ENUM('risk','ad','spam','privacy','other') NOT NULL,
    evidence TEXT,
    status ENUM('pending','handled','rejected') DEFAULT 'pending',
    handler_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES user(id),
    FOREIGN KEY (handler_id) REFERENCES user(id)
) ENGINE=InnoDB;

CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type ENUM('question','answer','comment','report','knowledge_entry') NOT NULL,
    target_id BIGINT NOT NULL,
    action ENUM('submit','approve','reject','delete','offline') NOT NULL,
    operator_id BIGINT NOT NULL,
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (operator_id) REFERENCES user(id)
) ENGINE=InnoDB;

-- 3. 知识库与来源
CREATE TABLE knowledge_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    category ENUM('disease','symptom','drug','exam','population','other') DEFAULT 'other',
    definition TEXT NOT NULL,
    symptoms TEXT,
    medical_advice TEXT,
    diet_exercise TEXT,
    reference_summary TEXT,
    status ENUM('draft','published','offline') DEFAULT 'draft',
    publisher_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES user(id)
) ENGINE=InnoDB;

CREATE TABLE reference_source (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_id BIGINT NOT NULL,
    source_title VARCHAR(200) NOT NULL,
    source_type ENUM('guideline','paper','book','website','other') DEFAULT 'other',
    source_year VARCHAR(10),
    source_link VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (entry_id) REFERENCES knowledge_entry(id)
) ENGINE=InnoDB;

CREATE TABLE entry_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    change_log VARCHAR(255),
    content_snapshot MEDIUMTEXT NOT NULL,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uniq_version (entry_id, version_no),
    FOREIGN KEY (entry_id) REFERENCES knowledge_entry(id),
    FOREIGN KEY (created_by) REFERENCES user(id)
) ENGINE=InnoDB;

-- 4. 搜索配置与日志
CREATE TABLE synonym_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(100) NOT NULL,
    synonyms VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE stopword_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN DEFAULT TRUE,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE search_weight (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    field_name VARCHAR(100) NOT NULL UNIQUE,
    weight DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    similarity_threshold DECIMAL(5,2) DEFAULT 0.70,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE search_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    keyword VARCHAR(200) NOT NULL,
    filters JSON,
    result_count INT,
    latency_ms INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, created_at),
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB;

-- 5. 推荐与画像
CREATE TABLE user_profile (
    user_id BIGINT PRIMARY KEY,
    age_group ENUM('youth','adult','elderly'),
    chronic_tags VARCHAR(255),
    interest_tags VARCHAR(255),
    risk_level ENUM('low','medium','high') DEFAULT 'low',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB;

CREATE TABLE behavior_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    event_type VARCHAR(50) NOT NULL,
    event_detail JSON,
    occurred_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_event_time (user_id, event_type, occurred_at),
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB;

-- 6. 运营与风控
CREATE TABLE rate_limit_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scope ENUM('account','ip') DEFAULT 'account',
    target VARCHAR(50) NOT NULL,
    action ENUM('question','answer','comment','login') NOT NULL,
    window_seconds INT NOT NULL,
    max_count INT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE sanction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reason VARCHAR(255),
    action ENUM('mute','ban') NOT NULL,
    start_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_at DATETIME,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (created_by) REFERENCES user(id)
) ENGINE=InnoDB;

CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    category ENUM('notice','hotword','topic') DEFAULT 'notice',
    is_active BOOLEAN DEFAULT TRUE,
    start_at DATETIME,
    end_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 7. 消息通知
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM('invite','adopted','comment','system') NOT NULL,
    payload JSON,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB;

CREATE TABLE follow (
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, followee_id),
    FOREIGN KEY (follower_id) REFERENCES user(id),
    FOREIGN KEY (followee_id) REFERENCES user(id)
) ENGINE=InnoDB;

-- 额外索引（可根据业务量追加）
CREATE INDEX idx_question_status ON question(status);
CREATE INDEX idx_answer_status ON answer(status);
CREATE INDEX idx_report_status ON report(status);
CREATE INDEX idx_notification_read ON notification(user_id, is_read);
