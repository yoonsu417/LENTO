package com.example.lento;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class JoinActivity extends Activity {

    // DB 헬퍼, 커서 선언
    SQLiteHelper helper;
    SQLiteDatabase db;
    Cursor emailCheckCS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // DB 테이블 생성 + read
        helper = new SQLiteHelper(this);
        db = helper.getReadableDatabase();

        // 선언 및 연결
        EditText name = (EditText)findViewById(R.id.CreateName2);
        EditText email = (EditText)findViewById(R.id.CreateId2);
        EditText pw = (EditText)findViewById(R.id.CreatePw2);
        Button joinBt = (Button)findViewById(R.id.JoinBt);
        // 버튼 연동
        joinBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String set_name = name.getText().toString();
                String set_email = email.getText().toString();
                String set_pw = pw.getText().toString();
                // 1. name, email, pw 중 공백 존재 : 경고 메시지
                if(set_name.isEmpty() || set_email.isEmpty() || set_pw.isEmpty() ) {
                    Toast.makeText(getApplicationContext(), "공백 없이 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    // 커서 -> 중복 이메일 가입 방지
                    emailCheckCS = db.rawQuery("SELECT EMAIL FROM USER " +
                            "WHERE EMAIL = '" + set_email + "';", null);
                    // 2. 이메일 중복 : 경고 메시지
                    if(emailCheckCS.getCount() != 0) {
                        Toast.makeText(getApplicationContext(), "이미 가입한 이메일입니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 3. 신규 회원 : 회원 정보 생성 + 화면 전환
                        // DB write 전환, INSERT 쿼리
                        db = helper.getWritableDatabase();
                        db.execSQL("INSERT INTO USER VALUES ('" +
                                set_email + "', '" + set_name +"', '" + set_pw+ "' , 0);");
                        emailCheckCS.close();
                        db.close();
                        // 완료 토스트 메시지
                        Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        // 화면 전환
                        Intent intent = new Intent(JoinActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });

        /*
        backBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(JoinActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

         */

        // 이미지 버튼 구현 미완으로 오류가 나서 우선 테스트 버튼(R.id.back_test)으로 대체했습니다. 추후 수정 부탁드려요! (_ _)


        // 뒤로가기 클릭, 회원가입 -> 로그인 화면 이동
        ImageView back;
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });

    }

}
