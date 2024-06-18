package com.example.lento;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PerformAccuracyActivity extends AppCompatActivity {

    private SQLiteHelper dbHelper;
    TextView date;
    ImageView back;
    SQLiteDatabase db;
    Button goHome;
    Button again;
    public static final String SHARED_PREF_NAME = "practicePrefs";
    public static final String KEY_IMAGE_PATH = "imagePath";
    public static final String KEY_PRACTICE_DATE = "practiceDate";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performaccuracy);

        dbHelper = new SQLiteHelper(this);

        date = (TextView)findViewById(R.id.accuracyDate);
        back = (ImageView)findViewById(R.id.back);


        OpenCVtestActivity openCVtestActivity = new OpenCVtestActivity();
        // 지금은 고향의 봄 악보로 고정
        openCVtestActivity.processImage(this, R.drawable.sheet);

        List<int[]> beatPitch = openCVtestActivity.getBeatPitchStat();

        System.out.println("PerfromAccuracyActivity: 박자, 계이름, 객체 위치 출력");
        if (beatPitch != null) {
            for (int[] list : beatPitch) {
                System.out.println(Arrays.toString(list));
            }
        } else {
            Log.e("PerformAccuracyActivity", "beatPitch 리스트가 null입니다.");
        }

        // 오늘 날짜 출력
        Date currentDate = new Date();
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formatDate = mFormat.format(currentDate);

        date.setText(formatDate);

        // 페이지 뒤로 가기
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 이미지 경로 및 연습한 날짜 저장
        String imagePath = getIntent().getStringExtra("imagePath");
        String practicedate = formatDate;

        RecentPractice recentPractice = new RecentPractice(this);
        recentPractice.saveDB(imagePath,practicedate);

        goHome = (Button)findViewById(R.id.homeButton);
        goHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(PerformAccuracyActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
        again = (Button)findViewById(R.id.again);
        goHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(PerformAccuracyActivity.this, PlayActivity.class);
                startActivity(intent);
            }
        });

    }

}