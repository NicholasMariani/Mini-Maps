<?php
require "conn.php";

$locationName = $_REQUEST["LocationName"];
$locationStreetAddress = $_REQUEST["LocationStreetAddress"];
$locationCity = $_REQUEST["LocationCity"];
$locationState = $_REQUEST["LocationState"];
$locationType = $_REQUEST["LocationType"];

$mysql_qry = "insert into Location (name, streetAddress, city, state, type) values ('$locationName', '$locationStreetAddress', '$locationCity', '$locationState', '$locationType')";

if($conn->query($mysql_qry) === TRUE)
{
   echo "Insert Successful";
}
else
{
   echo "Error: " . $mysql_qry . "<br>" . $conn->error;
}
$conn->close();

?>