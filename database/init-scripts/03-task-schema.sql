-- Task categories table
CREATE TABLE todo_task.task_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    color VARCHAR(7) NOT NULL DEFAULT '#007bff',
    user_id BIGINT NOT NULL, -- reference to todo_auth.users.id (no FK)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY unique_user_category_name (user_id, name),
    INDEX idx_user_id (user_id),
    INDEX idx_name (name),
    INDEX idx_color (color),
    INDEX idx_created_at (created_at)
);

-- Tasks table
CREATE TABLE todo_task.tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGINT NOT NULL, -- reference to todo_auth.users.id (no FK)
    category_id BIGINT,
    due_date TIMESTAMP NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'MEDIUM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (category_id) REFERENCES todo_task.task_categories(id) ON DELETE SET NULL,

    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_completed (completed),
    INDEX idx_priority (priority),
    INDEX idx_due_date (due_date),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at)
);

-- Task tags
CREATE TABLE todo_task.task_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL, -- reference to todo_auth.users.id (no FK)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY unique_user_tag_name (user_id, name),
    INDEX idx_user_id (user_id),
    INDEX idx_name (name),
    INDEX idx_created_at (created_at)
);

-- Task tag relationships
CREATE TABLE todo_task.task_tag_relationships (
    task_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (task_id, tag_id),
    FOREIGN KEY (task_id) REFERENCES todo_task.tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES todo_task.task_tags(id) ON DELETE CASCADE
);

-- Task comments
CREATE TABLE todo_task.task_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL, -- reference to todo_auth.users.id (no FK)
    comment TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (task_id) REFERENCES todo_task.tasks(id) ON DELETE CASCADE
);

-- Task activity log
CREATE TABLE todo_task.task_activity_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL, -- reference to todo_auth.users.id (no FK)
    activity_type ENUM('CREATED', 'UPDATED', 'COMPLETED', 'REOPENED', 'DELETED', 'COMMENTED', 'PRIORITY_CHANGED', 'DUE_DATE_CHANGED', 'CATEGORY_CHANGED', 'TAG_ADDED', 'TAG_REMOVED') NOT NULL,
    old_value TEXT,
    new_value TEXT,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (task_id) REFERENCES todo_task.tasks(id) ON DELETE CASCADE
);
-- ============================================
-- INSERT SAMPLE DATA
-- ============================================


-- Insert sample categories
INSERT INTO todo_task.task_categories (name, description, color, user_id) VALUES
('Development', 'Software development tasks', '#007bff', 1),
('Testing', 'Testing and quality assurance tasks', '#28a745', 1),
('Documentation', 'Documentation and writing tasks', '#ffc107', 1),
('Deployment', 'Deployment and DevOps tasks', '#dc3545', 1),
('Research', 'Research and learning tasks', '#6f42c1', 1),
('Planning', 'Planning and strategy tasks', '#fd7e14', 1);

-- Insert sample tags
INSERT INTO todo_task.task_tags (name, user_id) VALUES
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
INSERT INTO todo_task.tasks (title, description, completed, user_id, category_id, due_date, priority) VALUES
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
CREATE VIEW todo_task.active_tasks AS
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
FROM todo_task.tasks t
JOIN todo_auth.users u ON t.user_id = u.id
LEFT JOIN todo_task.task_categories c ON t.category_id = c.id
WHERE u.enabled = TRUE AND u.account_locked = FALSE;

-- Task statistics view
CREATE VIEW todo_task.task_statistics AS
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
FROM todo_auth.users u
LEFT JOIN todo_task.tasks t ON u.id = t.user_id
WHERE u.enabled = TRUE AND u.account_locked = FALSE
GROUP BY u.id, u.username, u.email;

-- ============================================
-- CREATE TRIGGERS
-- ============================================

DELIMITER //

CREATE TRIGGER todo_task.update_tasks_timestamp
    BEFORE UPDATE ON todo_task.tasks
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER todo_task.update_task_categories_timestamp
    BEFORE UPDATE ON todo_task.task_categories
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER todo_task.update_task_comments_timestamp
    BEFORE UPDATE ON todo_task.task_comments
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER todo_task.log_task_creation
    AFTER INSERT ON todo_task.tasks
    FOR EACH ROW
BEGIN
    INSERT INTO todo_task.task_activity_log (task_id, user_id, activity_type, new_value, description)
    VALUES (NEW.id, NEW.user_id, 'CREATED', NEW.title, CONCAT('Task "', NEW.title, '" was created'));
END//

CREATE TRIGGER todo_task.log_task_completion
    AFTER UPDATE ON todo_task.tasks
    FOR EACH ROW
BEGIN
    IF OLD.completed = FALSE AND NEW.completed = TRUE THEN
        INSERT INTO todo_task.task_activity_log (task_id, user_id, activity_type, old_value, new_value, description)
        VALUES (NEW.id, NEW.user_id, 'COMPLETED', 'FALSE', 'TRUE', CONCAT('Task "', NEW.title, '" was completed'));
    ELSEIF OLD.completed = TRUE AND NEW.completed = FALSE THEN
        INSERT INTO todo_task.task_activity_log (task_id, user_id, activity_type, old_value, new_value, description)
        VALUES (NEW.id, NEW.user_id, 'REOPENED', 'TRUE', 'FALSE', CONCAT('Task "', NEW.title, '" was reopened'));
    END IF;

    IF OLD.title != NEW.title OR OLD.description != NEW.description OR OLD.priority != NEW.priority OR OLD.due_date != NEW.due_date THEN
        INSERT INTO todo_task.task_activity_log (task_id, user_id, activity_type, description)
        VALUES (NEW.id, NEW.user_id, 'UPDATED', CONCAT('Task "', NEW.title, '" was updated'));
    END IF;
END//

CREATE TRIGGER todo_task.log_task_comment
    AFTER INSERT ON todo_task.task_comments
    FOR EACH ROW
BEGIN
    INSERT INTO todo_task.task_activity_log (task_id, user_id, activity_type, new_value, description)
    VALUES (NEW.task_id, NEW.user_id, 'COMMENTED', LEFT(NEW.comment, 100),
           CONCAT('Comment added to task by user ID ', NEW.user_id));
END//

DELIMITER ;

-- ============================================
-- ADD COMMENTS TO TABLES
-- ============================================

ALTER TABLE todo_task.tasks COMMENT = 'Core tasks with user assignments, categories, and tracking';
ALTER TABLE todo_task.task_categories COMMENT = 'User-defined categories for organizing tasks';
ALTER TABLE todo_task.task_tags COMMENT = 'Flexible tagging system for tasks';
ALTER TABLE todo_task.task_tag_relationships COMMENT = 'Many-to-many relationship between tasks and tags';
ALTER TABLE todo_task.task_comments COMMENT = 'Comments and discussions on tasks';
ALTER TABLE todo_task.task_activity_log COMMENT = 'Audit trail for task changes and activities';

-- ============================================
-- PERFORMANCE OPTIMIZATION INDEXES
-- ============================================

CREATE INDEX idx_tasks_user_status_priority ON todo_task.tasks(user_id, completed, priority);
CREATE INDEX idx_tasks_due_status ON todo_task.tasks(due_date, completed);
CREATE INDEX idx_activity_log_task_type_date ON todo_task.task_activity_log(task_id, activity_type, created_at);
CREATE INDEX idx_comments_task_user_date ON todo_task.task_comments(task_id, user_id, created_at);

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

SELECT 'Tasks' as table_name, COUNT(*) as record_count FROM todo_task.tasks
UNION ALL
SELECT 'Categories', COUNT(*) FROM todo_task.task_categories
UNION ALL
SELECT 'Tags', COUNT(*) FROM todo_task.task_tags
UNION ALL
SELECT 'Comments', COUNT(*) FROM todo_task.task_comments
UNION ALL
SELECT 'Activity Logs', COUNT(*) FROM todo_task.task_activity_log;