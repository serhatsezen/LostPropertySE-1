<?php

require "init.php";
$Cevap = $_POST['cevap'];
$UserName = $_POST['userName'];
$TokenDevice = $_POST['tokendevice'];
$path_to_fcm = 'https://fcm.googleapis.com/fcm/send';
$server_key = "AIzaSyDr6wjJL4c3VAHRM7g5eV8v68hfiUGkmik";
$sql = "select fcm_token_haber from fcm_haberler";
$result = mysqli_query($con,$sql);
$row = mysqli_fetch_row($result);
$key = $row[0];
$headers = array(
			'Authorization:key ='.$server_key,
			'Content-Type:application/json'
		);

$fields = array(
        'registration_ids' =>  [$TokenDevice],
         'notification' => array('title' => $UserName." Cevap!", 
         'body' => $Cevap,
         'subtitle'      => $Cevap,
         'smallIcon'	=> 'ic',
         'sound' => "default"),
         'data' => array('message' => $Cevap )
        );
        
$payload = json_encode($fields);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $path_to_fcm);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);  
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_IPRESOLVE, CURL_IPRESOLVE_V4);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));		

$result = curl_exec($ch);   
curl_close($ch);
$clos = mysqli_close($con);
if($clos)
	echo "Closed";
else
	echo "Error Closed";


?>