<?php

	error_reporting(E_ALL); 
	ini_set('display_errors',1); 

	include('dbcon.php');


	//POST 값을 읽어온다.
	$userID = isset($_POST['userID']) ? $_POST['userID'] : ''; //이름값 가져옴
	$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

	if ($userID != "" ){ 

    $sql="select max(order_id) from USERORDER where userID='$userID'";
    $stmt = $con->prepare($sql);
    $stmt->execute();
 
    if ($stmt->rowCount() == 0){

        echo "'";
        echo $userID;
        echo "'은 찾을 수 없습니다.";
    }
	else{

   		$data = array(); 

        while($row=$stmt->fetch(PDO::FETCH_ASSOC)){

        	extract($row);

			//data배열 만듬
            array_push($data, 
                array('order_id'=>$row["max(order_id)"],
            ));
        }


        if (!$android) {
            echo "<pre>"; 
            print_r($data); 
            echo '</pre>';
        }else
        {
            header('Content-Type: application/json; charset=utf8');
            $json = json_encode(array("webnautes"=>$data), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE); //한글출력 위함
            echo $json;
        }
    }
}
else {
    echo "검색할 아이디를 입력하세요 ";
}

?>

<?php

$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

if (!$android){
?>

<html>
   <body>
   
      <form action="<?php $_PHP_SELF ?>" method="POST">
         아이디: <input type = "text" name = "userID" />
         <input type = "submit" />
      </form>
   
   </body>
</html>
<?php
}

   
?>