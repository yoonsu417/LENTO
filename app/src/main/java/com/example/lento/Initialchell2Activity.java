package com.example.lento;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Initialchell2Activity extends Activity {

    // DB 관련
    SQLiteHelper helper;
    SQLiteDatabase db;
    //카테고리 버튼 선택 관련
    String category = ""; // 선택한 버튼에 따라 카테고리 설정
    private boolean isFinishGoalSelected = false; // 완곡목표 버튼이 선택되었는지 여부
    private boolean isPracticeGoalSelected = false; // 연습목표 버튼이 선택되었는지 여부
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialchell2);

        //연결
        Button finishGoalBtn = findViewById(R.id.finishgoal);
        Button practiceGoalBtn = findViewById(R.id.practicegoal);
        Button chellsetbtn = (Button)findViewById(R.id.NextBt);
        EditText chellnameEditText = findViewById(R.id.chellname);
        EditText chellsdateEditText = findViewById(R.id.chellsdate);
        EditText chellfdateEditText = findViewById(R.id.chellfdate);

        helper = new SQLiteHelper(this);

        // 완곡목표 버튼 클릭 이벤트 처리
        finishGoalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishGoalSelected) { // 완곡목표 버튼 선택하는 경우
                    finishGoalBtn.setBackground(getResources().getDrawable(R.drawable.rounded_chell1));
                    practiceGoalBtn.setBackground(getResources().getDrawable(R.drawable.rounded_chellselect1));
                    isFinishGoalSelected = true;
                    isPracticeGoalSelected = false;
                }
            }
        });

        // 연습목표 버튼 클릭 이벤트 처리
        practiceGoalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPracticeGoalSelected) { // 연습목표 버튼 선택하는 경우
                    practiceGoalBtn.setBackground(getResources().getDrawable(R.drawable.rounded_chell2));
                    finishGoalBtn.setBackground(getResources().getDrawable(R.drawable.rounded_chellselect1));
                    isPracticeGoalSelected = true;
                    isFinishGoalSelected = false;

                }
            }
        });

        chellsetbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (isFinishGoalSelected) {
                    category = "완곡목표";
                } else if (isPracticeGoalSelected) {
                    category = "연습목표";
                }

                String title = chellnameEditText.getText().toString(); // 챌린지 내용 가져오기
                String startDateString = chellsdateEditText.getText().toString(); // 시작일 가져오기
                String endDateString = chellfdateEditText.getText().toString(); // 종료일 가져오기

                Date startDate = stringToDate(startDateString); // String을 Date로 변환
                Date endDate = stringToDate(endDateString); // String을 Date로 변환



                CurrentUser user = (CurrentUser)getApplicationContext();
                String set_user = user.getCurrentUser();


                // DB에 저장하는 로직 추가
                db = helper.getWritableDatabase();
                if (db != null) {
                    ContentValues values = new ContentValues();
                    values.put("ENROLL_USER", set_user);
                    values.put("CATEGORY", category);
                    values.put("TITLE", title);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    values.put("STARTDATE", dateFormat.format(startDate));
                    values.put("DEADLINE", dateFormat.format(endDate));

                    long result = db.insert("CHALLENGE", null, values);
                    if (result == -1) {
                        Log.e("Initialchell2Activity", "Error inserting data into CHALLENGE table");
                        Toast.makeText(Initialchell2Activity.this, "Failed to insert data", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Initialchell2Activity", "Data inserted successfully into CHALLENGE table");
                        Toast.makeText(Initialchell2Activity.this, "Data inserted successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Initialchell2Activity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                    db.close();
                } else {
                    Log.e("Initialchell2Activity", "Error accessing database");
                    Toast.makeText(Initialchell2Activity.this, "Failed to access database", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    // String을 Date로 변환하는 메서드
    private Date stringToDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
