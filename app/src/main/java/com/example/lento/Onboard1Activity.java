package com.example.lento;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.android.OpenCVLoader;

public class Onboard1Activity extends Activity {
    private static final String TAG = "TEST_OPEN_CV_ANDROID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard1);

        // OpenCV 라이브러리 초기화
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV 초기화 실패!");
            return;
        } else {
            Log.d(TAG, "OpenCV 초기화 성공!!!!!");
        }

        Button nextButton = findViewById(R.id.NextBt);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다음으로 버튼 클릭시 온보딩2로 이동
                Intent intent = new Intent(Onboard1Activity.this,Onboard2Activity.class);
                startActivity(intent);
            }
        });
    }
}
