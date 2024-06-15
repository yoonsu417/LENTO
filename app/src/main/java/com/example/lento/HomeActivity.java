package com.example.lento;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.pdf.PdfRenderer;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class HomeActivity extends Activity {

    PdfRenderer renderer;
    int display_page = 0;
    public static final int PICK_FILE = 99;
    SQLiteHelper dbHelper;
    SQLiteDatabase db;

    ImageView recentPractice;
    private TextView nameP, madeP, genreP, dateP, emptyMessage;
    private LinearLayout songDetailsLayout;

    private static final String SHARED_PREF_NAME = "practicePrefs";
    private static final String KEY_IMAGE_PATH = "imagePath";
    private static final String KEY_PRACTICE_DATE = "practiceDate";
    private String imagePath;
    private String practiceDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar progressBar = findViewById(R.id.progressBar);

        // 프로그레스바의 전체 Drawable 얻기
        LayerDrawable drawable = (LayerDrawable) progressBar.getProgressDrawable();

        // 채워진 부분 색상 변경
        Drawable progressDrawable = drawable.findDrawableByLayerId(android.R.id.progress);
        progressDrawable.setTint(Color.parseColor("#96AAFF"));

        // 채워지지 않은 부분 색상 변경
        Drawable backgroundDrawable = drawable.findDrawableByLayerId(android.R.id.background);
        backgroundDrawable.setTint(Color.parseColor("#4DFFFFFF"));


        // DatabaseHelper 초기화
        dbHelper = new SQLiteHelper(this);
        db = dbHelper.getReadableDatabase();

        // 사용자 이름을 가져와서 사용자 이름에 맞게 홈화면 설정
        TextView userNameTextView = findViewById(R.id.name);
        String userName = getUserNameFromDB(); // 데이터베이스에서 사용자 이름 가져오기
        userName += "님,";
        userNameTextView.setText(userName); // 텍스트뷰에 사용자 이름 설정

        // 최근 저장된 챌린지 정보를 가져와서 홈 화면에 표시
        displayRecentChallenge();


        // 악보이미지 누르면 pdf 선택 화면으로 이동
        Button Upload;

        Upload = (Button)findViewById(R.id.Upload);

        /*
        // 악보인식 테스트용 버튼
        Button test;
        test = (Button)findViewById(R.id.OpenCVtest);

        test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,OpenCVtestActivity.class);
                startActivity(intent);
            }
        });

         */

        Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf"); // pdf 파일만 선택 설정
                startActivityForResult(intent, PICK_FILE);
            }
        });

        nameP = findViewById(R.id.nameP);
        madeP = findViewById(R.id.madeP);
        genreP = findViewById(R.id.genreP);
        dateP = findViewById(R.id.dateP);
        recentPractice = findViewById(R.id.recentPractice);
        emptyMessage = findViewById(R.id.emptyMessage);
        songDetailsLayout = findViewById(R.id.songDetailsLayout);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        imagePath = sharedPreferences.getString(KEY_IMAGE_PATH, null);
        practiceDate = sharedPreferences.getString(KEY_PRACTICE_DATE, null);

        dateP.setText(practiceDate);

        if (imagePath != null && !imagePath.isEmpty()) {
            emptyMessage.setVisibility(View.GONE);
            songDetailsLayout.setVisibility(View.VISIBLE);

            Uri fileUri = Uri.parse(imagePath);

            // 권한 재확인 및 파일 처리
            try {
                getContentResolver().takePersistableUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(fileUri, "r");
                renderer = new PdfRenderer(parcelFileDescriptor);
                display_page = 0;
                _display(display_page);
                displayRecentDetails(imagePath);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
                System.out.println("파일을 찾을 수 없습니다: " + fnfe.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("IO 예외 발생: " + e.getMessage());
            } catch (SecurityException se) {
                se.printStackTrace();
                System.out.println("권한이 거부되었습니다: " + se.getMessage());
            }
        } else {
            emptyMessage.setVisibility(View.VISIBLE);
            songDetailsLayout.setVisibility(View.GONE);
        }
    }

    private void _display(int _n) {
        if(renderer != null) {
            PdfRenderer.Page page = renderer.openPage(_n);
            Bitmap mBitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            recentPractice.setImageBitmap(mBitmap);
            page.close();
        }
    }

    protected void onPause() {
        super.onPause();

    }

    private void displayRecentDetails(String imagePath) {
        DBManager dbManager = new DBManager(this);
        Recent recent = dbManager.getRecentByImagePath(imagePath);

        if (recent != null) {
            nameP = findViewById(R.id.nameP);
            madeP = findViewById(R.id.madeP);
            genreP = findViewById(R.id.genreP);

            nameP.setText(recent.getTitle());
            madeP.setText(recent.getComposer());
            genreP.setText(recent.getGenre());
        }
    }


    // 데이터베이스에서 사용자 이름을 가져오는 메서드
    public String getUserName(String userEmail) {
        dbHelper = new SQLiteHelper(this);
        db = dbHelper.getReadableDatabase();
        String userName = "";

        // 사용자의 이메일을 기반으로 사용자 이름을 검색하는 쿼리
        String query = "SELECT NAME FROM USER WHERE EMAIL = '" + userEmail + "'";
        Cursor cursor = db.rawQuery(query, null);

        // 쿼리 결과에서 사용자 이름을 가져옴
        if (cursor.moveToFirst()) {
            userName = cursor.getString(0);
        }

        cursor.close();
        db.close();

        return userName;
    }


    private String getUserNameFromDB() {
        String userName = "";

        // 데이터베이스에서 사용자 이름을 가져오는 쿼리
        //String query = "SELECT NAME FROM USER";

        CurrentUser user = (CurrentUser)getApplicationContext();
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

    // 최근 저장된 챌린지 정보를 가져와서 홈 화면에 표시하는 메서드
    private void displayRecentChallenge() {
        TextView challTypeTextView = findViewById(R.id.challtype);
        TextView challGoalTextView = findViewById(R.id.challgoal);
        TextView challSTimeTextView = findViewById(R.id.challstime);
        TextView challFTimeTextView = findViewById(R.id.challftime);

        // 현재 로그인한 사용자의 이메일 가져오기
        CurrentUser currentUser = (CurrentUser) getApplicationContext();
        String userEmail = currentUser.getCurrentUser();

        // 최근 저장된 챌린지 정보를 가져오는 쿼리
        String query = "SELECT CATEGORY, TITLE, STARTDATE, DEADLINE FROM CHALLENGE " +
                "WHERE ENROLL_USER = ? ORDER BY CODE DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{userEmail});

        // 조회된 결과가 있으면
        if (cursor.moveToFirst()) {
            // 챌린지 정보 가져오기
            int categoryIndex = cursor.getColumnIndex("CATEGORY");
            String category = cursor.getString(categoryIndex);
            int titleIndex = cursor.getColumnIndex("TITLE");
            String title = cursor.getString(titleIndex);
            int startDateIndex = cursor.getColumnIndex("STARTDATE");
            String startDate = cursor.getString(startDateIndex);
            int endDateIndex = cursor.getColumnIndex("DEADLINE");
            String endDate = cursor.getString(endDateIndex);

            // 각 뷰에 챌린지 정보 설정
            challTypeTextView.setText(category);
            challGoalTextView.setText(title);
            challSTimeTextView.setText(startDate);
            challFTimeTextView.setText(endDate);
        }else{
            // 최근 저장된 챌린지가 없는 경우 처리
            challTypeTextView.setText("");
            challGoalTextView.setText("챌린지를 추가하세요.");
            challSTimeTextView.setText("");
            challFTimeTextView.setText("");
        }

        // 커서를 닫음
        cursor.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FILE && resultCode == RESULT_OK) {
            if(data != null){
                Uri uri = data.getData();

                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent intent = new Intent(HomeActivity.this, UploadActivity.class);
                intent.putExtra("fileUri", uri.toString());
                startActivity(intent);
            }
        }
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

