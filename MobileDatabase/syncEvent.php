<?php
include_once('connection.php');
if( isset($_GET['userEmail'])&&
    isset($_GET['eventName'])&&
    isset($_GET['eventDescription'])&&
    isset($_GET['eventDate'])&&
    isset($_GET['eventStartTime'])&&
    isset($_GET['eventFinishTime'])){
    $email=$_GET['userEmail'];
    $eventName=$_GET['eventName'];
    $eventDescription=$_GET['eventDescription'];
    $eventDate=$_GET['eventDate'];
    $eventStartTime=$_GET['eventStartTime'];
    $eventFinishTime=$_GET['eventFinishTime'];

    $query="SELECT* FROM events WHERE 
            user_email='$email' AND 
            event_name='$eventName'AND 
            event_description='$eventDescription'AND
            event_start_time='$eventStartTime'AND
            event_finish_time='$eventFinishTime'";
    $result=$conn->query($query);
    if($result->num_rows>0)
    {
        $row=$result->fetch_assoc();
        $id=$row['id'];
        $query1="UPDATE events 
                SET sync_status='SYNCED'
                WHERE id='$id'";
        $conn->query($query1);
    }
    
}
?>