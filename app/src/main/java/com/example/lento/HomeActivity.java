package com.example.lento;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;


public class HomeActivity extends Activity {

    public static final int PICK_FILE = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 악보이미지 누르면 pdf 선택 화면으로 이동
        Button Upload;

        Upload = (Button)findViewById(R.id.Upload);

        // 악보인식 테스트용 버튼
        Button test;
        test = (Button)findViewById(R.id.OpenCVtest);

        test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,OpenCVtestActivity.class);
                startActivity(intent);
            }
        });


        Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf"); // pdf 파일만 선택 설정
                startActivityForResult(intent, PICK_FILE);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FILE && resultCode == RESULT_OK) {
            if(data != null){
                Uri uri = data.getData();
                Intent intent = new Intent(HomeActivity.this, UploadActivity.class);
                intent.putExtra("fileUri", uri.toString());
                startActivity(intent);
            }
        }
    }
    // 홈 클릭 시 실행되는 메서드
    public void onSheetClicked(View view) {
        // 홈 화면으로 이동하는 코드를 작성합니다.
        Intent intent = new Intent(this, ScorelistActivity.class);
        startActivity(intent);
    }
}

