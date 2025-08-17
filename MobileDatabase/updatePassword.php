<?php
include_once("connection.php");

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $email = $_POST['email'];
    $old_password = $_POST['old_password'];
    $new_password = $_POST['new_password'];

    // Check if old password is correct
    $query = "SELECT password FROM admins WHERE email = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();
    $stmt->bind_result($stored_password);
    
    if ($stmt->fetch()) {
        // Verify the old password with the hashed password in the database
        if (password_verify($old_password, $stored_password)) {
            // Hash the new password before updating
            $hashed_new_password = password_hash($new_password, PASSWORD_DEFAULT);

            // Update the password in the database
            $updateQuery = "UPDATE admins SET password = ? WHERE email = ?";
            $updateStmt = $conn->prepare($updateQuery);
            $updateStmt->bind_param("ss", $hashed_new_password, $email);
            
            if ($updateStmt->execute()) {
                echo json_encode(["success" => true, "message" => "Password updated successfully"]);
            } else {
                echo json_encode(["success" => false, "message" => "Password update failed"]);
            }
        } else {
            echo json_encode(["success" => false, "message" => "Incorrect old password"]);
        }
    } else {
        echo json_encode(["success" => false, "message" => "User not found"]);
    }

    $stmt->close();
    $conn->close();
}
?>