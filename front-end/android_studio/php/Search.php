# -*- coding: utf-8 -*-
<?php

	error_reporting(E_ALL); 
	ini_set('display_errors',1); 

	include('dbcon.php');


	//POST ���� �о�´�.
	$Name=isset($_POST['Name']) ? $_POST['Name'] : ''; //�̸��� ������
	$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

	if ($Name != "" ){ 

    $sql="select * from SHOPBASKET where Name='$Name'";
    $stmt = $con->prepare($sql);
    $stmt->execute();
 
    if ($stmt->rowCount() == 0){

        echo "'";
        echo $Name;
        echo "'�� ã�� �� �����ϴ�.";
    }
	else{

   		$data = array(); 

        while($row=$stmt->fetch(PDO::FETCH_ASSOC)){

        	extract($row);

			//data�迭 ����
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
            $json = json_encode(array("webnautes"=>$data), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE); //�ѱ���� ����(webnautes�� ���)
            echo $json;
        }
    }
}
else {
    echo "�˻��� �̸��� �Է��ϼ��� ";
}

?>

<?php

$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

if (!$android){
?>

<html>
   <body>
   
      <form action="<?php $_PHP_SELF ?>" method="POST">
         �̸�: <input type = "text" name = "Name" />
         <input type = "submit" />
      </form>
   
   </body>
</html>
<?php
}

   
?>