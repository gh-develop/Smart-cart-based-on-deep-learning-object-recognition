package com.example.JustCart_ver4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

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
import java.util.HashMap;
import java.util.Map;


public class CreateQR extends AppCompatActivity {

    private ImageView ivOutput, iv_qrSmallView;
    private Button btGenerate;
    private EditText etInput;
    private String qr_user_id;
    private String user_nickname;
    private String USER_ID;
    private TextView tv_userID, result;

    private static String TAG = "phpquerytest";

    private static final String TAG_JSON="webnautes";
    //private static final String TAG_USER = "userID";
    private static final String TAG_ORDER = "order_id";


    private TextView mTextViewResult;
    private ArrayList<PersonalData> mArrayList;
    EditText mEditTextSearchKeyword;
    private UsersAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private String mJsonString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qr);

        ivOutput = findViewById(R.id.qrBigView);
        tv_userID = findViewById(R.id.tv_userID);
        result = findViewById(R.id.textView14);

        Intent intent =getIntent();
        USER_ID = SharedPreference.getUserID(CreateQR.this);
        tv_userID.setText(USER_ID);



        MultiFormatWriter multiFormatWriter  = new MultiFormatWriter();
        try{
            BitMatrix bitmatrix = multiFormatWriter .encode(USER_ID, BarcodeFormat.QR_CODE, 350, 350);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitmatrix);
            ivOutput.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                //????????? ??? ????????? ?????? ??????
                showResult2(USER_ID);
                mArrayList = new ArrayList<>(); //Personal????????? ?????? array?????????(????????? ????????? ????????????)
                finish();
            }
        }, 5000);// 5??? ?????? ???????????? ??? ??? ??????
        Toast.makeText(getApplicationContext(),"5??? ??? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
*/



        //showResult2(USER_ID);
        //mArrayList = new ArrayList<>(); //Personal????????? ?????? array?????????(????????? ????????? ????????????)
    }

    //???????????? ?????? ????????? ???
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showResult2(USER_ID);
        mArrayList = new ArrayList<>(); //Personal????????? ?????? array?????????(????????? ????????? ????????????)
        finish();
    }

    void showResult2(String stringData) {//?????????????????? ?????? ?????? ??????
        CreateQR.GetData task2 = new CreateQR.GetData();
        task2.execute(stringData);
    }


    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(CreateQR.this,
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

            String searchKeyword = params[0];

            String serverURL = "http://3.37.3.112/Order_id.php";
            String postParameters = "&userID=" + searchKeyword;


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
            String a = "";

            for (int i = 0; i < jsonArray.length(); i++) {

                //??? ??????????????? ?????????-> ????????? ??????????????? ??????????????? ???????????? ??????
                JSONObject item = jsonArray.getJSONObject(i);

                //String User = item.getString(TAG_USER);
                String Order = item.getString(TAG_ORDER);

                Intent intent = new Intent(CreateQR.this, MainActivity.class); //??????Activity->?????????Activity??? ??????
                SharedPreference.setOrderID(CreateQR.this, Order);

                a=SharedPreference.getOrderID(CreateQR.this);
                //??????????????? Personal????????? ???????????? ?????????
                //PersonalData personalData = new PersonalData();

                //personalData.setName(User);
                //personalData.setPrice(Order);

                //mArrayList.add(personalData); //?????? ??????????????? ?????????????????? ?????? ????????????????????? ?????? ??????

            }
            result.setText(a);
            //mAdapter = new UsersAdapter(this, mArrayList);
            //mRecyclerView.setAdapter(mAdapter);

        } catch (JSONException /*| MalformedURLException*/ e) {

            Log.d(TAG, "showResult : ", e);
        }

    }
}