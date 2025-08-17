<?php
include_once("connection.php");

if (isset($_GET['email'])) {
    $email = $_GET['email'];
    
    $query = "SELECT first_name, last_name FROM admins WHERE email = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();
    $stmt->bind_result($first_name, $last_name);
    
    if ($stmt->fetch()) {
        echo json_encode(["success" => true, "first_name" => $first_name, "last_name" => $last_name]);
    } else {
        echo json_encode(["success" => false, "message" => "User not found"]);
    }
}
?>