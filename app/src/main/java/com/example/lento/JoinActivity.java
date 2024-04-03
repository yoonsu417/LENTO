package com.example.lento;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class JoinActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // 뒤로가기 클릭, 회원가입 -> 로그인 화면 이동
        TextView back;
        back = (TextView)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });
    }

}
