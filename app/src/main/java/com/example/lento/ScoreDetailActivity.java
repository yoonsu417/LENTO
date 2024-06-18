package com.example.lento;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScoreDetailActivity extends AppCompatActivity{
    private boolean isStarred = false; // 즐겨찾기 상태
    private ScoreAdapter adapter;
    PdfRenderer renderer;
    int display_page = 0;
    private static final int REQUEST_CODE = 1;
    private ImageView sheetImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreinfo);

        //정확도 표 클릭시 상세페이지
        ImageView accuracy=findViewById(R.id.accuracyPer);
        accuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent next2Intent = new Intent(ScoreDetailActivity.this, PerformProgressActivity.class);
                startActivity(next2Intent);
            }
        });


        // 즐겨찾기 상태를 SharedPreferences에서 불러오기
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        isStarred = sharedPreferences.getBoolean("isStarred", false);

        //back아이콘 클릭시 다시 리스트 페이지 이동
        ImageView backImageView = findViewById(R.id.back);
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 액티비티를 종료하고 이전 액티비티로 돌아감
                //Intent nextIntent = new Intent(ScoreDetailActivity.this, ScorelistActivity.class);
                finish();
            }
        });

        // 즐겨찾기 버튼 클릭 이벤트 처리
        ImageView starImageView = findViewById(R.id.star);
        starImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 즐겨찾기 상태 변경
                isStarred = !isStarred;

                // 이미지 업데이트
                starImageView.setImageResource(isStarred ? R.drawable.starfill : R.drawable.starstroke);

                // 토스트 메시지 표시
                if (isStarred) {
                    Toast.makeText(ScoreDetailActivity.this, "즐겨찾기에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ScoreDetailActivity.this, "즐겨찾기가 해제되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // 인텐트에서 데이터 가져오기
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String composer = intent.getStringExtra("composer");

            // XML 레이아웃에서 뷰 찾기
            sheetImage = findViewById(R.id.simage);
            TextView sheetTitle = findViewById(R.id.stitle);
            TextView sheetComposer = findViewById(R.id.scomposer);
            TextView sheetPage = findViewById(R.id.pages);
            TextView sheetGenre = findViewById(R.id.genre);
            TextView sheetUploadDate = findViewById(R.id.uploaddate);

            // 악보 페이지 수, 장르, 업로드 날짜 가져오기
            DBManager dbManager = new DBManager(this);
            ScoreDetails scoreDetails = dbManager.getScoreDetails(title, composer);
            //SQLiteHelper dbHelper = new SQLiteHelper(this);
            //ScoreDetails scoreDetails = dbHelper.getScoreDetails(title, composer);

            // 데이터 설정
            sheetTitle.setText(title);
            sheetComposer.setText(composer);
            sheetPage.setText(scoreDetails.getPage()+ " 페이지");
            sheetGenre.setText(scoreDetails.getGenre());
            // 업로드 날짜 설정
            String uploadDateString = scoreDetails.getUploadDate();
            String format = formatDate(uploadDateString);
            sheetUploadDate.setText(format);

            // 복원된 즐겨찾기 상태에 따라 아이콘 이미지 업데이트
            updateStarImage();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            } else {
                loadImageFromIntent(imagePath);

            }
        }

        Button practicebtn=findViewById(R.id.practiceBt);
        practicebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 화면 이동
                Intent next2pIntent = new Intent(ScoreDetailActivity.this, PlayActivity.class);
                // 이미지 경로 가져가기
                next2pIntent.putExtra("imagePath", imagePath);
                startActivity(next2pIntent);

            }
        });

    }


    private void loadImageFromIntent(String imagePath) {
        Intent intent = getIntent();
        Uri imageUri = Uri.parse(imagePath);

        Log.d("ImagePath", "imagePath: " + imagePath);
        Log.d("ImageUri", "imageUri: " + imageUri.toString());

        // 악보 이미지 출력
        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver()
                    .openFileDescriptor(imageUri, "r");
            renderer = new PdfRenderer(parcelFileDescriptor);
            display_page = 0;
            _display(display_page);
        } catch(FileNotFoundException fnfe){

        }catch (IOException e){

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onResume() 메서드에서 다시 즐겨찾기 상태를 확인하여 업데이트
        updateStarImage();
    }

    private void updateStarImage() {
        ImageView starImageView = findViewById(R.id.star);
        starImageView.setImageResource(isStarred ? R.drawable.starfill : R.drawable.starstroke);
    }

    // 업로드 날짜 오류 수정
    private String formatDate(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH); // 입력 문자열 형식
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd"); // 원하는 출력 형식

        Date date = null;
        try {
            date = inputFormat.parse(dateString); // 문자열을 Date 객체로 변환
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputFormat.format(date);
    }
    private void _display(int _n) {
        if(renderer != null) {
            PdfRenderer.Page page = renderer.openPage(_n);
            Bitmap mBitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            sheetImage.setImageBitmap(mBitmap);
            page.close();
        }
    }
}
