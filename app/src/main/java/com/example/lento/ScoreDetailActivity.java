package com.example.lento;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScoreDetailActivity extends AppCompatActivity{
    private boolean isStarred = false; // 즐겨찾기 상태
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreinfo);

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
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String composer = intent.getStringExtra("composer");
            String imagePath = intent.getStringExtra("imagePath");

            // XML 레이아웃에서 뷰 찾기
            ImageView sheetImage = findViewById(R.id.simage);
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
            Date currentDate = new Date();
            SimpleDateFormat mFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            String formatDate = mFormat.format(currentDate);
            sheetUploadDate.setText(formatDate);
            //sheetUploadDate.setText(scoreDetails.getUploadDate());

            // Glide를 사용하여 이미지 설정
            Glide.with(this)
                    .load(new File(imagePath)) // 이미지 경로 설정
                    .error(R.drawable.scorelisttmp) // 이미지 로드 실패 시 대체할 이미지 설정
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("GlideError", "Image loading failed", e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(sheetImage);
            // 복원된 즐겨찾기 상태에 따라 아이콘 이미지 업데이트
            updateStarImage();
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
}
