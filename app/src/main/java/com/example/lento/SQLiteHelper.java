package com.example.lento;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LENTO.db";
    private static final int DATABASE_VERSION = 1;

    public static final String DELETE_TABLE_ALL = "DROP TABLE IF EXISTS USER";

    public SQLiteHelper (@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void onCreate(SQLiteDatabase db) {
        // 사용자 DB
        db.execSQL("CREATE TABLE IF NOT EXISTS USER (" +
                "EMAIL TEXT NOT NULL PRIMARY KEY," +
                "NAME TEXT NOT NULL," +
                "PW TEXT NOT NULL," +
                "TOTAL_SHEET INT DEFAULT 0," +
                "TOTAL_PRACTICE INT DEFAULT 0);");

        // ***테스트용으로 추가한 사용자 DB 칼럼*** 추후 삭제 예정
        db.execSQL("INSERT INTO USER VALUES (" +
                "'asdf123', " + "'asdf', " + "'123', 0, 0);");

        // 악보 DB
        db.execSQL("CREATE TABLE IF NOT EXISTS SHEET (" +
                "SHEET_TITLE TEXT NOT NULL PRIMARY KEY," +
                "SHEET_FILE TEXT NOT NULL," +       // IE에 없는 칼럼. 악보 파일 URI
                "UPLOAD_USER TEXT NOT NULL, " +
                "UPLOAD_DATE DATE," +
                "PAGE INTEGER," +
                "COMPOSER TEXT," +
                "GENRE TEXT," +
                "COUNT INTEGER DEFAULT 0," +        // 연습횟수
                "FAVORITE INTEGER DEFAULT 0," +     // BOOLEAN이 없어서 0, 1로 대체.
                "PRECISION_AVG INTEGER," +
                "FOREIGN KEY (UPLOAD_USER) REFERENCES USER (EMAIL) " +
                "ON UPDATE CASCADE ON DELETE CASCADE);");

        // 도전과제 DB
        db.execSQL("CREATE TABLE IF NOT EXISTS CHALLENGE (" +
                "CODE INTEGER NOT NULL PRIMARY KEY," +
                "ENROLL_USER TEXT NOT NULL," +
                "TITLE TEXT NOT NULL," +
                "DEADLINE DATE NOT NULL," +
                "COMPLETE INTEGER DEFAULT 0," +     // "
                "FOREIGN KEY (ENROLL_USER) REFERENCES USER (EMAIL) " +
                "ON UPDATE CASCADE ON DELETE CASCADE);");

        // 연습 통계 DB
        db.execSQL("CREATE TABLE IF NOT EXISTS STATISTICS (" +
                "SHEET_TITLE TEXT NOT NULL," +
                "PRACTICE_DATE DATE NOT NULL," +
                "PRACTICE_PRECISION INTEGER," +
                "FEEDBACK_FILE TEXT," +
                "PRIMARY KEY (SHEET_TITLE, PRACTICE_DATE)," +
                "FOREIGN KEY (SHEET_TITLE) REFERENCES SHEET (SHEET_TITLE)" +
                "ON UPDATE CASCADE ON DELETE CASCADE);");



    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DB 삭제 후 재생성 구문 (용이한 테스트 위해 미리 짜둠)
        db.execSQL(DELETE_TABLE_ALL);
        onCreate(db);
    }
}
