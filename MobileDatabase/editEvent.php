<?php
include_once("connection.php");

if (isset($_GET['userEmail']) && isset($_GET['syncStatus'])) {
    $userEmail = $_GET['userEmail'];
    $syncStatus = $_GET['syncStatus'];

    // Exploding the syncStatus parameter
    $format = explode("/", $syncStatus);


    // Fetch event ID
    $query = "SELECT id FROM events 
              WHERE user_email = ? AND event_name = ? AND event_description = ? AND 
                    event_date = ? AND event_start_time = ? AND event_finish_time = ?";

    $stmt = $conn->prepare($query);
    $stmt->bind_param("ssssss", $userEmail, $format[1], $format[3], $format[5], $format[7], $format[9]);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $eventId = $row['id'];

        // Update the event
        $query1 = "UPDATE events 
                   SET event_name = ?, 
                       event_description = ?, 
                       event_date = ?, 
                       event_start_time = ?, 
                       event_finish_time = ?, 
                       sync_status = 'SYNCED' 
                   WHERE id = ?";
        
        $stmt1 = $conn->prepare($query1);
        $stmt1->bind_param("sssssi", $format[2], $format[4], $format[6], $format[8], $format[10], $eventId);
        $stmt1->execute();

        echo "Event updated successfully!";
    } else {
        echo "Event not found!";
    }

}

?>
