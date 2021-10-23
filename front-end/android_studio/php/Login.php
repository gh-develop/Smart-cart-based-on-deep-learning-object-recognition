<?php
    $con = mysqli_connect("localhost", "knormal", "knormal@0102", "knormal");
    mysqli_query($con,'SET NAMES utf8');

    $userID = $_POST["userID"];
    $userPassword = $_POST["userPassword"];
    
    $statement = mysqli_prepare($con, "SELECT userID, userPassword, userName, userEmail FROM USER WHERE userID = ? AND userPassword = ?");
    mysqli_stmt_bind_param($statement, "ss", $userID, $userPassword);
    mysqli_stmt_execute($statement);


    mysqli_stmt_store_result($statement);
    mysqli_stmt_bind_result($statement, $userID, $userPassword, $userName, $userEmail);

    $response = array();
    $response["success"] = false;
 
    while(mysqli_stmt_fetch($statement)) {
        $response["success"] = true;
        $response["userID"] = $userID;
        $response["userPassword"] = $userPassword;
        $response["userName"] = $userName;
		$response["userEmail"] = $userEmail;
        //$response["userAge"] = $userAge;        
    }

    echo json_encode($response);



?>