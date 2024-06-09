package com.example.lento;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 스플래시 화면을 몇 초간 보여주고 메인 액티비티로 이동
        int SPLASH_DISPLAY_LENGTH = 2000; // 2초

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 메인 액티비티로 이동
                Intent mainIntent = new Intent(SplashActivity.this, Onboard1Activity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}