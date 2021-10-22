package com.example.JustCart_ver2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.JustCart_ver2.Adapter;

public class EventActivity extends AppCompatActivity {

    private ImageButton btn_shop, btn_home, btn_mypage;

    Adapter adapter;
    ViewPager viewPager;

    //private View view = View.inflate(R.layout.frag_event1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        viewPager = (ViewPager) findViewById(R.id.view_event1);
        adapter = new Adapter(this);
        viewPager.setAdapter(adapter);

        viewPager = (ViewPager) findViewById(R.id.view_event2);
        adapter = new Adapter(this);
        viewPager.setAdapter(adapter);

        viewPager = (ViewPager) findViewById(R.id.view_event3);
        adapter = new Adapter(this);
        viewPager.setAdapter(adapter);

        btn_shop = findViewById(R.id.btn_shop3);
        btn_home = findViewById(R.id.btn_home3);
        btn_mypage = findViewById(R.id.btn_mypage3);

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

        //이벤트 버튼을 클릭 시 수행
        btn_mypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, EventActivity.class);
                startActivity(intent);
            }
        });
    }
}