CREATE DATABASE IF NOT EXISTS reviv_db;
USE reviv_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    failed_attempts INT DEFAULT 0,
    locked BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS sos_requests (
    sos_id VARCHAR(50) NOT NULL PRIMARY KEY,
    user_id INT,
    description VARCHAR(255),
    zone VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    team_id INT DEFAULT 0,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rescue_teams (
    team_id INT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL,
    captain VARCHAR(50),
    available BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS resources (
    resource_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_name VARCHAR(100) NOT NULL UNIQUE,
    quantity INT NOT NULL,
    used_quantity INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS shelters (
    shelter_id INT AUTO_INCREMENT PRIMARY KEY,
    shelter_name VARCHAR(100) NOT NULL,
    zone VARCHAR(50),
    capacity INT NOT NULL,
    free_capacity INT NOT NULL
);

CREATE TABLE IF NOT EXISTS hospitals (
    hospital_id INT AUTO_INCREMENT PRIMARY KEY,
    hospital_name VARCHAR(100) NOT NULL,
    zone VARCHAR(50),
    free_beds INT NOT NULL
);

CREATE TABLE IF NOT EXISTS volunteers (
    volunteer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    skill VARCHAR(100),
    user_id INT DEFAULT 0,
    available BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    sos_id VARCHAR(50),
    zone VARCHAR(50),
    message VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS donations (
    donation_id INT AUTO_INCREMENT PRIMARY KEY,
    donor VARCHAR(100),
    amount DOUBLE
);

INSERT INTO users (username, password, role) VALUES
    ('admin', 'admin123', 'ADMIN'),
    ('admin2', 'admin123', 'ADMIN'),
    ('admin3', 'admin123', 'ADMIN'),
    ('dispatcher', 'dispatcher123', 'DISPATCHER'),
    ('dispatcher2', 'dispatcher123', 'DISPATCHER'),
    ('dispatcher3', 'dispatcher123', 'DISPATCHER'),
    ('citizen', 'citizen123', 'CITIZEN'),
    ('citizen2', 'citizen123', 'CITIZEN'),
    ('citizen3', 'citizen123', 'CITIZEN'),
    ('volunteer', 'volunteer123', 'VOLUNTEER'),
    ('volunteer2', 'volunteer123', 'VOLUNTEER'),
    ('volunteer3', 'volunteer123', 'VOLUNTEER');

INSERT INTO rescue_teams (team_name, captain, available) VALUES
    ('Team Alpha', 'Rahul', TRUE),
    ('Team Beta', 'Anita', TRUE),
    ('Team Gamma', 'Vikram', FALSE),
    ('Team Delta', 'Meena', TRUE),
    ('Team Echo', 'Suresh', TRUE);

INSERT INTO hospitals (hospital_name, zone, free_beds) VALUES
    ('Hospital City', 'Zone A', 20),
    ('Hospital Civil', 'Zone B', 15),
    ('Hospital Gov', 'Zone A', 30),
    ('Hospital St Mary', 'Zone C', 10);

INSERT INTO shelters (shelter_name, zone, capacity, free_capacity) VALUES
    ('Shelter Relief', 'Zone A', 50, 50),
    ('Shelter Safe Haven', 'Zone B', 40, 40),
    ('Shelter Hope Center', 'Zone C', 25, 25);

INSERT INTO resources (resource_name, quantity) VALUES
    ('Food Packet', 100),
    ('Water Bottle', 200),
    ('Blanket', 80),
    ('Medical Kit', 30),
    ('Tent', 12);

INSERT INTO volunteers (name, skill, user_id, available) VALUES
    ('John', 'Medical', (SELECT id FROM users WHERE username = 'volunteer'), TRUE),
    ('Mary', 'Medical', (SELECT id FROM users WHERE username = 'volunteer2'), TRUE),
    ('Ravi', 'Rescue', (SELECT id FROM users WHERE username = 'volunteer3'), TRUE),
    ('Kavya', 'Rescue', 0, TRUE),
    ('Arjun', 'Cooking', 0, TRUE);
