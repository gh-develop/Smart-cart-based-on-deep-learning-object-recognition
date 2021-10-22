package com.example.JustCart_ver2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SearchActivity extends Activity {
    private static String IP_ADDRESS = "yeahss.dothome.co.kr";
    private static String TAG = "phptest";

    private TextView mTextViewResult;
    private ImageView imageView;
    private ArrayList<PersonalData> mArrayList;
    private UsersAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private String mJsonString;
    //private Button btn_test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mTextViewResult = (TextView)findViewById(R.id.textView_main_result);
        //imageView = findViewById(R.id.imageView);//이미지 출력
        mRecyclerView = (RecyclerView) findViewById(R.id.listView_main_list);
        //mRecyclerView2 = (RecyclerView) findViewById(R.id.listView_main_list2);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //mRecyclerView2.setLayoutManager(new LinearLayoutManager(this));
        //btn_test = (Button) findViewById(R.id.btn_test);


        mTextViewResult.setMovementMethod(new ScrollingMovementMethod());



        mArrayList = new ArrayList<>(); //Personal객체를 담을 array리스트(어댑터 쪽으로 날릴거임)

        mAdapter = new UsersAdapter(this, mArrayList); //UsersAdapter로부터 생성된 애들을 mArrayList에 담아줌
        mRecyclerView.setAdapter(mAdapter); //mAdapter에 담아져 있는 데이터의 adapter를 recyclerview에 세팅
        //mRecyclerView2.setAdapter(mAdapter);

        Button btn_test = (Button) findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mArrayList.clear(); //기존 배열 리스트가 존재하지 않게 초기화
                mAdapter.notifyDataSetChanged();

                GetData task = new GetData();
                task.execute( "http://" + IP_ADDRESS + "/Loading.php", "");
            }
        });

    }

    private class GetData extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SearchActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d(TAG, "response - " + result);

            if (result == null){

                mTextViewResult.setText(errorString);
            }
            else {

                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }


    private void showResult(){

        String TAG_JSON="results";
        String TAG_NAME = "Name";
        String TAG_PRICE = "Price";
        String TAG_DESC ="Desc";
        String TAG_LOCATION ="Location";
        String TAG_IMAGE ="Image";


        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String Name = item.getString(TAG_NAME);
                String Price = item.getString(TAG_PRICE);
                String Desc = item.getString(TAG_DESC);
                String Location = item.getString(TAG_LOCATION);
                String Image = item.getString(TAG_IMAGE);
                /*
                //서버에 올려둔 이미지 URL로 이미지 불러오기
                URL url = new URL(Image);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setDoInput(true); //Server 통신에서 입력 가능한 상태로 만듦

                conn.connect(); //연결된 곳에 접속할 때 (connect() 호출해야 실제 통신 가능함)
                InputStream is = conn.getInputStream(); //inputStream 값 가져오기

                bitmap = BitmapFactory.decodeStream(is); // Bitmap으로 반환
                imageView.setImageBitmap(bitmap);
                */
                //나머지 정보 불러오기

                //만들어뒀던 Personal객체에 데이터를 담는다
                PersonalData personalData = new PersonalData();

                personalData.setName(Name);
                personalData.setPrice(Price);
                personalData.setDesc(Desc);
                personalData.setLocation(Location);
                personalData.setImage(Image);
                //personalData.setImageView(personalData.getImageView());

                mArrayList.add(personalData); //담은 데이터들을 배열리스트에 넣고 리사이클러뷰로 보낼 준비
                mAdapter.notifyDataSetChanged(); //새로고침
            }



        } catch (JSONException /*| MalformedURLException*/ e) {

            Log.d(TAG, "showResult : ", e);
        } /*catch (IOException e) {
            e.printStackTrace();
        }*/
        mAdapter = new UsersAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);
    }

}
