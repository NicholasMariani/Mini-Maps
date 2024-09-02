<?php

$conn= mysqli_connect("localhost", "myUsernameThatIUsedWouldBeHere", "HahNoPasswordForYou", "isitbusydb");

    //Checking Connection
if (mysqli_connect_errno()) {
    printf("Connect failed: %s\n", mysqli_connect_error());
    exit();
}

?>