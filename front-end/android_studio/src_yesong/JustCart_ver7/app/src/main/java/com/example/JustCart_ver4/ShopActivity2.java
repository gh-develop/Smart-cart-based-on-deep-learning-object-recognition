package com.example.JustCart_ver4;

import android.content.Intent;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pedro.library.AutoPermissions;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
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

public class ShopActivity2 extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ImageButton btn_shop, btn_home, btn_mypage, btn_pay;
    //private BeaconManager beaconManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String TAG2 = "ShopActivity";
    public static Activity shopActivity;
    private List<Beacon> beaconList = new ArrayList<>();
    private String USER_ID, ORDER_ID;

    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    protected static final String TAG_beacon = "RangingActivity";

    private static String TAG = "phpquerytest";

    private static final String TAG_JSON="webnautes";
    private static final String TAG_productNAME = "productName";
    private static final String TAG_productPrice = "productPrice";
    private static final String TAG_classNum ="classNum";
    //private BeaconManager beaconManager;


    private TextView mTextViewResult, text_totalprice;
    private ArrayList<PersonalData> mArrayList;
    EditText mEditTextSearchKeyword;
    private ShopAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private String mJsonString;
    private String totalprice;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //액티비티 생성할 때
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        shopActivity = ShopActivity2.this;

        /*
        new Handler().postDelayed(new Runnable()
        {

            @Override
            public void run()
            {

                ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> info = manager.getRunningTasks(1);
                if(info.get(0).topActivity.getClassName().equals("com.example.JustCart_ver4.ShopActivity2")){
                    //딜레이 후 시작할 코드 작성
                    Intent intent = new Intent(getApplicationContext(),ShopActivity2.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //getWindow().setWindowAnimations(0);
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                    finish();

                }
            }
        }, 5000);// 5초 정도 딜레이를 준 후 시작

         */
        //비콘인식
        //AutoPermissions.Companion.loadAllPermissions(this,101); // AutoPermissions

        //beaconManager = BeaconManager.getInstanceForApplication(this);
        //BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);

        //beaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);
        //RunningAverageRssiFilter.setSampleExpirationMilliseconds(3000l);
        //RangedBeacon.setSampleExpirationMilliseconds(3000l);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")); //iBeacon의 layout
        Log.i("foxy", "ScanBT - i am in onCreateeeeeeeee");

        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")); //iBeacon의 layout
/*
        btn_pay = findViewById(R.id.btn_pay);

        //결제하기 버튼을 클릭 시 수행
        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopActivity2.this, CardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

*/
        // If you don't want to stop scanning every other minute, use code like this, which immediately starts a new scan cycle after the last one ends
        //beaconManager.setForegroundScanPeriod(1100);
        //beaconManager.setForegroundBetweenScanPeriod(0);

        //업데이트
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

                        if (((Beacon) beacons.iterator().next()).getDistance() < 0.5 && minor == 55155) {// minor가 55177인 비콘 인식
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
        //뒤로가기 버튼 눌렀을 때
        @Override
        public void onBackPressed() {
            super.onBackPressed();
            //stopPlay(); //이 액티비티에서 종료되어야 하는 활동 종료시켜주는 함수
            Toast.makeText(ShopActivity2.this, "방송 시청이 종료되었습니다.", Toast.LENGTH_SHORT).show();   //토스트 메시지
            Intent intent = new Intent(ShopActivity2.this, ShopActivity2.class); //지금 액티비티에서 다른 액티비티로 이동하는 인텐트 설정
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
            startActivity(intent);  //인텐트 이동
            finish();   //현재 액티비티 종료
        }


 */
        View view =findViewById(R.id.view1);
        view.setClickable(true);
        /*
        try {
            //TODO 액티비티 화면 재갱신 시키는 코드
            Intent intent = getIntent();
            finish(); //현재 액티비티 종료 실시
            overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
            startActivity(intent); //현재 액티비티 재실행 실시
            overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
        }
        catch (Exception e){
            e.printStackTrace();
        }

         */

        USER_ID = SharedPreference.getUserID(ShopActivity2.this);
        ORDER_ID = SharedPreference.getOrderID(ShopActivity2.this);

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        //드래그로 화면전환(체크리스트 화면)
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float distance = 0;
                float pressedX = 0;
                float pressedX2 = 0;

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 손가락을 touch 했을 떄 x 좌표값 저장
                    pressedX = event.getX();
                    Log.d("viewTest", "newXvalue : " + pressedX);    // View 내부에서 터치한 지점의 상대 좌표값.
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    pressedX2 = event.getX();
                    distance = pressedX - pressedX2;  // View 내부에서 터치한 지점의 상대 좌표값.
                    Log.d("viewTest", "Distance : "+ distance);
                }


                // 해당 거리가 100이 되지 않으면 이벤트 처리 하지 않는다.
                if (Math.abs(distance) < 200) {
                    return false;
                }

                if (distance > 0) {
                    // 손가락을 왼쪽으로 움직였으면 오른쪽 화면이 나타나야 한다.
                    Intent intent = new Intent(ShopActivity2.this, CheckActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                } else {
                    // 손가락을 오른쪽으로 움직였으면 왼쪽 화면이 나타나야 한다.
                    Intent intent = new Intent(ShopActivity2.this, CheckActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in, R.anim.right_out);
                }
                finish(); // finish 해주지 않으면 activity가 계속 쌓인다.

                return true;
            }
        });

        btn_shop = findViewById(R.id.btn_shop2);
        btn_home = findViewById(R.id.btn_home2);
        btn_mypage = findViewById(R.id.btn_mypage2);

        //장바구니 버튼을 클릭 시 수행
        btn_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopActivity2.this, ShopActivity2.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        //홈 버튼을 클릭 시 수행
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopActivity2.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        //마이페이지 버튼을 클릭 시 수행
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

        mArrayList = new ArrayList<>(); //Personal객체를 담을 array리스트(어댑터 쪽으로 날릴거임)
    }

    // 당겨서 새로고침 했을 때 뷰 변경 메서드
    public void updateLayoutView() {
        mArrayList.clear();
        showResult2(USER_ID, ORDER_ID);
        text_totalprice.setText(totalprice);
    }

    //비콘인식
    @Override
    protected void onResume() {
        //Log.i(TAG, "I'm in onResume() "+" @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        super.onResume();
        beaconManager.addRangeNotifier (new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    for (Beacon beacon : beacons) {
                        int minor = beacon.getId3().toInt();// beacon minor
                        if (((Beacon) beacons.iterator().next()).getDistance() < 0.3 && minor == 55155) {// minor가 55177인 비콘 인식
                            Log.i(TAG_beacon, "The first beacon I see is about " + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            Log.i(TAG_beacon, "The first beacon I see is about " + ((Beacon) beacons.iterator().next()).getDistance() + " meters away."+minor);
                            Intent intent = new Intent(ShopActivity2.this, PayActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            /*
                            try{
                                Thread.sleep(3000);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }

                             */
                            //test.setText("비콘연결완료");
                        } else {
                            Log.i(TAG_beacon, "The no beacon I see is about@@@@@@@@@@@@@ " + ((Beacon) beacons.iterator().next()).getDistance() + " meters away."+ minor);
                            //test.setText("아직!!!!");
                        }
                        //Log.i(TAG, "The first beacon I see is about " + ((Beacon) beacons.iterator().next()).getDistance() + " meters away.");
                        //Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
                        //Beacon firstBeacon = beacons.iterator().next();

                        //logToDisplay("The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                        //logToDisplay(firstBeacon.getDistance() + " \n ");
                    }
                }
            }

        });
        //Region region = new Region("myRangingUniqueId", uUID, major, minor);
        //Region region1 = new Region("myIdentifier1", Identifier.parse("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6"), Identifier.parse("1"), Identifier.parse("1"));
        //beaconManager.addRangeNotifier(rangeNotifier);
        beaconManager.startRangingBeacons(new Region("CardBeacon", Identifier.parse("E2C56DB5-DFFB-48D2-B060-D0F5A71096E0"), Identifier.parse("40010"), Identifier.parse("55155")));
    }

    @Override
    //여기 주석 살리면 액티비티 옮겨갈 때 비콘인식 그만함
    protected void onPause() {
        super.onPause();
        //beaconManager.stopRangingBeacons(BeaconReferenceApplication.wildcardRegion);
        //beaconManager.removeAllRangeNotifiers();
    }
/*
    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                //EditText editText = (EditText)CardActivity.this.findViewById(R.id.rangingText1);
                //editText.append(line+"\n");
            }
        });
    }

 */
/*
    //뒤로가기 버튼 눌렀을 때
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ShopActivity2.this, ShopActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

 */
/*
    @Override
    protected void onResume() {//액티비티가 화면에 나타나있고 실행중일 때
        super.onResume();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")); //iBeacon의 layout
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

                        if (((Beacon) beacons.iterator().next()).getDistance() < 0.5 && minor == 55155) {// minor가 55177인 비콘 인식
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
    void showResult2(String Data1, String Data2) {//메인화면에서 검색 결과 수행
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
        protected void onPostExecute(String result) { //에러있는 경우 에러메세지 보여줌/ 아니면 JSON파싱해서 화면에 보여줌
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
        protected String doInBackground(String... params) { //php파일 실행

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

    @Override
    public void onRefresh() {
        //새로 고침 코드
        updateLayoutView();

        //새로 고침 완
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showResult() { //DB에서 상세정보 다 가져옴

        try {
            Integer a=0;
            JSONObject jsonObject = new JSONObject(mJsonString); //json파일 받아옴
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON); //json파일의 result array 가져옴

            for (int i = 0; i < jsonArray.length(); i++) {

                //한 딕셔너리를 가져옴-> 하나의 오브젝트에 상품하나의 상세정보 저장
                JSONObject item = jsonArray.getJSONObject(i);

                String productName = item.getString(TAG_productNAME);
                String productPrice = item.getString(TAG_productPrice);
                String classNum = item.getString(TAG_classNum);

                a += Integer.parseInt(productPrice,10)*Integer.parseInt(classNum,10);

                //만들어뒀던 Personal객체에 데이터를 담는다
                PersonalData personalData = new PersonalData();

                personalData.setproductName(productName);
                personalData.setproductPrice(productPrice);
                personalData.setclassNum(classNum);

                mArrayList.add(personalData); //담은 데이터들을 배열리스트에 넣고 리사이클러뷰로 보낼 준비
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
