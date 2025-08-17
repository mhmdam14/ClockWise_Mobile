<?php
include_once("connection.php");


    $query="SELECT * FROM events";
    $result =mysqli_query($conn,$query);
    $array=array();
    while($row=mysqli_fetch_array($result)){
        $row_array['id']= $row['id'];
        $row_array['email']= $row['user_email'];
        $row_array['eventName']= $row['event_name'];
        $row_array['eventDescription']= $row['event_description'];
        $row_array['eventDate']= $row['event_date'];
        $row_array['eventStartTime']= $row['event_start_time'];
        $row_array['eventFinishTime']= $row['event_finish_time'];
        $row_array['syncStatus']= $row['sync_status'];
        array_push($array, $row_array);
}
echo json_encode($array);


?>