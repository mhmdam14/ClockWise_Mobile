<?php
include_once("connection.php");

if(isset($_GET['userEmail'])){
    $email = $_GET['userEmail'];
    $query = "DELETE FROM events WHERE sync_status='PENDING_DELETE' AND user_email=?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("s", $email);
    $stmt->execute();
}
?>