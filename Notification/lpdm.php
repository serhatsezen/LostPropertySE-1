<?php
require "init.php";

$Cevap = $_POST['dm'];
$UserName = $_POST['userName'];
$SenderName = $_POST['sender_name'];
$TokenDevice = $_POST['tokeNDevice'];
$ReceiverName = $_POST['receiver_name'];

$path_to_fcm = 'https://fcm.googleapis.com/fcm/send';
$server_key = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";


$headers = array(
			'Authorization:key ='.$server_key,
			'Content-Type:application/json',
		);

$fields = array(
        'registration_ids' =>  [$TokenDevice],
         'notification' => array('title' =>$UserName ." Mesaj Gönderdi!", 
         'body' => $Cevap,
         'subtitle'      => $Cevap,
         'smallIcon'	=> 'ic',
         'click_action' =>'Chat',
         'sound' => "default"),
         'data' => array('sender_name' =>$SenderName,
         'receiver_name'	=> $ReceiverName,
         'msg' => $Cevap,
         'title' => $UserName ." Mesaj Gönderdi!",
         'click_action' =>'Chat')
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