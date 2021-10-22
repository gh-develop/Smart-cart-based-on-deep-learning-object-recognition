package com.example.JustCart_ver4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MypageActivity extends AppCompatActivity {

    private ImageButton btn_my_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        btn_my_login = findViewById(R.id.btn_my_login);

        //로그인 버튼을 클릭 시 수행
        btn_my_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}