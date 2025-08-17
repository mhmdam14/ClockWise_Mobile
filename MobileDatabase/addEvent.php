<?php

include_once("connection.php");

if (isset($_GET['eventName']) && isset($_GET['eventDescription']) && isset($_GET['eventDate']) &&
    isset($_GET['eventStartTime']) && isset($_GET['eventFinishTime']) && isset($_GET['syncStatus'])) {

    $userEmail = $_GET['userEmail'];
    $eventName = mysqli_real_escape_string($conn, $_GET['eventName']);
    $eventDescription = mysqli_real_escape_string($conn, $_GET['eventDescription']);
    $eventDate = $_GET['eventDate'];
    $eventStartTime = $_GET['eventStartTime'];
    $eventFinishTime = $_GET['eventFinishTime'];
    $syncStatus = $_GET['syncStatus'];

    $query = "INSERT INTO events (user_email, event_name, event_description, event_date, event_start_time, event_finish_time, sync_status) 
              VALUES ('$userEmail', '$eventName', '$eventDescription', '$eventDate', '$eventStartTime', '$eventFinishTime', '$syncStatus')";

    if ($conn->query($query)) {
        echo json_encode([
            'status' => 'success',
            'message' => 'Event "' . $eventName . '" added successfully.'
        ]);
    } else {
        echo json_encode([
            'status' => 'error',
            'message' => 'Error: ' . $conn->error
        ]);
    }

} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'Missing parameters.'
    ]);
}
?>

