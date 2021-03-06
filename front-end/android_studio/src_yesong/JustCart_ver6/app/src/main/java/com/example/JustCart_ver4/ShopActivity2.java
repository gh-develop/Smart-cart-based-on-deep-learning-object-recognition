package com.example.JustCart_ver4;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pedro.library.AutoPermissions;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;
import org.altbeacon.beacon.service.RangedBeacon;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;
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
import java.util.Collection;
import java.util.List;

public class ShopActivity2 extends AppCompatActivity {

    private ImageButton btn_shop, btn_home, btn_mypage, btn_pay;
    //private BeaconManager beaconManager;
    private String TAG2 = "ShopActivity";
    public static Activity shopActivity;
    private List<Beacon> beaconList = new ArrayList<>();
    private String USER_ID, ORDER_ID;

    private static String TAG = "phpquerytest";

    private static final String TAG_JSON="webnautes";
    private static final String TAG_productNAME = "productName";
    private static final String TAG_productPrice = "productPrice";
    private static final String TAG_classNum ="classNum";
    private BeaconManager beaconManager;


    private TextView mTextViewResult, text_totalprice;
    private ArrayList<PersonalData> mArrayList;
    EditText mEditTextSearchKeyword;
    private ShopAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private String mJsonString;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //???????????? ????????? ???
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        shopActivity = ShopActivity2.this;

        new Handler().postDelayed(new Runnable()
        {

            @Override
            public void run()
            {

                ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> info = manager.getRunningTasks(1);
                if(info.get(0).topActivity.getClassName().equals("com.example.JustCart_ver4.ShopActivity2")){
                    //????????? ??? ????????? ?????? ??????
                    Intent intent = new Intent(getApplicationContext(),ShopActivity2.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //getWindow().setWindowAnimations(0);
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                    finish();

                }
            }
        }, 5000);// 5??? ?????? ???????????? ??? ??? ??????
        //????????????
        //AutoPermissions.Companion.loadAllPermissions(this,101); // AutoPermissions

        beaconManager = BeaconManager.getInstanceForApplication(this);
        //BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);

        //beaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);
        //RunningAverageRssiFilter.setSampleExpirationMilliseconds(3000l);
        //RangedBeacon.setSampleExpirationMilliseconds(3000l);


        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")); //iBeacon??? layout

        btn_pay = findViewById(R.id.btn_pay);

        //???????????? ????????? ?????? ??? ??????
        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopActivity2.this, CardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });


        //beaconManager.setBackgroundBetweenScanPeriod(0);
        //beaconManager.setBackgroundScanPeriod(1100);

        // If you don't want to stop scanning every other minute, use code like this, which immediately starts a new scan cycle after the last one ends
        //beaconManager.setForegroundScanPeriod(1100);
        //beaconManager.setForegroundBetweenScanPeriod(0);

        //????????????
/*
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

 */

        //beaconManager.setDebug(true);

/*
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                        String uuid = beacon.getId1().toString(); //beacon uuid
                        int major = beacon.getId2().toInt(); //beacon major
                        int minor = beacon.getId3().toInt();// beacon minor
                        String address = beacon.getBluetoothAddress();

                        if (((Beacon) beacons.iterator().next()).getDistance() < 0.5 && minor == 55155) {// minor??? 55177??? ?????? ??????
                            Log.i(TAG, "The first beacon I see is about " + minor + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            Log.i(TAG, "The first beacon I see is about " + ((Beacon) beacons.iterator().next()).getDistance() + " meters away.");
                            Intent intent = new Intent(ShopActivity2.this, PayActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.i(TAG, "The no beacon I see is about " + ((Beacon) beacons.iterator().next()).getDistance() + " meters away.");
                        }

                    }

                }
            }
        });

        beaconManager.startRangingBeacons(new Region("AC:23:3F:7E:09:8C", null, null, null));

*/

/*
        //???????????? ?????? ????????? ???
        @Override
        public void onBackPressed() {
            super.onBackPressed();
            //stopPlay(); //??? ?????????????????? ??????????????? ?????? ?????? ?????????????????? ??????
            Toast.makeText(ShopActivity2.this, "?????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();   //????????? ?????????
            Intent intent = new Intent(ShopActivity2.this, ShopActivity2.class); //?????? ?????????????????? ?????? ??????????????? ???????????? ????????? ??????
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //????????? ????????? ??????
            startActivity(intent);  //????????? ??????
            finish();   //?????? ???????????? ??????
        }


 */



        View view =findViewById(R.id.view1);
        view.setClickable(true);
        /*
        try {
            //TODO ???????????? ?????? ????????? ????????? ??????
            Intent intent = getIntent();
            finish(); //?????? ???????????? ?????? ??????
            overridePendingTransition(0, 0); //????????? ??????????????? ?????????
            startActivity(intent); //?????? ???????????? ????????? ??????
            overridePendingTransition(0, 0); //????????? ??????????????? ?????????
        }
        catch (Exception e){
            e.printStackTrace();
        }

         */

        USER_ID = SharedPreference.getUserID(ShopActivity2.this);
        ORDER_ID = SharedPreference.getOrderID(ShopActivity2.this);

        //???????????? ????????????(??????????????? ??????)
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float distance = 0;
                float pressedX = 0;
                float pressedX2 = 0;

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    // ???????????? touch ?????? ??? x ????????? ??????
                    pressedX = event.getX();
                    Log.d("viewTest", "newXvalue : " + pressedX);    // View ???????????? ????????? ????????? ?????? ?????????.
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    pressedX2 = event.getX();
                    distance = pressedX - pressedX2;  // View ???????????? ????????? ????????? ?????? ?????????.
                    Log.d("viewTest", "Distance : "+ distance);
                }


                // ?????? ????????? 100??? ?????? ????????? ????????? ?????? ?????? ?????????.
                if (Math.abs(distance) < 200) {
                    return false;
                }

                if (distance > 0) {
                    // ???????????? ???????????? ??????????????? ????????? ????????? ???????????? ??????.
                    Intent intent = new Intent(ShopActivity2.this, CheckActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                } else {
                    // ???????????? ??????????????? ??????????????? ?????? ????????? ???????????? ??????.
                    Intent intent = new Intent(ShopActivity2.this, CheckActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in, R.anim.right_out);
                }
                finish(); // finish ????????? ????????? activity??? ?????? ?????????.

                return true;
            }
        });

        btn_shop = findViewById(R.id.btn_shop2);
        btn_home = findViewById(R.id.btn_home2);
        btn_mypage = findViewById(R.id.btn_mypage2);

        //???????????? ????????? ?????? ??? ??????
        btn_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopActivity2.this, ShopActivity2.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        //??? ????????? ?????? ??? ??????
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopActivity2.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        //??????????????? ????????? ?????? ??? ??????
        btn_mypage.setOnClickListener(new View.OnClickListener() {
            Intent intent =getIntent();
            @Override
            public void onClick(View v) {
                if(SharedPreference.getUserName(ShopActivity2.this).length() == 0) {
                    Intent intent = new Intent(ShopActivity2.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(ShopActivity2.this, MypageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("STD_NUM", SharedPreference.getUserName(ShopActivity2.this).toString());
                    startActivity(intent);
                    finish();
                }
            }
        });

        mTextViewResult = (TextView) findViewById(R.id.textView13);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView13);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        text_totalprice = (TextView) findViewById(R.id.text_totalprice);
        //mEditTextSearchKeyword = (EditText) findViewById(R.id.editText_main_searchKeyword);

        showResult2(USER_ID, ORDER_ID);

        mArrayList = new ArrayList<>(); //Personal????????? ?????? array?????????(????????? ????????? ????????????)



    }
/*
    @Override
    protected void onResume() {//??????????????? ????????? ??????????????? ???????????? ???
        super.onResume();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")); //iBeacon??? layout
        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                        String uuid = beacon.getId1().toString(); //beacon uuid
                        int major = beacon.getId2().toInt(); //beacon major
                        int minor = beacon.getId3().toInt();// beacon minor
                        String address = beacon.getBluetoothAddress();

                        if (((Beacon) beacons.iterator().next()).getDistance() < 0.5 && minor == 55155) {// minor??? 55177??? ?????? ??????
                            Log.i(TAG, "The first beacon I see is about " + minor + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            Log.i(TAG, "The first beacon I see is about " + ((Beacon) beacons.iterator().next()).getDistance() + " meters away.");
                            Intent intent = new Intent(ShopActivity2.this, PayActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.i(TAG, "The no beacon I see is about " + ((Beacon) beacons.iterator().next()).getDistance() + " meters away.");
                        }

                    }

                }
            }

        };
        beaconManager.addRangeNotifier(rangeNotifier);
        //beaconManager.startRangingBeacons(BeaconReferenceApplication.wildcardRegion);
        beaconManager.startRangingBeacons(new Region("AC:23:3F:7E:09:8C", null, null, null));
    }

*/
    void showResult2(String Data1, String Data2) {//?????????????????? ?????? ?????? ??????
        GetData task2 = new GetData();
        task2.execute(Data1, Data2);
    }

    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ShopActivity2.this,
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
            //Integer searchKeyword3 = parserInt(searchKeyword2);

            //String searchKeyword = "snack";

            String serverURL = "http://3.37.3.112/Load_userbasket.php";
            String postParameters = "&userID=" + searchKeyword1 +"&order_id=" + searchKeyword2;



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
            String totalprice;
            Integer a=0;
            JSONObject jsonObject = new JSONObject(mJsonString); //json?????? ?????????
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON); //json????????? result array ?????????

            for (int i = 0; i < jsonArray.length(); i++) {

                //??? ??????????????? ?????????-> ????????? ??????????????? ??????????????? ???????????? ??????
                JSONObject item = jsonArray.getJSONObject(i);

                String productName = item.getString(TAG_productNAME);
                String productPrice = item.getString(TAG_productPrice);
                String classNum = item.getString(TAG_classNum);

                a += Integer.parseInt(productPrice,10)*Integer.parseInt(classNum,10);

                //??????????????? Personal????????? ???????????? ?????????
                PersonalData personalData = new PersonalData();

                personalData.setproductName(productName);
                personalData.setproductPrice(productPrice);
                personalData.setclassNum(classNum);

                mArrayList.add(personalData); //?????? ??????????????? ?????????????????? ?????? ????????????????????? ?????? ??????
            }
            mAdapter = new ShopAdapter(this, mArrayList);
            mRecyclerView.setAdapter(mAdapter);
            totalprice=String.valueOf(a);
            SharedPreference.setTotalPrice(ShopActivity2.this, totalprice);
            text_totalprice.setText(totalprice);

        } catch (JSONException /*| MalformedURLException*/ e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

}
