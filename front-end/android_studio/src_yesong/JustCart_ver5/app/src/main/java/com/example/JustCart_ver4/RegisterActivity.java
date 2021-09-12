package com.example.JustCart_ver4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText et_id, et_pass, et_name, et_age;
    private ImageButton btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_id = findViewById(R.id.et_id);
        et_pass = findViewById(R.id.et_pass);
        et_name = findViewById(R.id.et_name);
        et_age = findViewById(R.id.et_age);

        btn_register = findViewById(R.id.btn_register);
        //회원가입 버튼 눌렀을 때 입력한 정보 모아서 DB에 전송
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){ //회원가입 버튼 눌렀을 때
                //EditText에 현재 입력되어있는 값을 get해온다(문자열 형태로)
                String userID = et_id.getText().toString();
                String userPass = et_pass.getText().toString();
                String userName = et_name.getText().toString();
                int userAge = Integer.parseInt(et_age.getText().toString());

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    //서버로 데이터 넘겨줄 때 json파일 형식으로 넘겨줌
                    public void onResponse(String response) {
                        //response: 회원가입 요청을 한 뒤 결과값(Success)을 jsonObject로 받음
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success"); //success가 진짜 success냐, 서버통신 성공여부 알려줌
                            if(success){ //회원등록에 성공한 경우
                                Toast.makeText(getApplicationContext(),"회원 등록에 성공하였습니다!", Toast.LENGTH_SHORT).show(); //서버통신 성공하면 짧은 시간동안 문장 보여줌
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class); //출발Activity->로그인Activity로 이동
                                startActivity(intent);
                            } else{//회원등록에 실패한 경우
                                Toast.makeText(getApplicationContext(),"회원 등록에 실패하였습니다!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace(); //예외처리들을 가져옴
                        }

                    }
                };
                //서버로 Volley를 이용해서 요청을 함
                RegisterRequest registerRequest = new RegisterRequest(userID, userPass, userName, userAge, responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(registerRequest);
            }
        });

    }
}