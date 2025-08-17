<?php
include_once 'connection.php';

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json; charset=UTF-8");

$response = [];

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $first_name = trim($_POST['first_name']);
    $last_name = trim($_POST['last_name']);
    $email = filter_var(trim($_POST['email']), FILTER_SANITIZE_EMAIL);
    $password = trim($_POST['password']);

    if (empty($first_name) || empty($last_name) || empty($email) || empty($password)) {
        $response["error"] = true;
        $response["message"] = "All fields are required.";
    } elseif (strlen($password) < 8) {
        $response["error"] = true;
        $response["message"] = "Password must be at least 8 characters.";
    } else {
        $query = "SELECT email FROM admins WHERE email = ?";
        $stmt = $conn->prepare($query);
        $stmt->bind_param('s', $email);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result->num_rows > 0) {
            $response["error"] = true;
            $response["message"] = "Email already exists.";
        } else {
            $hashed_password = password_hash($password, PASSWORD_DEFAULT);
            $query = "INSERT INTO admins (first_name, last_name, email, password) VALUES (?, ?, ?, ?)";
            $stmt = $conn->prepare($query);
            $stmt->bind_param("ssss", $first_name, $last_name, $email, $hashed_password);

            if ($stmt->execute()) {
                $response["error"] = false;
                $response["message"] = "Registration successful!";
            } else {
                $response["error"] = true;
                $response["message"] = "Error during registration.";
            }
        }
    }
} else {
    $response["error"] = true;
    $response["message"] = "Invalid request.";
}

echo json_encode($response);
?>
