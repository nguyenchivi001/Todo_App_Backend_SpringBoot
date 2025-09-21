-- Drop existing databases if they exist (optional, for clean setup)
DROP DATABASE IF EXISTS todo_auth;
DROP DATABASE IF EXISTS todo_task;

-- Create databases with proper character set and collation
CREATE DATABASE todo_auth
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE todo_task
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;