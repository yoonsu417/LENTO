package com.example.lento;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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


    }
}