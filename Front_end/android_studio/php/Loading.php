<?php

    $mysqli = new mysqli("localhost", "yeahss", "yesong826!", "yeahss") or die("connection failed"); //mysql에 접속 시도
	//mysql_select_db("knormal", $con) or die("db selection failed"); //DB를 선택
	//$link = mysqli_connect($host, $user, $password, $dbname);

	$query = "SELECT * FROM SHOPBASKET"; //쿼리문 작성
	//mysqli_query($mysqli,"set names utf8");

	$result = $mysqli->query($query); // mysqli_query([연결 객체], [쿼리]), 데이터를 읽어옴
	$total_record = $result->num_rows;

	$result_array=array();

	for($i=0;$i<$total_record;$i++){
		//한 행씩 일기 위해 offset을 줌
		$result->data_seek($i);
  
		// 결과값을 배열로 바꾼다.
		$row = $result->fetch_array();

		 // 결과값들을 JSON형식으로 넣기 위해 연관배열(key하나, 값 하나)로 넣는다.
		$row_array = array(
		"Name" => $row['Name'],
		"Price" => $row['Price'],
		"Desc" => $row['Desc'],
		"Location" => $row['Location'],
		"Image"=> $row['Image'],
		);
		// 한 행을 results에 넣을 배열의 끝에 추가한다.
		array_push($result_array,$row_array);
	}

	// 위에서 얻은 결과를 다시 JSON형식으로 넣는다.
	$arr = array(
	"status" => "OK",
	"num_result" => "$total_record",
	"results" => $result_array
	);
 
	// 만든건 그냥 배열이므로 JSON형식으로 인코딩을 한다.
	$json_array = json_encode($arr, JSON_UNESCAPED_UNICODE);

	// 인코딩한 JSON배열을 출력한다.
	print_r($json_array);
?>
