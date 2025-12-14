-- RBAC minimal schema for Spring Boot + Spring Security + MySQL 8

CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(64) NOT NULL,
    `phone` VARCHAR(20),
    `password` VARCHAR(100) NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=enabled,0=disabled',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_phone` (`phone`),
    INDEX `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    `description` VARCHAR(255),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_role_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `code` VARCHAR(100) NOT NULL,
    `name` VARCHAR(128) NOT NULL,
    `type` VARCHAR(16) NOT NULL COMMENT 'MENU/API/BUTTON',
    `path_or_api` VARCHAR(255),
    `method` VARCHAR(16),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_permission_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_role` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`, `role_id`)
    -- CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    -- CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `role_permission` (
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`role_id`, `permission_id`)
    -- CONSTRAINT `fk_role_perm_role` FOREIGN KEY (`role_id`) REFERENCES `role`(`id`) ON DELETE CASCADE,
    -- CONSTRAINT `fk_role_perm_perm` FOREIGN KEY (`permission_id`) REFERENCES `permission`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- seed roles
INSERT INTO `role` (`code`, `name`, `description`)
VALUES
    ('admin', 'Administrator', '系统管理员'),
    ('staff', 'Staff', '运营人员'),
    ('customer', 'Customer', '普通用户'),
    ('expert', 'Expert', '专家用户');

-- seed permissions (示例)
INSERT INTO `permission` (`code`, `name`, `type`, `path_or_api`, `method`)
VALUES
    ('user:read', '查看用户', 'API', '/api/users', 'GET'),
    ('user:write', '管理用户', 'API', '/api/users', 'POST'),
    ('role:read', '查看角色', 'API', '/api/roles', 'GET'),
    ('role:write', '管理角色', 'API', '/api/roles', 'POST'),
    ('perm:read', '查看权限', 'API', '/api/permissions', 'GET');

-- bind admin role to all permissions
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `role` r CROSS JOIN `permission` p WHERE r.code = 'admin';

-- seed an admin user (password hash = bcrypt("password"))
INSERT INTO `user` (`username`, `phone`, `password`, `status`)
VALUES ('admin', '18800000000', '$2a$10$CwTycUXWue0Thq9StjUM0uJ8TZY.q6r0pe1.GhXCTITVEWilR/Gui', 1);

-- bind admin user to admin role
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `user` u CROSS JOIN `role` r WHERE u.username = 'admin' AND r.code = 'admin';
