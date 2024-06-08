package com.example.lento;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class MypageActivity extends AppCompatActivity {
    SQLiteHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // SQLiteHelper 초기화
        dbHelper = new SQLiteHelper(this);
        // 사용자 이름과 이메일 가져와서 설정
        TextView nameTextView = findViewById(R.id.name);
        TextView emailTextView = findViewById(R.id.email);

        // 사용자 이름과 이메일 가져오기
        String userEmail = getUserEmailFromDB(); // 사용자 이메일 가져오기
        String userName = getUserNameFromDB(userEmail); // 사용자 이름 가져오기

        // TextView에 설정
        emailTextView.setText(userEmail);
        nameTextView.setText(userName);

        //로그아웃 버튼 클릭시 로그인창으로 이동
        Button logoutBt = findViewById(R.id.logoutBt);
        logoutBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MypageActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
                builder.setView(dialogView);
                AlertDialog alertDialog = builder.create();

                TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
                Button btnCancel = dialogView.findViewById(R.id.dialog_cancel);
                Button btnConfirm = dialogView.findViewById(R.id.dialog_confirm);


                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 확인 버튼을 누르면 로그인 창으로 이동
                        Intent intent = new Intent(MypageActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // 현재 액티비티 종료
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();
            }
        });

        ViewPager viewPager = findViewById(R.id.tab_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        // ViewPager에 어댑터 설정
        TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        // TabLayout과 ViewPager 연결
        tabLayout.setupWithViewPager(viewPager);

    }
    // 사용자 이메일 가져오는 메서드
    private String getUserEmailFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String userEmail = "";

        // 사용자의 이메일을 가져오는 쿼리
        String query = "SELECT EMAIL FROM USER";
        Cursor cursor = db.rawQuery(query, null);

        // 쿼리 결과에서 첫 번째 행의 이메일 가져오기
        if (cursor.moveToFirst()) {
            userEmail = cursor.getString(0);
        }

        cursor.close();
        db.close();

        return userEmail;
    }

    // 사용자 이름 가져오는 메서드
    private String getUserNameFromDB(String userEmail) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String userName = "";

        // 사용자의 이메일을 기반으로 사용자 이름을 가져오는 쿼리
        String query = "SELECT NAME FROM USER WHERE EMAIL = '" + userEmail + "'";
        Cursor cursor = db.rawQuery(query, null);

        // 쿼리 결과에서 사용자 이름을 가져오기
        if (cursor.moveToFirst()) {
            userName = cursor.getString(0);
        }

        cursor.close();
        db.close();

        return userName;
    }
    public void onHomeClicked(View view) {
        // 홈 화면으로 이동하는 코드
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
    public void onSheetClicked(View view) {
        // 악보리스트 화면으로 이동하는 코드
        Intent intent = new Intent(this, ScorelistActivity.class);
        startActivity(intent);
    }
    public void onMypageClicked(View view) {
        // 마이페이지 화면으로 이동하는 코드
        Intent intent = new Intent(this, MypageActivity.class);
        startActivity(intent);
    }
    public void onTheoryClicked(View view) {
        // 음악이론 화면으로 이동하는 코드
        Intent intent = new Intent(this, TheoryActivity.class); //음악이론 액티비티 추가하면 바꿀것.
        startActivity(intent);
    }
}

