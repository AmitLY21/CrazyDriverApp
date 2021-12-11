package com.example.crazydriver.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.crazydriver.MSP;
import com.example.crazydriver.R;
import com.example.crazydriver.callbacks.CallBack_List;
import com.example.crazydriver.callbacks.CallBack_Map;
import com.example.crazydriver.fragments.Fragment_List;
import com.example.crazydriver.fragments.Fragment_Map;
import com.example.crazydriver.objects.MyDB;
import com.example.crazydriver.objects.Record;
import com.example.crazydriver.objects.RecordComparator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

@RequiresApi(api = Build.VERSION_CODES.O)
public class GameOverActivity extends AppCompatActivity {

    private Button btnExit;
    private Button btnRerun;

    private Fragment_List fragmentList;
    private Fragment_Map fragmentMap;
    private String score;

    private MyDB myDB;
    double[] latLon = new double[2];

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over_activity);

        findViews();
        initFragments();

        //load
        loadFromSP();
        if (myDB == null) {
            generateDefaultRecords();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            score = extras.getString("finalScore");
            latLon[0] = extras.getDouble("lat");
            latLon[1] = extras.getDouble("lon");
            Toast.makeText(this, "Your Final Score is: " + score, Toast.LENGTH_SHORT).show();
            myDB.getRecords().add(getNewRecord());
        }

        //save
        saveToSP();

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
        btnRerun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(GameOverActivity.this, StartGameActivity.class));
            }
        });
    }

    private void generateDefaultRecords() {
        ArrayList<Record> defaultRec = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            defaultRec.add(new Record().setTime("99:9" + i).setScore(-1).setLon(0).setLon(0));
        }
        myDB = new MyDB();
        myDB.setRecords(defaultRec);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Record getNewRecord() {
        Record r = new Record();
        r.setScore(Integer.parseInt(score))
                .setTime(String.valueOf(LocalTime.now().format(dtf)))
                .setLat(latLon[0])
                .setLon(latLon[1]);

        return r;
    }

    //-----------------------SP--------------------
    private void saveToSP() {
        Collections.sort(myDB.getRecords(), new RecordComparator());
        myDB = pickTop10FromDB();
        String json = new Gson().toJson(myDB);
        MSP.getMe().putString("MY_DB", json);
    }

    private void loadFromSP() {
        String js = MSP.getMe().getString("MY_DB", "");
        myDB = new Gson().fromJson(js, MyDB.class);
    }

    private MyDB pickTop10FromDB() {
        ArrayList<Record> res = new ArrayList<>();
        int counter = 0;
        while (res.size() < 10 && counter < myDB.getRecords().size()) {
            if (myDB.getRecords().get(counter).getScore() != -1) {
                res.add(myDB.getRecords().get(counter));
            }
            counter++;
        }
        return myDB.setRecords(res);
    }

    //-----------------------CallBacks--------------------
    CallBack_Map callBack_map = (lat, lon) -> {
        //Zoom to place
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(googleMap -> {
            LatLng latLng = new LatLng(lat, lon);
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Played Here!"));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15), 5000, null);
        });
    };

    CallBack_List callBackList = new CallBack_List() {

        @Override
        public ArrayList<Record> getRecords() {
            return myDB.getRecords();
        }

        @Override
        public void ZoomOnMap(double lat, double lon) {
            callBack_map.mapClicked(lat, lon);
        }

    };

    //-----------------------Views--------------------
    private void findViews() {
        btnExit = findViewById(R.id.panel_BTN_exit);
        btnRerun = findViewById(R.id.panel_BTN_rerun);
    }

    private void initFragments() {
        fragmentList = new Fragment_List();
        fragmentList.setActivity(this);
        fragmentList.setCallBackList(callBackList);
        getSupportFragmentManager().beginTransaction().add(R.id.frame_top10_list, fragmentList).commit();


        fragmentMap = new Fragment_Map();
        fragmentMap.setActivity(this);
        fragmentMap.setCallBack_map(callBack_map);
        getSupportFragmentManager().beginTransaction().add(R.id.frame_map, fragmentMap).commit();

    }

}
