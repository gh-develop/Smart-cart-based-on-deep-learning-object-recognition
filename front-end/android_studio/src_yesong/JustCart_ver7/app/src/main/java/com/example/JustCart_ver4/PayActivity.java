package com.example.JustCart_ver4;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PayActivity extends Activity {

    private TextView text_test;
    private String USER_ID, ORDER_ID, TOTAL_PRICE;
    private ArrayList<CheckData> mArrayList;
    private TextView mTextViewResult, textView15;
    private String mJsonString;
    private static String TAG = "phpquerytest";
    private static final String TAG_JSON="webnautes";
    //private static final String TAG_NAME = "productName";
    private CheckAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        ShopActivity2 shopActivity2 = (ShopActivity2)ShopActivity2.shopActivity;
        shopActivity2.finish();

        //MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
        //mainActivity.finish();
        //Intent intent = getIntent();
        mTextViewResult = (TextView) findViewById(R.id.textView_main_result2);

        USER_ID = SharedPreference.getUserID(PayActivity.this);
        ORDER_ID = SharedPreference.getOrderID(PayActivity.this);
        TOTAL_PRICE = SharedPreference.getTotalPrice(PayActivity.this);
        showResult2(TOTAL_PRICE, USER_ID, ORDER_ID);
        String A = USER_ID+ORDER_ID+TOTAL_PRICE;


        text_test = (TextView) findViewById(R.id.text_test);

        //Handler mHandler = new Handler();

        //mHandler.postDelayed(new Runnable(){
            //public void run(){
                //Intent intent = new Intent(PayActivity.this, ShopActivity2.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//???????????? ????????????
                //startActivity(intent);
                //finish();
                //overridePendingTransition(0, 0); //????????? ??????????????? ?????????



                /*
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }

                 */
            //}
        //}, 1000);


        mArrayList = new ArrayList<>(); //Personal????????? ?????? array?????????(????????? ????????? ????????????)

    }

    //?????????????????? ?????? ?????? ??????
    void showResult2(String stringData1, String stringData2, String stringData3) {
        GetData task3 = new GetData();
        task3.execute(stringData1, stringData2, stringData3);
    }

    //?????????????????? ??????
    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(PayActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) { //???????????? ?????? ??????????????? ?????????/ ????????? JSON???????????? ????????? ?????????
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
        protected String doInBackground(String... params) { //php?????? ??????

            String searchKeyword1 = params[0];
            String searchKeyword2 = params[1];
            String searchKeyword3 = params[2];

            String serverURL = "http://3.37.3.112/Update_totalprice.php";
            String postParameters = "&total_price=" + searchKeyword1 + "&userID=" + searchKeyword2 + "&order_id=" + searchKeyword3;


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

    private void showResult() { //DB?????? ???????????? ??? ?????????

        try {
            JSONObject jsonObject = new JSONObject(mJsonString); //json?????? ?????????
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON); //json????????? result array ?????????
            String result = "";

            //textView3.setText(result);
            //mAdapter = new CheckAdapter(this, mArrayList);
            //mRecyclerView.setAdapter(mAdapter);

            //????????? btn_add??? ????????????
            //??????????????? ???????????? Name??? ?????? + ??????????????? DB??? ???????????????





        } catch (JSONException /*| MalformedURLException*/ e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    //???????????? ?????? ????????? ???
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PayActivity.this, ShopActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
