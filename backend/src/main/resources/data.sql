-- admin account
-- password (plaintext): Admin123!
INSERT INTO `user` (`id`, `username`, `phone`, `password`, `status`, `created_at`, `updated_at`, `nickname`)
VALUES (1, 'admin', '13800000000', '$2a$10$cltVtPrhH5QRqkoSKfWwgenq.hR8kfCqJAC9.C2tflzU0.FUlnArO', 1, NOW(), NOW(), 'Administrator')
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `updated_at` = NOW();
