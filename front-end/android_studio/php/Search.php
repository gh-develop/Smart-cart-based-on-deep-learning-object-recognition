<?php

	error_reporting(E_ALL); 
	ini_set('display_errors',1); 

	include('dbcon.php');


	//POST 값을 읽어온다.
	$Name=isset($_POST['Name']) ? $_POST['Name'] : ''; //이름값 가져옴
	$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

	if ($Name != "" ){ 

    $sql="select * from SHOPBASKET where Name='$Name'";
    $stmt = $con->prepare($sql);
    $stmt->execute();
 
    if ($stmt->rowCount() == 0){

        echo "'";
        echo $Name;
        echo "'은 찾을 수 없습니다.";
    }
	else{

   		$data = array(); 

        while($row=$stmt->fetch(PDO::FETCH_ASSOC)){

        	extract($row);

			//data배열 만듬
            array_push($data, 
                array('Name'=>$row["Name"],
                'Price'=>$row["Price"],
                'Desc'=>$row["Desc"],
				'Location'=>$row["Location"],
				'Image'=>$row["Image"],
            ));
        }


        if (!$android) {
            echo "<pre>"; 
            print_r($data); 
            echo '</pre>';
        }else
        {
            header('Content-Type: application/json; charset=utf8');
            $json = json_encode(array("webnautes"=>$data), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE); //한글출력 위함(webnautes로 출력)
            echo $json;
        }
    }
}
else {
    echo "검색할 이름을 입력하세요 ";
}

?>

<?php

$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

if (!$android){
?>

<html>
   <body>
   
      <form action="<?php $_PHP_SELF ?>" method="POST">
         이름: <input type = "text" name = "Name" />
         <input type = "submit" />
      </form>
   
   </body>
</html>
<?php
}

   
?>