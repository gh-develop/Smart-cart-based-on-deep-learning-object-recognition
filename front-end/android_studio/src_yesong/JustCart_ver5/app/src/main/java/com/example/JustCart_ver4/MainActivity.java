package com.example.JustCart_ver4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.viewpager.widget.ViewPager;

public class MainActivity extends Activity {

    private ImageButton btn_shop, btn_home, btn_event, btn_mypage, imageButton;

    //ListView 참조변수
    ListView listview;

    //이름,가격을 가지고 있는 MemberData 클래스의 객체를 배열로 보관하기 위한 ArrayList 객체 생성
    //MemberData[] 이렇게 선언하는 일반배열은 배열 개수가 정해져 있어서 나중에 추가,삭제가 불편함
    //배열 요소의 개수를 유동적으로 조절할 수 있는 ArrayList 객체로 data 보관

    Adapter adapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.view);
        adapter = new Adapter(this);
        viewPager.setAdapter(adapter);

        btn_shop = findViewById(R.id.btn_shop);
        btn_home = findViewById(R.id.btn_home);
        btn_event = findViewById(R.id.btn_event);
        btn_mypage = findViewById(R.id.btn_mypage);
        imageButton = findViewById(R.id.imageButton);

        //장바구니 버튼을 클릭 시 수행
        btn_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShopActivity.class);
                startActivity(intent);
            }
        });

        //홈 버튼을 클릭 시 수행
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = new Intent(MainActivity.this, MainActivity.class);
                 startActivity(intent);
            }
        });

        //이벤트 버튼을 클릭 시 수행
        btn_event.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MainActivity.this, EventActivity.class);
                 startActivity(intent);
             }
        });

        //마이페이지 버튼을 클릭 시 수행
        btn_mypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MypageActivity.class);
                startActivity(intent);
            }
        });

        //검색 버튼을 클릭 시 수행
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

    }


}


