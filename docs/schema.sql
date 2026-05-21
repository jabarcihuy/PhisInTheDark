CREATE DATABASE IF NOT EXISTS phis_in_the_dark;
USE phis_in_the_dark;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS user_settings (
    user_id INT PRIMARY KEY,
    audio_muted BOOLEAN DEFAULT FALSE,
    ambience_muted BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS save_slots (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    slot_name VARCHAR(100) NOT NULL,
    mode VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    paranoia INT DEFAULT 0,
    ending_reached BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_played_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS save_inventory_items (
    save_slot_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    obtained_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (save_slot_id, item_name),
    FOREIGN KEY (save_slot_id) REFERENCES save_slots(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS save_solved_puzzles (
    save_slot_id INT NOT NULL,
    puzzle_id VARCHAR(100) NOT NULL,
    solved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attempt_count INT DEFAULT 1,
    last_answer VARCHAR(255),
    PRIMARY KEY (save_slot_id, puzzle_id),
    FOREIGN KEY (save_slot_id) REFERENCES save_slots(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS websites (
    id VARCHAR(100) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    url VARCHAR(255) NOT NULL,
    visual_style VARCHAR(50) NOT NULL,
    subtitle VARCHAR(255),
    screenshot_asset VARCHAR(100),
    unlocked_file_title VARCHAR(100),
    unlocked_file_content TEXT,
    sort_order INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS website_body_lines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    website_id VARCHAR(100) NOT NULL,
    line_order INT NOT NULL,
    content TEXT NOT NULL,
    FOREIGN KEY (website_id) REFERENCES websites(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS puzzles (
    id VARCHAR(100) PRIMARY KEY,
    website_id VARCHAR(100),
    title VARCHAR(100) NOT NULL,
    concept VARCHAR(100),
    code_block TEXT,
    instruction TEXT,
    answer VARCHAR(255),
    tutorial_answer VARCHAR(255),
    reward_item VARCHAR(100),
    solved_message TEXT,
    FOREIGN KEY (website_id) REFERENCES websites(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS puzzle_hints (
    id INT AUTO_INCREMENT PRIMARY KEY,
    puzzle_id VARCHAR(100) NOT NULL,
    hint_order INT NOT NULL,
    content TEXT NOT NULL,
    FOREIGN KEY (puzzle_id) REFERENCES puzzles(id) ON DELETE CASCADE
);
