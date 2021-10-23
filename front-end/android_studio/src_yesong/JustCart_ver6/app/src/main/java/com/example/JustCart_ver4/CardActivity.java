package com.example.JustCart_ver4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CardActivity extends AppCompatActivity {
    private List<Beacon> beaconList = new ArrayList<>();
    private BeaconManager beaconManager;
    private static String TAG = "phpquerytest";

    @Override
    protected void onCreate(Bundle savedInstanceState) { //액티비티 생성할 때
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")); //iBeacon의 layout

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
                            Intent intent = new Intent(CardActivity.this, PayActivity.class);
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


    }

    //뒤로가기 버튼 눌렀을 때
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CardActivity.this, ShopActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
