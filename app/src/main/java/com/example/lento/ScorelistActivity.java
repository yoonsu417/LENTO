package com.example.lento;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ScorelistActivity extends AppCompatActivity {
    Spinner spinner;
    SQLiteDatabase db;
    TextView textViewSheetMusicCount;
    private RecyclerView recyclerView;
    private ScoreAdapter adapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorelist);

        // 유저 확인
        CurrentUser Currentuser = (CurrentUser)getApplicationContext();
        String user = Currentuser.getCurrentUser();

        //스피너
        ArrayAdapter<CharSequence> adapter =ArrayAdapter.createFromResource(this, R.array.score_array, android.R.layout.simple_dropdown_item_1line);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinner=(Spinner)findViewById(R.id.SearchSort);
        spinner.setAdapter(adapter);

        // 악보 개수
        textViewSheetMusicCount = findViewById(R.id.textViewSheetMusicCount);
        // SQLiteHelper 객체 생성 및 데이터베이스 연결
        SQLiteHelper helper = new SQLiteHelper(this);
        db = helper.getReadableDatabase();
        // 악보 개수 가져와서 표시
        int sheetMusicCount = getSheetMusicCount(user);
        textViewSheetMusicCount.setText(String.valueOf(sheetMusicCount));

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerGridView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // 악보 데이터 가져와서 RecyclerView에 설정
        setupRecyclerView(user);


    }
    public void onClick(View v){
    }
    private int getSheetMusicCount(String user) {
        // 유저 확인
        /*
        CurrentUser Currentuser = (CurrentUser)getApplicationContext();
        user = Currentuser.getCurrentUser();
         */

        String countQuery = "SELECT COUNT(*) FROM SHEET WHERE UPLOAD_USER = '" + user + "';";
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    private void setupRecyclerView(String user) {
        // SQLiteHelper 객체 생성
        DBManager dbManager = new DBManager(this);
        //SQLiteHelper dbHelper = new SQLiteHelper(this);

        // 악보 데이터 가져오기
        List<Score> scoreList = dbManager.getScores(user);
        //List<Score> scoreList = dbHelper.getScores();

        // Adapter 설정
        adapter = new ScoreAdapter(this, scoreList);
        recyclerView.setAdapter(adapter);
    }

    // 홈 클릭 시 실행되는 메서드
    public void onHomeClicked(View view) {
        // 홈 화면으로 이동하는 코드를 작성합니다.
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
