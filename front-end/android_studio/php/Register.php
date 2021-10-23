<?php 
    $con = mysqli_connect("localhost", "knormal", "knormal@0102", "knormal");
    mysqli_query($con,'SET NAMES utf8');

    $userID = $_POST["userID"];
    $userPassword = $_POST["userPassword"];
    $userName = $_POST["userName"];
    $userEmail = $_POST["userEmail"];

    $statement = mysqli_prepare($con, "INSERT INTO USER (userID, userPassword, userName, userEmail) VALUES (?,?,?,?)");
    mysqli_stmt_bind_param($statement, "ssss", $userID, $userPassword, $userName, $userEmail);
    mysqli_stmt_execute($statement);


    $response = array();
    $response["success"] = true;
 
   
    echo json_encode($response);



?>