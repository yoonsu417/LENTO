package com.example.lento;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText InputEmail, InputPw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //이메일, 비밀번호 입력
        InputEmail = (EditText) findViewById(R.id.InputEmail);
        InputPw = (EditText) findViewById(R.id.InputPw);


        // 계정이 없으신가요? 눌렀을 때 회원가입 화면으로 이동
        TextView NoAccount;
        NoAccount = (TextView) findViewById(R.id.account);
        NoAccount.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                setContentView(R.layout.activity_join);
            }
        });


    }
}