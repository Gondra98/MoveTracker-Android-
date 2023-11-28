package com.example.a2100961;

import static com.example.a2100961.MainActivity.myHelper;
import static com.example.a2100961.MainActivity.sqlDB;
import static com.example.a2100961.MainActivity.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ListView extends AppCompatActivity {

    LocationManager locationManager;
    Location lastLocation;
    Geocoder geocoder;
    TextView textView3;
    DateFormat dateFormat;
    Button btnBack, btnreset;




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREAN);

        textView3 = findViewById(R.id.textView3);  // 현재 위치값

        btnBack = (Button) findViewById(R.id.button6) ;
        btnreset = (Button) findViewById(R.id.button7);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListViewAdapter adapter = new ListViewAdapter();

                android.widget.ListView listView = findViewById(R.id.listview);
                listView.setAdapter(adapter);

                // 데이터베이스 초기화
                try {
                    SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
                    String deleteSql = "DELETE FROM movetable";
                    sqlDB.execSQL(deleteSql);
                    sqlDB.close();




                } catch (Exception e) {
                    Log.e("ListViewActivity", "데이터베이스 초기화 중 오류 발생", e);
                }
            }
        });





        ListViewAdapter adapter = new ListViewAdapter();


        // 위치 변경 리스너
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                List<Address> address = null;
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                double altitude = location.getAltitude();

                textView3.setText("현재 상태" + "\n위도 : " + latitude +
                        "\n경도 : " + longitude + "\n고도 : " + altitude);
            }
        };

        // 현재 위치 업데이트 리스너 등록
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);




        // SQLite 데이터베이스 사용 예시
        try {
            SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
            String sql = "SELECT * FROM movetable";
            Log.d("SQL", sql);
            Cursor cursor = sqlDB.rawQuery(sql, null);

            int count = 1; // 번호를 나타내는 변수

            while (cursor.moveToNext()) {
                String start = cursor.getString(cursor.getColumnIndex("start"));
                String end = cursor.getString(cursor.getColumnIndex("end"));
                String distance = cursor.getString(cursor.getColumnIndex("distance"));

                // 어댑터에 아이템 추가
                adapter.addItem(start, end, distance + " (번호: " + count++ + ")");
            }

            cursor.close();
            sqlDB.close();

            // ListView에 어댑터 설정
            android.widget.ListView listView = findViewById(R.id.listview);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            // 예외 처리를 여기에 추가
            Log.e("ListViewActivity", "데이터베이스 작업 중 오류 발생", e);
        }
    }



}
