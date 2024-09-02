<?php
require "conn.php";

$locationName = $_REQUEST["LocationName"];
$locationStreetAddress = $_REQUEST["LocationStreetAddress"];
$locationCity = $_REQUEST["LocationCity"];
$locationState = $_REQUEST["LocationState"];
$locationRating = $_REQUEST["LocationRating"];
$personName = $_REQUEST["PersonName"];

$mysql_qry = "select * from Person where name like '$personName';";
$result = mysqli_query($conn ,$mysql_qry);
$PID = '0';

if(mysqli_num_rows($result) > 0) 
{
   	$row = mysqli_fetch_assoc($result);
   	$PID=$row["Pid"];
}
else
{
	$mysql_qry = "insert into Person (name, accuracy) values ('$personName','5')";
	
	if($conn->query($mysql_qry) === TRUE)
	{
	   echo "Insert new Person Successful";
	   echo "<br>";
	}
	else
	{
	   echo "Error: " . $mysql_qry . "<br>" . $conn->error;
	   echo "<br>";
	}
	
	$mysql_qry = "select * from Person where name like '$personName';";
        $result = mysqli_query($conn ,$mysql_qry);

	if(mysqli_num_rows($result) > 0) 
	{
   	   $row = mysqli_fetch_assoc($result);
   	   $PID=$row["Pid"];
	}
}

$mysql_qry = "select * from Location where name like '$locationName' and streetAddress like '$locationStreetAddress' and city like '$locationCity' and state like '$locationState';";
$result = mysqli_query($conn ,$mysql_qry);

if(mysqli_num_rows($result) > 0) 
{
   	$row = mysqli_fetch_assoc($result);
   	$LID=$row["Lid"];
	
	$mysql_qry = "select * from Person_Location where Person_id = '$PID' and Location_id = '$LID';";
	
	$result = mysqli_query($conn ,$mysql_qry);
	
	if(mysqli_num_rows($result) > 0)
	{
	   echo "Removing existing rating with same user and location ";
	   echo "<br>";
	   
	   $mysql_qry = "delete from Person_Location where Person_id = '$PID' and Location_id = '$LID';";
	
	   if($conn->query($mysql_qry) === TRUE)
	   {
	      echo "Delete existing rating Successful";
	      echo "<br>";
	   }
	   else
	   {
	      echo "Error: " . $mysql_qry . "<br>" . $conn->error;
	      echo "<br>";
	   }
	}
	else 
	{
	   echo "no existing rating with same user and location exists";
	   echo "<br>";
	}
	
	$mysql_qry = "insert into Person_Location (Person_id, Location_id, rating) values ('$PID','$LID','$locationRating')";
	
	if($conn->query($mysql_qry) === TRUE)
	{
	   echo "Insert new rating Successful";
	   echo "<br>";
	}
	else
	{
	   echo "Error: " . $mysql_qry . "<br>" . $conn->error;
	   echo "<br>";
	}
	   
	$mysql_qry = "UPDATE Location SET busyRating = 
			  (Select AVG(rating) FROM Person_Location 
	            	  WHERE Location_id = '$LID') 
	             WHERE name like '$locationName' and streetAddress like '$locationStreetAddress' and city like '$locationCity' and state like '$locationState';";
	
	if($conn->query($mysql_qry) === TRUE)
	{
	   echo "Update Rating in Location table Successful";
	   echo "<br>";
	}
	else
	{
	   echo "Error: " . $mysql_qry . "<br>" . $conn->error;
	}
	
}
else
{
   echo "Location not found";
}

$conn->close();

?>