package com.example.lento;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private static final String DATABASE_NAME = "LENTO.db";
    private static final int DATABASE_VERSION = 1;

    private Context context;

    public DBManager(Context context) {
        this.context = context;
    }

    public List<Score> getScores(String user) {
        List<Score> scoreList = new ArrayList<>();
        SQLiteDatabase db = new SQLiteHelper(context).getReadableDatabase();


        Cursor cursor = db.rawQuery("SELECT SHEET_FILE, SHEET_TITLE, COMPOSER FROM SHEET WHERE UPLOAD_USER = '" + user + "';", null);

        // 커서가 열 이름 목록을 포함하고 있는지 확인
        String[] columnNames = cursor.getColumnNames();
        if (columnNames.length == 0) {
            // 커서가 열 이름을 포함하지 않으면 에러 처리
            Log.e("SQLiteHelper", "No columns found in cursor");
            return scoreList;
        }

        int sheetFileIndex = cursor.getColumnIndex("SHEET_FILE");
        int sheetTitleIndex = cursor.getColumnIndex("SHEET_TITLE");
        int composerIndex = cursor.getColumnIndex("COMPOSER");

        if (sheetFileIndex == -1 || sheetTitleIndex == -1 || composerIndex == -1) {
            // 커서가 요청한 열을 찾지 못한 경우 에러 처리
            Log.e("SQLiteHelper", "Requested columns not found in cursor");
            return scoreList;
        }

        if (cursor.moveToFirst()) {
            do {
                // 열 이름을 수정하여 데이터를 가져옵니다.
                String imagePath = cursor.getString(sheetFileIndex);
                String title = cursor.getString(sheetTitleIndex);
                String composer = cursor.getString(composerIndex);

                // 악보 객체를 생성하여 리스트에 추가
                Score score = new Score(title, composer, imagePath);
                scoreList.add(score);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return scoreList;
    }

    public ScoreDetails getScoreDetails(String title, String composer) {
        ScoreDetails scoreDetails = null;
        SQLiteDatabase db = new SQLiteHelper(context).getReadableDatabase();

        String[] projection = {
                "PAGE",
                "GENRE",
                "UPLOAD_DATE"
        };

        String selection = "SHEET_TITLE = ? AND COMPOSER = ?";
        String[] selectionArgs = {title, composer};

        Cursor cursor = db.query(
                "SHEET",
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int page = cursor.getInt(cursor.getColumnIndexOrThrow("PAGE"));
            String genre = cursor.getString(cursor.getColumnIndexOrThrow("GENRE"));
            String uploadDate = cursor.getString(cursor.getColumnIndexOrThrow("UPLOAD_DATE"));

            scoreDetails = new ScoreDetails(page, genre, uploadDate);
        }

        if (cursor != null) {
            cursor.close();
        }

        db.close();

        return scoreDetails;
    }
}
