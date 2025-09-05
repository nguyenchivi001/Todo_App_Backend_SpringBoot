-- ============================================
-- TASKS TABLE - Core task management
-- ============================================
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGINT NOT NULL,
    category_id BIGINT,
    due_date TIMESTAMP NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'MEDIUM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES task_categories(id) ON DELETE SET NULL,

    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_completed (completed),
    INDEX idx_priority (priority),
    INDEX idx_due_date (due_date),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at),
    INDEX idx_user_completed (user_id, completed),
    INDEX idx_user_priority (user_id, priority),
    INDEX idx_user_due_completed (user_id, due_date, completed),
    INDEX idx_user_category (user_id, category_id),
    INDEX idx_user_created_completed (user_id, created_at, completed),

    FULLTEXT INDEX idx_title_description (title, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TASK CATEGORIES TABLE
-- ============================================
CREATE TABLE task_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    color VARCHAR(7) NOT NULL DEFAULT '#007bff',
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_category_name (user_id, name),

    INDEX idx_user_id (user_id),
    INDEX idx_name (name),
    INDEX idx_color (color),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TASK TAGS TABLE
-- ============================================
CREATE TABLE task_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_tag_name (user_id, name),

    INDEX idx_user_id (user_id),
    INDEX idx_name (name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TASK TAG RELATIONSHIPS (Many-to-Many)
-- ============================================
CREATE TABLE task_tag_relationships (
    task_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (task_id, tag_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES task_tags(id) ON DELETE CASCADE,

    INDEX idx_task_id (task_id),
    INDEX idx_tag_id (tag_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TASK ATTACHMENTS TABLE
-- ============================================
CREATE TABLE task_attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    uploaded_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_task_id (task_id),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_file_name (file_name),
    INDEX idx_content_type (content_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TASK COMMENTS TABLE
-- ============================================
CREATE TABLE task_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_task_id (task_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at),
    INDEX idx_task_created (task_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TASK ACTIVITY LOG TABLE - Track changes
-- ============================================
CREATE TABLE task_activity_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    activity_type ENUM('CREATED', 'UPDATED', 'COMPLETED', 'REOPENED', 'DELETED', 'COMMENTED', 'ATTACHED') NOT NULL,
    old_value TEXT,
    new_value TEXT,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_task_id (task_id),
    INDEX idx_user_id (user_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_created_at (created_at),
    INDEX idx_task_activity (task_id, activity_type),
    INDEX idx_task_created (task_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- INSERT SAMPLE DATA
-- ============================================

-- Insert sample categories
INSERT INTO task_categories (name, description, color, user_id) VALUES
('Development', 'Software development tasks', '#007bff', 1),
('Testing', 'Testing and quality assurance tasks', '#28a745', 1),
('Documentation', 'Documentation and writing tasks', '#ffc107', 1),
('Deployment', 'Deployment and DevOps tasks', '#dc3545', 1),
('Research', 'Research and learning tasks', '#6f42c1', 1),
('Planning', 'Planning and strategy tasks', '#fd7e14', 1);

-- Insert sample tags
INSERT INTO task_tags (name, user_id) VALUES
('urgent', 1),
('backend', 1),
('frontend', 1),
('database', 1),
('security', 1),
('api', 1),
('testing', 1),
('deployment', 1),
('documentation', 1),
('microservice', 1),
('jwt', 1),
('mysql', 1),
('docker', 1),
('spring-boot', 1);

-- Insert sample tasks
INSERT INTO tasks (title, description, completed, user_id, category_id, due_date, priority) VALUES
('Setup Development Environment', 'Install Java, Spring Boot, MySQL and configure the development environment', FALSE, 1, 1, DATE_ADD(NOW(), INTERVAL 3 DAY), 'HIGH'),
('Create User Authentication', 'Implement JWT-based authentication system with refresh tokens', FALSE, 1, 1, DATE_ADD(NOW(), INTERVAL 5 DAY), 'HIGH'),
('Design Database Schema', 'Create tables for users, tasks, and refresh tokens', TRUE, 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'MEDIUM'),
('Implement CRUD Operations', 'Create REST APIs for task management', FALSE, 1, 1, DATE_ADD(NOW(), INTERVAL 7 DAY), 'MEDIUM'),
('Setup API Gateway', 'Configure routing and load balancing for microservices', FALSE, 1, 4, DATE_ADD(NOW(), INTERVAL 4 DAY), 'HIGH'),
('Write Unit Tests', 'Create comprehensive test cases for all services', FALSE, 1, 2, DATE_ADD(NOW(), INTERVAL 10 DAY), 'LOW'),
('Deploy to Production', 'Setup Docker containers and deploy the application', FALSE, 1, 4, DATE_ADD(NOW(), INTERVAL 14 DAY), 'HIGH'),
('Code Review and Optimization', 'Review and optimize the codebase for performance', FALSE, 1, 1, DATE_ADD(NOW(), INTERVAL 8 DAY), 'MEDIUM'),
('Documentation', 'Write API documentation and user guides', FALSE, 1, 3, DATE_ADD(NOW(), INTERVAL 12 DAY), 'LOW'),
('Security Audit', 'Perform security testing and vulnerability assessment', FALSE, 1, 2, DATE_ADD(NOW(), INTERVAL 6 DAY), 'HIGH');

-- ============================================
-- CREATE VIEWS
-- ============================================

-- Active tasks view
CREATE VIEW active_tasks AS
SELECT
    t.id,
    t.title,
    t.description,
    t.completed,
    t.due_date,
    t.priority,
    t.created_at,
    t.updated_at,
    u.username,
    u.email,
    c.name as category_name,
    c.color as category_color,
    CASE
        WHEN t.due_date IS NULL THEN 'No Due Date'
        WHEN t.due_date < NOW() AND t.completed = FALSE THEN 'Overdue'
        WHEN t.due_date <= DATE_ADD(NOW(), INTERVAL 7 DAY) AND t.completed = FALSE THEN 'Due Soon'
        ELSE 'On Track'
    END as status
FROM tasks t
JOIN users u ON t.user_id = u.id
LEFT JOIN task_categories c ON t.category_id = c.id
WHERE u.enabled = TRUE AND u.account_locked = FALSE;

-- Task statistics view
CREATE VIEW task_statistics AS
SELECT
    u.id as user_id,
    u.username,
    u.email,
    COUNT(t.id) as total_tasks,
    SUM(CASE WHEN t.completed = TRUE THEN 1 ELSE 0 END) as completed_tasks,
    SUM(CASE WHEN t.completed = FALSE THEN 1 ELSE 0 END) as pending_tasks,
    SUM(CASE WHEN t.due_date < NOW() AND t.completed = FALSE THEN 1 ELSE 0 END) as overdue_tasks,
    SUM(CASE WHEN t.priority = 'HIGH' THEN 1 ELSE 0 END) as high_priority_tasks,
    SUM(CASE WHEN t.priority = 'MEDIUM' THEN 1 ELSE 0 END) as medium_priority_tasks,
    SUM(CASE WHEN t.priority = 'LOW' THEN 1 ELSE 0 END) as low_priority_tasks,
    ROUND(AVG(CASE WHEN t.completed = TRUE THEN 1 ELSE 0 END) * 100, 2) as completion_rate,
    MAX(t.created_at) as last_task_created,
    MAX(t.updated_at) as last_task_updated
FROM users u
LEFT JOIN tasks t ON u.id = t.user_id
WHERE u.enabled = TRUE AND u.account_locked = FALSE
GROUP BY u.id, u.username, u.email;

-- Overdue tasks view
CREATE VIEW overdue_tasks AS
SELECT
    t.id,
    t.title,
    t.description,
    t.due_date,
    t.priority,
    t.created_at,
    u.username,
    u.email,
    c.name as category_name,
    DATEDIFF(NOW(), t.due_date) as days_overdue
FROM tasks t
JOIN users u ON t.user_id = u.id
LEFT JOIN task_categories c ON t.category_id = c.id
WHERE t.due_date < NOW()
  AND t.completed = FALSE
  AND u.enabled = TRUE
  AND u.account_locked = FALSE
ORDER BY t.due_date ASC;

-- ============================================
-- CREATE TRIGGERS
-- ============================================

DELIMITER //

CREATE TRIGGER update_tasks_timestamp
    BEFORE UPDATE ON tasks
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER update_task_categories_timestamp
    BEFORE UPDATE ON task_categories
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER update_task_comments_timestamp
    BEFORE UPDATE ON task_comments
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER log_task_creation
    AFTER INSERT ON tasks
    FOR EACH ROW
BEGIN
    INSERT INTO task_activity_log (task_id, user_id, activity_type, new_value, description)
    VALUES (NEW.id, NEW.user_id, 'CREATED', NEW.title, CONCAT('Task "', NEW.title, '" was created'));
END//

CREATE TRIGGER log_task_completion
    AFTER UPDATE ON tasks
    FOR EACH ROW
BEGIN
    IF OLD.completed = FALSE AND NEW.completed = TRUE THEN
        INSERT INTO task_activity_log (task_id, user_id, activity_type, old_value, new_value, description)
        VALUES (NEW.id, NEW.user_id, 'COMPLETED', 'FALSE', 'TRUE', CONCAT('Task "', NEW.title, '" was completed'));
    ELSEIF OLD.completed = TRUE AND NEW.completed = FALSE THEN
        INSERT INTO task_activity_log (task_id, user_id, activity_type, old_value, new_value, description)
        VALUES (NEW.id, NEW.user_id, 'REOPENED', 'TRUE', 'FALSE', CONCAT('Task "', NEW.title, '" was reopened'));
    ELSEIF OLD.title != NEW.title OR OLD.description != NEW.description OR OLD.priority != NEW.priority OR OLD.due_date != NEW.due_date THEN
        INSERT INTO task_activity_log (task_id, user_id, activity_type, description)
        VALUES (NEW.id, NEW.user_id, 'UPDATED', CONCAT('Task "', NEW.title, '" was updated'));
    END IF;
END//

CREATE TRIGGER log_task_comment
    AFTER INSERT ON task_comments
    FOR EACH ROW
BEGIN
    INSERT INTO task_activity_log (task_id, user_id, activity_type, new_value, description)
    VALUES (NEW.task_id, NEW.user_id, 'COMMENTED', LEFT(NEW.comment, 100),
           CONCAT('Comment added to task by user ID ', NEW.user_id));
END//

CREATE TRIGGER log_task_attachment
    AFTER INSERT ON task_attachments
    FOR EACH ROW
BEGIN
    INSERT INTO task_activity_log (task_id, user_id, activity_type, new_value, description)
    VALUES (NEW.task_id, NEW.uploaded_by, 'ATTACHED', NEW.file_name,
           CONCAT('File "', NEW.file_name, '" was attached to task'));
END//

DELIMITER ;

-- ============================================
-- STORED PROCEDURES
-- ============================================

DELIMITER //

CREATE PROCEDURE sp_get_user_task_summary(IN p_user_id BIGINT)
BEGIN
    SELECT
        'Task Summary' as report_type,
        COUNT(*) as total_tasks,
        SUM(CASE WHEN completed = TRUE THEN 1 ELSE 0 END) as completed,
        SUM(CASE WHEN completed = FALSE THEN 1 ELSE 0 END) as pending,
        SUM(CASE WHEN due_date < NOW() AND completed = FALSE THEN 1 ELSE 0 END) as overdue,
        SUM(CASE WHEN due_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY) AND completed = FALSE THEN 1 ELSE 0 END) as due_this_week,
        SUM(CASE WHEN priority = 'HIGH' THEN 1 ELSE 0 END) as high_priority,
        SUM(CASE WHEN priority = 'MEDIUM' THEN 1 ELSE 0 END) as medium_priority,
        SUM(CASE WHEN priority = 'LOW' THEN 1 ELSE 0 END) as low_priority,
        ROUND(AVG(CASE WHEN completed = TRUE THEN 1 ELSE 0 END) * 100, 2) as completion_rate
    FROM tasks
    WHERE user_id = p_user_id;
END//

CREATE PROCEDURE sp_get_task_activity_log(IN p_task_id BIGINT)
BEGIN
    SELECT
        al.id,
        al.activity_type,
        al.old_value,
        al.new_value,
        al.description,
        al.created_at,
        u.username,
        u.email
    FROM task_activity_log al
    JOIN users u ON al.user_id = u.id
    WHERE al.task_id = p_task_id
    ORDER BY al.created_at DESC;
END//

CREATE PROCEDURE sp_cleanup_old_activity_logs(IN p_days_old INT)
BEGIN
    DELETE FROM task_activity_log
    WHERE created_at < DATE_SUB(NOW(), INTERVAL p_days_old DAY);

    SELECT ROW_COUNT() as deleted_records;
END//

DELIMITER ;

-- ============================================
-- CREATE FUNCTIONS
-- ============================================

DELIMITER //

CREATE FUNCTION fn_user_completion_rate(p_user_id BIGINT)
RETURNS DECIMAL(5,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE total_tasks INT DEFAULT 0;
    DECLARE completed_tasks INT DEFAULT 0;
    DECLARE completion_rate DECIMAL(5,2) DEFAULT 0.00;

    SELECT COUNT(*) INTO total_tasks
    FROM tasks
    WHERE user_id = p_user_id;

    IF total_tasks > 0 THEN
        SELECT COUNT(*) INTO completed_tasks
        FROM tasks
        WHERE user_id = p_user_id AND completed = TRUE;

        SET completion_rate = (completed_tasks / total_tasks) * 100;
    END IF;

    RETURN completion_rate;
END//

CREATE FUNCTION fn_get_task_priority_score(p_priority VARCHAR(10))
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    CASE p_priority
        WHEN 'HIGH' THEN RETURN 3;
        WHEN 'MEDIUM' THEN RETURN 2;
        WHEN 'LOW' THEN RETURN 1;
        ELSE RETURN 0;
    END CASE;
END//

DELIMITER ;

-- ============================================
-- ADD COMMENTS TO TABLES
-- ============================================

ALTER TABLE tasks COMMENT = 'Core tasks with user assignments, categories, and tracking';
ALTER TABLE task_categories COMMENT = 'User-defined categories for organizing tasks';
ALTER TABLE task_tags COMMENT = 'Flexible tagging system for tasks';
ALTER TABLE task_tag_relationships COMMENT = 'Many-to-many relationship between tasks and tags';
ALTER TABLE task_attachments COMMENT = 'File attachments associated with tasks';
ALTER TABLE task_comments COMMENT = 'Comments and discussions on tasks';
ALTER TABLE task_activity_log COMMENT = 'Audit trail for task changes and activities';

-- ============================================
-- PERFORMANCE OPTIMIZATION INDEXES
-- ============================================

-- Additional performance indexes
CREATE INDEX idx_tasks_user_status_priority ON tasks(user_id, completed, priority);
CREATE INDEX idx_tasks_due_status ON tasks(due_date, completed);
CREATE INDEX idx_activity_log_task_type_date ON task_activity_log(task_id, activity_type, created_at);
CREATE INDEX idx_comments_task_user_date ON task_comments(task_id, user_id, created_at);

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Verify table creation and sample data
SELECT 'Tasks' as table_name, COUNT(*) as record_count FROM tasks
UNION ALL
SELECT 'Categories', COUNT(*) FROM task_categories
UNION ALL
SELECT 'Tags', COUNT(*) FROM task_tags
UNION ALL
SELECT 'Comments', COUNT(*) FROM task_comments
UNION ALL
SELECT 'Attachments', COUNT(*) FROM task_attachments
UNION ALL
SELECT 'Activity Logs', COUNT(*) FROM task_activity_log;

-- Test completion rate function
SELECT
    username,
    fn_user_completion_rate(id) as completion_rate_percent
FROM users
WHERE id = 1;