package com.example.JustCart_ver4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MypageActivity extends AppCompatActivity {

    private ImageButton btn_shop, btn_home, btn_mypage, btn_qrcode;
    private Button btn_logout;
    private TextView tv_id, tv_name, tv_email, tv_age;
    public static Activity mypageActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        mypageActivity = MypageActivity.this;


        //메인->마이 이동시 메인 페이지 삭제
        //MainActivity mainActivity = (MainActivity) MainActivity.mainActivity;
        //mainActivity.finish();


        //장바구니->마이 이동시 장바구니 페이지 삭제
        //ShopActivity2 shopActivity = (ShopActivity2) ShopActivity2.shopActivity;
        //shopActivity.finish();
/*
        //이벤트->마이 이동시 이벤트 페이지 삭제
        EventActivity eventActivity = (EventActivity) EventActivity.eventActivity;
        eventActivity.finish();


 */




        btn_qrcode = findViewById(R.id.btn_qrcode);

        btn_shop = findViewById(R.id.btn_shop7);
        btn_home = findViewById(R.id.btn_home7);
        btn_mypage = findViewById(R.id.btn_mypage7);
        btn_logout = findViewById(R.id.btn_logout);

        tv_id = findViewById(R.id.tv_id);
        tv_name = findViewById(R.id.tv_name);
        tv_email = findViewById(R.id.tv_email);
        //tv_age = findViewById(R.id.tv_email);

        Intent intent = getIntent();
        String userID = SharedPreference.getUserID(MypageActivity.this);
        String userName = SharedPreference.getUserName(MypageActivity.this);
        String userEmail = SharedPreference.getUserEmail(MypageActivity.this);

        tv_id.setText(userID);
        tv_name.setText(userName);
        tv_email.setText(userEmail);

        //QR 버튼을 클릭 시 수행
        btn_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, CreateQR.class);
                startActivity(intent);
            }
        });

        //장바구니 버튼을 클릭 시 수행
        btn_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, ShopActivity2.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        //홈 버튼을 클릭 시 수행
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });


        //마이페이지 버튼을 클릭 시 수행
        btn_mypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, MypageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        //로그아웃 버튼을 클릭 시 수행
        btn_logout.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "로그아웃 성공!", Toast.LENGTH_SHORT).show();
                SharedPreference.clearUserName(MypageActivity.this);
                //Intent intent = new Intent(MypageActivity.this,MainActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //intent.putExtra("KILL",true);
                //startActivity(intent);
                finish();
            }
        });
    }
}