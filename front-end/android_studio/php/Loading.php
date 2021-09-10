<?php

    $mysqli = new mysqli("localhost", "knormal", "knormal@0102", "knormal") or die("connection failed"); //mysql�� ���� �õ�
	//mysql_select_db("knormal", $con) or die("db selection failed"); //DB�� ����
	//$link = mysqli_connect($host, $user, $password, $dbname);

	$query = "SELECT * FROM SHOPBASKET"; //������ �ۼ�
	//mysqli_query($mysqli,"set names utf8");

	$result = $mysqli->query($query); // mysqli_query([���� ��ü], [����]), �����͸� �о��
	$total_record = $result->num_rows;

	$result_array=array();

	for($i=0;$i<$total_record;$i++){
		//�� �྿ �ϱ� ���� offset�� ��
		$result->data_seek($i);
  
		// ������� �迭�� �ٲ۴�.
		$row = $result->fetch_array();

		 // ��������� JSON�������� �ֱ� ���� �����迭(key�ϳ�, �� �ϳ�)�� �ִ´�.
		$row_array = array(
		"Name" => $row['Name'],
		"Price" => $row['Price'],
		"Desc" => $row['Desc'],
		"Location" => $row['Location'],
		"Image"=> $row['Image'],
		);
		// �� ���� results�� ���� �迭�� ���� �߰��Ѵ�.
		array_push($result_array,$row_array);
	}

	// ������ ���� ����� �ٽ� JSON�������� �ִ´�.
	$arr = array(
	"status" => "OK",
	"num_result" => "$total_record",
	"results" => $result_array
	);
 
	// ����� �׳� �迭�̹Ƿ� JSON�������� ���ڵ��� �Ѵ�.
	$json_array = json_encode($arr, JSON_UNESCAPED_UNICODE);

	// ���ڵ��� JSON�迭�� ����Ѵ�.
	print_r($json_array);
?>
