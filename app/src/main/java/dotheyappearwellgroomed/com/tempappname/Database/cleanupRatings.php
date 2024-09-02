<?php
require "conn.php";

$mysql_qry = "DELETE FROM Person_Location WHERE time < (NOW() - INTERVAL 60 MINUTE)";

if($conn->query($mysql_qry) === TRUE)
{
   echo "Cleanup Successful";
}
else
{
   echo "Error: " . $mysql_qry . "<br>" . $conn->error;
}
$conn->close();
?>