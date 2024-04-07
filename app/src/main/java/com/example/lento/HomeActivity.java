package com.example.lento;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;


public class HomeActivity extends Activity {

    public static final int YOUR_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 악보이미지 누르면 악보 추가
        //로그인 누르면 홈 화면으로 이동
        Button Upload;

        Upload = (Button)findViewById(R.id.Upload);
        Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*"); // 모든 파일 유형을 선택할 수 있도록 설정
                startActivityForResult(intent, YOUR_REQUEST_CODE);
            }

        });

    }


}

