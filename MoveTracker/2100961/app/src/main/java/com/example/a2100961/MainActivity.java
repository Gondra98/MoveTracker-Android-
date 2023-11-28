package com.example.a2100961;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap gMap;
    SupportMapFragment mapFragment;
    LocationManager locationManager;
    PolylineOptions polylineOptions;
    ArrayList<LatLng> arrayList;
    Button btnZoomIn, btnZoomOut, btnMapType, btnStart, btnNext;
    TextView textView, textView2;
    Integer mapType = GoogleMap.MAP_TYPE_SATELLITE;
    static myDBHelper myHelper;
    static SQLiteDatabase sqlDB;
    static String sql;

    double currentLat, currentLog, currentTime;
    double startLat, startLog, startTime;
    double lastLat, lastLog, lastTime;

    String formattedStartTime, formattedEndTime;

    boolean sw = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        myHelper = new myDBHelper(this); // DBHelper 객체를 생성하고 초기화

        // 현재 시간을 "yyyy-MM-dd HH:mm" 형태로 포맷팅
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREAN);




        // 위치 권한 요청 결과 처리를 위한 ActivityResultLauncher 설정
        ActivityResultLauncher<String[]> permissionResult = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.getOrDefault(
                            android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(
                            android.Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    if (fineLocationGranted != null && fineLocationGranted) {
                        Toast.makeText(getApplicationContext(), "자세한 위치 권한이 허용됨",
                                Toast.LENGTH_SHORT).show();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        Toast.makeText(getApplicationContext(), "대략적인 위치 권한이 허용됨",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "위치 권한이 허용되지 않음",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // 위치 권한이 없는 경우 권한 요청
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            permissionResult.launch(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        // UI 요소 초기화
        btnZoomIn = (Button) findViewById(R.id.button);
        btnZoomOut = (Button) findViewById(R.id.button2);
        btnMapType = (Button) findViewById(R.id.button3);
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        btnStart = (Button) findViewById(R.id.button4);
        btnNext = (Button) findViewById(R.id.btnNext);

        // 위치 정보 및 지도 초기화
        arrayList = new ArrayList<LatLng>();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 위치 업데이트 요청
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 1, locationListener);



        // 버튼 동작 설정
        btnZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gMap.moveCamera(CameraUpdateFactory.zoomIn());
            }
        });

        btnZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gMap.moveCamera(CameraUpdateFactory.zoomOut());
            }
        });

        btnMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 지도 유형 변경
                if (mapType == GoogleMap.MAP_TYPE_SATELLITE) {
                    mapType = GoogleMap.MAP_TYPE_NORMAL;
                    gMap.setMapType(mapType);
                    btnMapType.setText("위성");
                }
                else if (mapType == GoogleMap.MAP_TYPE_NORMAL) {
                    mapType = GoogleMap.MAP_TYPE_SATELLITE;
                    gMap.setMapType(mapType);
                    btnMapType.setText("일반");
                }
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 시작/종료 버튼 동작
                try {
                    if (sw) {

                        // 현재 위치 저장
                        lastLat = currentLat;
                        lastLog = currentLog;
                        lastTime = currentTime;
                        formattedEndTime = dateFormat.format(new Date());



                        sw = false;
                        btnStart.setText("시작");

                        double distDiff = distance(startLat, startLog, lastLat, lastLog);
                        double timeDiff = (lastTime - startTime) / 1000;
                        double avgSpeed = distDiff / timeDiff;

                        sqlDB = myHelper.getWritableDatabase();
                        String sql = "insert into movetable (start, end, distance) values ('" +
                                "위도 : " + startLat  +
                                "\n경도 : " + startLog +
                                "\n시간 : "+ formattedStartTime+
                                "', '" +
                                "위도 : " + lastLat  +
                                "\n경도 : " + lastLog +
                                "\n시간 : "+ formattedEndTime +
                                "', '" +
                                "이동 거리(m) : " + distDiff +
                                "\n이동 시간(s) : "+ timeDiff +
                                "\n평균 속도(m/s) :" + avgSpeed + "')";
                        Log.d("SQL", sql);
                        sqlDB.execSQL(sql);
                        sqlDB.close();
                    }
                    else {
                        startLat = currentLat;
                        startLog = currentLog;
                        startTime = currentTime;
                        formattedStartTime = dateFormat.format(new Date());
                        sw = true;
                        arrayList.clear();
                        gMap.clear();
                        btnStart.setText("종료");

                    }
                }  catch (Exception e) {
                    Log.e("ButtonClickError", "버튼 클릭 동작 중 오류 발생", e);
                }

            }
        });





        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 현재 액티비티에서 다음 액티비티로 이동하는 코드
                Intent intent = new Intent(MainActivity.this, ListView.class);
                startActivity(intent);

            }
        });


    }

    // 위치 변경 리스너
    final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            String provider = location.getProvider();
            currentLat = location.getLatitude();
            currentLog = location.getLongitude();
            currentTime = System.currentTimeMillis();
            LatLng latLng = new LatLng(currentLat, currentLog);
            textView.setText("Lat : "+currentLat+", Log : "+ currentLog);
            gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // 이동 중일 때 경로 그리기
            if (sw) {
                double distDiff = distance(lastLat, lastLog, currentLat, currentLog);
                double timeDiff = (currentTime - lastTime) / 1000;
                double speedMs = distDiff / timeDiff;
                if (speedMs < 3) {
                    textView2.setText("속도 : "+String.format("%.1f", speedMs)+"m/s");
                }
                else {
                    Integer speedKMH = (int)(speedMs * 3.6);
                    textView2.setText("속도 : "+speedKMH+"Km/h");
                }

                // 지도에 경로 그리기
                polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.RED);
                polylineOptions.width(5);
                arrayList.add(latLng);
                polylineOptions.addAll(arrayList);
                gMap.addPolyline(polylineOptions);
            }


        }
    };

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // 지도 초기화 및 설정
        gMap = googleMap;
        gMap.setMapType(mapType);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(35.945, 126.683),
                16));
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                // 지도를 터치했을 때 경로 그리기
                polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.RED);
                polylineOptions.width(5);
                arrayList.add(latLng);
                polylineOptions.addAll(arrayList);
                gMap.addPolyline(polylineOptions);
            }
        });
    }

    // 두 지점 간의 거리 계산
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1609.344;

        return (dist);
    }

    // 도(degree)를 라디안(radian)으로 변환
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // 라디안을 도(degree)로 변환
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }





    public class myDBHelper extends SQLiteOpenHelper {

        public myDBHelper(Context context) {
            super(context, "moveDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sql = "create table movetable (" +
                    "no integer primary key ," +
                    "start char(50), end char(50), distance REAL)";
            Log.d("SQL", sql);
            sqLiteDatabase.execSQL(sql);

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            String sql = "drop table if exists movetable";
            Log.d("SQL", sql);
            sqLiteDatabase.execSQL(sql);
            onCreate(sqLiteDatabase);
        }
    }


}




