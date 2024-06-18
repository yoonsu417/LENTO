package com.example.lento;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

// 최근 연습 이미지 경로, db에 저장하는 클래스
public class RecentPractice {
    private Context context;
    private SQLiteHelper dbHelper;
    SQLiteDatabase db;
    public RecentPractice(Context context) {
        this.context = context;
        dbHelper = new SQLiteHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public void saveDB(String imagePath, String practicedate, String result) {

        String userName;
        userName = getUserNameFromDB();

        db.delete("PRACTICE", "USER_NAME = ?", new String[]{userName});

        Log.d("RecentPractice", "Saving data for user: " + userName + ", imagePath: " + imagePath + ", practiceDate: " + practicedate);

        String query = "SELECT SHEET_TITLE, COMPOSER, GENRE FROM SHEET WHERE SHEET_FILE = ? AND UPLOAD_USER = ?";
        Cursor cursor = db.rawQuery(query, new String[]{imagePath, userName});

        if (cursor != null && cursor.moveToFirst()) {
            // 검색된 레코드의 TITLE, COMPOSER, GENRE 값을 가져오기
            String title = cursor.getString(0);
            String composer = cursor.getString(1);
            String genre = cursor.getString(2);

            // 가져온 값들 출력 (디버깅용)
            Log.d("RecentPractice", "Title: " + title + ", Composer: " + composer + ", Genre: " + genre);

            // PRACTICE 테이블에 데이터 삽입
            ContentValues values = new ContentValues();
            values.put("USER_NAME", userName);
            values.put("IMAGE_PATH", imagePath);
            values.put("PRACTICE_DATE", practicedate);
            values.put("TITLE", title);
            values.put("COMPOSER", composer);
            values.put("GENRE", genre);
            values.put("ACCURACY", result);

            long rowId = db.insert("PRACTICE", null, values);

            // 예시로 제목, 작곡가, 장르, 이미지 경로 및 연습 날짜를 출력
            System.out.println("Title: " + title + ", Composer: " + composer + ", Genre: " + genre);
            System.out.println("Image Path: " + imagePath + ", Practice Date: " + practicedate + " Accuracy" + result);
        } else {
            // 해당 imagePath에 대한 레코드가 없을 경우 처리
            Log.d("RecentPractice", "No matching sheet found for the given imagePath.");
        }

        cursor.close();
    }

    public String getUserNameFromDB() {
        String userName = "";

        // 데이터베이스에서 사용자 이름을 가져오는 쿼리
        //String query = "SELECT NAME FROM USER";

        CurrentUser user = (CurrentUser)context.getApplicationContext();
        String set_user = user.getCurrentUser();
        String query = "SELECT NAME FROM USER WHERE EMAIL = '" + set_user + "'";
        Cursor cursor = db.rawQuery(query, null);

        // 결과가 있으면 사용자 이름을 가져옴
        if (cursor.moveToFirst()) {
            userName = cursor.getString(0);
        }

        // 커서를 닫음
        cursor.close();

        return userName;
    }



}
