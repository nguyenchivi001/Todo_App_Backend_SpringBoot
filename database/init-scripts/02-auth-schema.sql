CREATE TABLE todo_auth.users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    login_attempts INT NOT NULL DEFAULT 0,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_enabled (enabled),
    INDEX idx_account_locked (account_locked),
    INDEX idx_created_at (created_at)
);

-- Refresh tokens table
CREATE TABLE todo_auth.refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP NULL,

    FOREIGN KEY (user_id) REFERENCES todo_auth.users(id) ON DELETE CASCADE,

    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_revoked (revoked),
    INDEX idx_ip_address (ip_address),
    INDEX idx_created_at (created_at),
    INDEX idx_last_used (last_used),
    INDEX idx_user_revoked (user_id, revoked),
    INDEX idx_user_expires (user_id, expires_at)
);

-- User roles table
CREATE TABLE todo_auth.user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_name VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES todo_auth.users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_role (user_id, role_name),

    INDEX idx_user_id (user_id),
    INDEX idx_role_name (role_name)
);

-- Login history table
CREATE TABLE todo_auth.login_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    login_status ENUM('SUCCESS', 'FAILED', 'BLOCKED') NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES todo_auth.users(id) ON DELETE CASCADE,

    INDEX idx_user_id (user_id),
    INDEX idx_ip_address (ip_address),
    INDEX idx_login_status (login_status),
    INDEX idx_created_at (created_at),
    INDEX idx_user_status (user_id, login_status),
    INDEX idx_user_date (user_id, created_at)
);

-- Password reset tokens table
CREATE TABLE todo_auth.password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP NULL,

    FOREIGN KEY (user_id) REFERENCES todo_auth.users(id) ON DELETE CASCADE,

    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_used (used)
);

-- Insert default admin user (password: admin123456)
INSERT INTO todo_auth.users (username, email, password, first_name, last_name, enabled) VALUES
('admin', 'admin@todo.com', '$2a$12$A.qVSsI9qED0UZ2t2TNZv.fExC354uctVpLoj1qz2jJ/VO6q3nG6S', 'System', 'Administrator', TRUE);

-- Insert default user role for admin
INSERT INTO todo_auth.user_roles (user_id, role_name)
SELECT id, 'ADMIN' FROM todo_auth.users WHERE username = 'admin';

INSERT INTO todo_auth.user_roles (user_id, role_name)
SELECT id, 'USER' FROM todo_auth.users WHERE username = 'admin';

-- Create indexes for performance optimization
CREATE INDEX idx_users_enabled_locked ON todo_auth.users(enabled, account_locked);
CREATE INDEX idx_refresh_tokens_valid ON todo_auth.refresh_tokens(revoked, expires_at);
CREATE INDEX idx_login_history_recent ON todo_auth.login_history(created_at DESC, user_id);

-- Create views
CREATE VIEW todo_auth.active_users AS
SELECT id, username, email, first_name, last_name, last_login, created_at
FROM todo_auth.users
WHERE enabled = TRUE AND account_locked = FALSE;

CREATE VIEW todo_auth.user_statistics AS
SELECT DATE(created_at) as registration_date,
       COUNT(*) as registrations_count
FROM todo_auth.users
GROUP BY DATE(created_at)
ORDER BY registration_date DESC;

-- Create trigger
DELIMITER //

CREATE TRIGGER todo_auth.update_users_timestamp
    BEFORE UPDATE ON todo_auth.users
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

DELIMITER ;

-- Add comments to tables
ALTER TABLE todo_auth.users COMMENT = 'User accounts and authentication information';
ALTER TABLE todo_auth.refresh_tokens COMMENT = 'JWT refresh tokens for maintaining user sessions';
ALTER TABLE todo_auth.user_roles COMMENT = 'User role assignments for authorization';
ALTER TABLE todo_auth.login_history COMMENT = 'Login attempt history for security auditing';
ALTER TABLE todo_auth.password_reset_tokens COMMENT = 'Password reset tokens for account recovery';