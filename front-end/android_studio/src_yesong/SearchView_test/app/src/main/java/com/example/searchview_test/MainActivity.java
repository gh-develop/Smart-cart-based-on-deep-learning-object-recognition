package com.example.searchview_test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class MainActivity extends AppCompatActivity {
    //private List<String> items = Arrays.asList("apple", "ap24", "brain", "captin", "carrot");

    private static String TAG = "phpquerytest";

    private static final String TAG_JSON="webnautes";
    private static final String TAG_NAME = "Name";
    private static final String TAG_PRICE = "Price";
    private static final String TAG_DESC ="Desc";
    private static final String TAG_LOCATION ="Location";
    private static final String TAG_IMAGE ="Image";


    //private static String IP_ADDRESS = "yeahss.dothome.co.kr";
    //private static String TAG = "phptest";

    private TextView mTextViewResult;
    //private ImageView imageView;
    //private ImageButton btn_shop, btn_home, btn_mypage;
    //ArrayList<HashMap<String, String>> mArrayList;
    private ArrayList<PersonalData> mArrayList;
    EditText mEditTextSearchKeyword;
    private UsersAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private String mJsonString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mTextViewResult = (TextView) findViewById(R.id.textView_main_result);
        mRecyclerView = (RecyclerView) findViewById(R.id.listView_main_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //btn_test = (Button) findViewById(R.id.btn_test);
        mEditTextSearchKeyword = (EditText) findViewById(R.id.editText_main_searchKeyword);
        Button button_search = (Button) findViewById(R.id.button_main_search);
        button_search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mArrayList.clear();


                GetData task = new GetData();
                task.execute(mEditTextSearchKeyword.getText().toString());
            }
        });

        mArrayList = new ArrayList<>(); //Personal????????? ?????? array?????????(????????? ????????? ????????????)

    }

/*
        SearchView searchView = findViewById(R.id.search_view);
        TextView resultTextView = findViewById(R.id.textView_main_result);
        //resultTextView.setText(getResult()); //????????? ????????? ????????? ?????? ????????????

        mTextViewResult.setMovementMethod(new ScrollingMovementMethod());

        mArrayList = new ArrayList<>(); //Personal????????? ?????? array?????????(????????? ????????? ????????????)

        mAdapter = new UsersAdapter(this, mArrayList); //UsersAdapter????????? ????????? ????????? mArrayList??? ?????????
        mRecyclerView.setAdapter(mAdapter); //mAdapter??? ????????? ?????? ???????????? adapter??? recyclerview??? ??????

        Button btn_test = (Button) findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mArrayList.clear(); //?????? ?????? ???????????? ???????????? ?????? ?????????
                mAdapter.notifyDataSetChanged();

                GetData task = new GetData();
                task.execute( "http://" + IP_ADDRESS + "/Loading.php", "");
            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { //???????????? ???????????? ?????? ????????? ??? ???????????? ??????
                resultTextView.setText(search(query)); //????????? ????????? ????????? ?????? ????????????
                return true;
            }//??????????????? ???????????? ??? ??????

            @Override
            public boolean onQueryTextChange(String newText) { //???????????? ?????? ?????????????????? ??????
                /////////////////////////////???????????? ??????
                /*
                if(newText.equals("")){
                    this.onQueryTextSubmit("");
                }
                */
 //               return false;

 //           }
 //        });


 //   }



//    private String search(String query){//??????????????? ???????????????????????? ????????? ?????????: query

        /*
        mArrayList.clear(); //?????? ?????? ???????????? ???????????? ?????? ?????????
        mAdapter.notifyDataSetChanged();

        GetData task = new GetData();
        task.execute( "http://" + IP_ADDRESS + "/Loading.php", "");

        */
        //showResult(query);//showResult??? query??????
//        String a=showResult(query);

        /*
        StringBuilder sb = new StringBuilder();
        for (int i= 0;i<items.size();i++){
            String item = items.get(i); //items??? ????????? ??????
            if (item.contains(query)){ //????????? query??? ????????? ????????????
                sb.append(item); //stringbuilder??? ????????????
                if(i != items.size()-1){
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
        */
//        return a;
//    }

    /*
    private String getResult() { //?????? ???????????? ??????
        StringBuilder sb = new StringBuilder();
        for (int i= 0;i<items.size();i++){
            String item = items.get(i); //items??? ????????? ??????
            if (i == items.size()-1){
                sb.append(item);
            } else {
                sb.append(item + "\n"); //stringbuilder??? ????????????
            }
        }

        return sb.toString();
    }
    */

    private class GetData extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) { //???????????? ?????? ??????????????? ?????????/ ????????? JSON???????????? ????????? ?????????
            super.onPostExecute(result);

            progressDialog.dismiss();
            mTextViewResult.setText(result);
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

            String serverURL = "http://yeahss.dothome.co.kr/Search.php";
            String postParameters = "&Name=" + searchKeyword;


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

            for (int i = 0; i < jsonArray.length(); i++) {

                //??? ??????????????? ?????????-> ????????? ??????????????? ??????????????? ???????????? ??????
                JSONObject item = jsonArray.getJSONObject(i);

                String Name = item.getString(TAG_NAME);
                String Price = item.getString(TAG_PRICE);
                String Desc = item.getString(TAG_DESC);
                String Location = item.getString(TAG_LOCATION);
                String Image = item.getString(TAG_IMAGE);

                //??????????????? Personal????????? ???????????? ?????????
                PersonalData personalData = new PersonalData();

                personalData.setName(Name);
                personalData.setPrice(Price);
                personalData.setDesc(Desc);
                personalData.setLocation(Location);
                personalData.setImage(Image);

                mArrayList.add(personalData); //?????? ??????????????? ?????????????????? ?????? ????????????????????? ?????? ??????
                mAdapter.notifyDataSetChanged(); //????????????
            }

            mAdapter = new UsersAdapter(this, mArrayList);
            mRecyclerView.setAdapter(mAdapter);

        } catch (JSONException /*| MalformedURLException*/ e) {

            Log.d(TAG, "showResult : ", e);
        }

    }
}