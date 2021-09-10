package com.example.JustCart_ver4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class EventActivity extends AppCompatActivity {

    private ImageButton btn_shop, btn_home, btn_mypage;

    private ImageButton btn_event1, btn_event2, btn_event3;

    //private View view = View.inflate(R.layout.frag_event1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);


        btn_shop = findViewById(R.id.btn_shop3);
        btn_home = findViewById(R.id.btn_home3);
        btn_mypage = findViewById(R.id.btn_mypage3);

        btn_event1 = findViewById(R.id.btn_event1);
        btn_event2 = findViewById(R.id.btn_event2);
        btn_event3 = findViewById(R.id.btn_event3);

        btn_event1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, Frag_event1Activity.class);
                startActivity(intent);
            }
        });

        btn_event2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, Frag_event2Activity.class);
                startActivity(intent);
            }
        });

        btn_event3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, Frag_event3Activity.class);
                startActivity(intent);
            }
        });

        //장바구니 버튼을 클릭 시 수행
        btn_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, ShopActivity.class);
                startActivity(intent);
            }
        });

        //홈 버튼을 클릭 시 수행
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //마이페이지 버튼을 클릭 시 수행
        btn_mypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}