<?php

require "init.php";
$UserName = $_POST['userName'];
$TokenDevice = $_POST['tokendevice'];
$bildirimpost = $_POST['bildirimPost'];
$path_to_fcm = 'https://fcm.googleapis.com/fcm/send';
$server_key = "AIzaSyDr6wjJL4c3VAHRM7g5eV8v68hfiUGkmik";
$sql = "select fcm_token_haber from fcm_haberler";
$result = mysqli_query($con,$sql);
$row = mysqli_fetch_row($result);
$headers = array(
			'Authorization:key ='.$server_key,
			'Content-Type:application/json',
			'Accept-Charset: UTF-8'
		);

$fields = array(
        'registration_ids' =>  [$TokenDevice],
         'notification' => array('title' => $UserName, 
         'body' => $bildirimpost,
         'subtitle'      => $bildirimpost,
         'smallIcon'	=> 'ic',
         'sound' => "default"),
         'data' => array('message' => $bildirimpost)
        );
        
$payload = json_encode($fields);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $path_to_fcm);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt($ch, CURLOPT_ENCODING ,"UTF-8", "iso-8859-9");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);  
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_IPRESOLVE, CURL_IPRESOLVE_V4);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));	

$result = curl_exec($ch);
$dom = new DOMDocument('1.0', 'utf-8');
libxml_use_internal_errors(true);
@$dom->loadHTML(mb_convert_encoding($result, 'HTML-ENTITIES', 'UTF-8'));

curl_close($ch);
$clos = mysqli_close($con);

if($clos)
	echo "Closed";
else
	echo "Error Closed";


?>