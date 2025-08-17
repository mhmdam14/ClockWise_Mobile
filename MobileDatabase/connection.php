<?php
$server_name = "localhost";  // Change if using remote database
$username = "root"; 
$password = ""; 
$db_name = "mydb";

// Create connection
$conn = new mysqli($server_name, $username, $password, $db_name);

// Check connection
if ($conn->connect_error) {
    die(json_encode(["error" => true, "message" => "Database Connection Failed: " . $conn->connect_error]));
}

// Create `admins` table if not exists
$table_creation_query = "CREATE TABLE IF NOT EXISTS admins (
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    profile_pic LONGBLOB NULL
)";
$conn->query($table_creation_query);

// Create `events` table if not exists
$create_events_table = "CREATE TABLE IF NOT EXISTS events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(100) NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    event_description TEXT NOT NULL,
    event_date DATE NOT NULL,
    event_start_time TIME NOT NULL,
    event_finish_time TIME NOT NULL,
    sync_status VARCHAR(200) NOT NULL,
    FOREIGN KEY (user_email) REFERENCES admins(email) ON DELETE CASCADE
)";
$conn->query($create_events_table);
?>
