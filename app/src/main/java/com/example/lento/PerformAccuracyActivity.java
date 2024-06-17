package com.example.lento;

import android.content.Intent;
import android.content.SharedPreferences;
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
    TextView date;
    ImageView back;

    Button goHome;
    Button again;
    public static final String SHARED_PREF_NAME = "practicePrefs";
    public static final String KEY_IMAGE_PATH = "imagePath";
    public static final String KEY_PRACTICE_DATE = "practiceDate";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performaccuracy);

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

        // 이미지 경로, 연습한 날짜 home으로 보내기
        String imagePath = getIntent().getStringExtra("imagePath");
        String practicedate = formatDate;

        // SharedPreferences에 데이터 저장
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_IMAGE_PATH, imagePath);
        editor.putString(KEY_PRACTICE_DATE, practicedate);
        editor.apply();

        goHome = (Button)findViewById(R.id.homeButton);
        goHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(PerformAccuracyActivity.this, HomeActivity.class);
                intent.putExtra("imagePath", imagePath);
                intent.putExtra("practiceDate", practicedate);
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