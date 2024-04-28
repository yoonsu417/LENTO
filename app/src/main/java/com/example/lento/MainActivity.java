package com.example.lento;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;



public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TEST_OPEN_CV_ANDROID";

    // 위젯 선언
    TextView NoAccount;
    EditText email, pw;
    Button loginBt;
    // DB 헬퍼, 커서 선언
    SQLiteHelper helper;
    SQLiteDatabase db;
    Cursor loginCheckCS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // DB 테이블 연결 + read
        helper = new SQLiteHelper(this);
        db = helper.getReadableDatabase();

        // OpenCV 초기화 코드인데 다른 자바 파일에 중복으로 있는 것 같아서 우선 주석 처리 했습니다.
        /*

        // OpenCV 라이브러리 초기화
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV 초기화 실패!");
            return;
        } else {
            Log.d(TAG, "OpenCV 초기화 성공!!!!!");
        }

         */

        // 연결
        email = (EditText) findViewById(R.id.InputEmail);
        pw = (EditText) findViewById(R.id.InputPw);
        loginBt = (Button)findViewById(R.id.loginBt);
        NoAccount = (TextView) findViewById(R.id.account);

        // 계정이 없으신가요? 눌렀을 때 회원가입 화면으로 이동
        NoAccount.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        });

        //로그인 버튼
        loginBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String input_email = email.getText().toString();
                String input_pw = pw.getText().toString();
                // 1. name, pw 중 공백 존재 : 경고 메시지
                if(input_email.isEmpty() || input_pw.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "공백 없이 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    // 커서 설정
                    loginCheckCS = db.rawQuery("SELECT * FROM USER " +
                            "WHERE EMAIL = '" + input_email + "' AND PW ='" + input_pw + "';", null);
                    loginCheckCS.moveToFirst();
                    // 2. 이메일-비밀번호 불일치 : 오류 메시지
                    if(loginCheckCS.getCount() == 0) {
                        Toast.makeText(getApplicationContext(), "이메일 혹은 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show();
                    } else {
                        // 3. 이메일-비밀번호 일치 : 로그인 + 화면 전환
                        loginCheckCS.close();
                        db.close();

                        // 로그인한 유저 전역변수 설정
                        CurrentUser user = (CurrentUser)getApplicationContext();
                        user.setCurrentUser(input_email);
                        // 완료 토스트 메시지
                        Toast.makeText(getApplicationContext(), "로그인되었습니다.", Toast.LENGTH_SHORT).show();
                        // 화면 전환
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });




        // 이전 로그인 버튼
        /*
        loginBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
        */

    }
}