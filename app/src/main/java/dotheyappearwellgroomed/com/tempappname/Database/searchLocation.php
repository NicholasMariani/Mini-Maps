<?php
require "conn.php";

$locationName = $_REQUEST["LocationName"];
$locationCity = $_REQUEST["LocationCity"];
$locationState = $_REQUEST["LocationState"];

$mysql_qry = "select * from Location where name like '$locationName' and city like '$locationCity' and state like '$locationState';";
$result = mysqli_query($conn ,$mysql_qry);

if(mysqli_num_rows($result) > 0) 
{
   
   while($row = mysqli_fetch_assoc($result))
   {
      $output[]=$row;
   }
   print(json_encode($output));
}
else 
{
   echo "no go sry bro";
}

?>