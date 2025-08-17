<?php
include_once 'connection.php';

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json; charset=UTF-8");

$response = [];


if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = trim($_POST['email']);
    $password = trim($_POST['password']);
    
  
    $query = "SELECT email, password FROM admins WHERE email = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $hashed_password = $row['password'];

        if (password_verify($password, $hashed_password)) {
            $response['success'] = true;
            $response['message'] = 'Logged in successfully!';
        } else {
            $response['success'] = false;
            $response['message'] = 'Password incorrect!';
        }
    } else {
        $response['success'] = false;
        $response['message'] = "email address doesn't exist";
    }

    echo json_encode($response);
}
?>
