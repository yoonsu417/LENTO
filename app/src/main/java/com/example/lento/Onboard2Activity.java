package com.example.lento;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Onboard2Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard2);

        Button nextButton = findViewById(R.id.NextBt);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다음으로 버튼 클릭시 온보딩2로 이동
                Intent intent = new Intent(Onboard2Activity.this,Onboard3Activity.class);
                startActivity(intent);
            }
        });
    }
}
