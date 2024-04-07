package com.example.lento;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Onboard3Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard3);


        Button nextButton = findViewById(R.id.NextBt);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그인 하기 버튼 클릭시 로그인 화면으로 이동
                Intent intent = new Intent(Onboard3Activity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
