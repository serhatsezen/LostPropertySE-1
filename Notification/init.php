<?php
$host = "xx.xx.xx.xx";
$db_user = "xxxxx";
$db_password = "xxxx";
$db_name = "xxxxx";

$con = mysqli_connect($host,$db_user,$db_password,$db_name);

if($con)
	echo "Connection Success....";
else
	echo "Connection Error....";




?>