<?php
include_once("connection.php");

if (isset($_GET['eventName']) && isset($_GET['userEmail'])) {

    // Get event data from URL parameters
    $userEmail = $_GET['userEmail'];
    $eventName = $_GET['eventName'];
    $eventDescription = $_GET['eventDescription'];
    $eventDate = $_GET['eventDate'];
    $eventStartTime = $_GET['eventStartTime'];
    $eventFinishTime = $_GET['eventFinishTime'];
    

    $query = "DELETE FROM events WHERE user_email=? AND event_name=? AND event_description=? AND event_date=? AND event_start_time=? AND event_finish_time=?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("ssssss", $userEmail, $eventName, $eventDescription, $eventDate, $eventStartTime, $eventFinishTime);
    $stmt->execute();

}

?>