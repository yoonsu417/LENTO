package com.example.lento;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddInfoActivity extends Activity {
    PdfRenderer renderer;
    int total_pages = 0;
    int display_page = 0;

    ImageView showPdf;
    TextView date, title, composer, genre, page;
    Button addBt;

    // DB 관련
    SQLiteHelper helper;
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addinfo);

        // 연결
        showPdf = (ImageView) findViewById(R.id.image);
        date = (TextView)findViewById(R.id.inputDate);
        title = (TextView)findViewById(R.id.inputTitle);
        composer = (TextView)findViewById(R.id.inputComposer);
        genre = (TextView)findViewById(R.id.inputGenre);
        page = (TextView)findViewById(R.id.inputCount);
        addBt = (Button)findViewById(R.id.addsheet);

        helper = new SQLiteHelper(this);

        String fileUriString = getIntent().getStringExtra("fileUri");
        Uri fileUri = Uri.parse(fileUriString);

        // 악보 이미지 출력
        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver()
                    .openFileDescriptor(fileUri, "r");
            renderer = new PdfRenderer(parcelFileDescriptor);
            total_pages = renderer.getPageCount();
            display_page = 0;
            _display(display_page);
        } catch(FileNotFoundException fnfe){

        }catch (IOException e){

        }


        // 업로드 날짜 출력
        Date currentDate = new Date();
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formatDate = mFormat.format(currentDate);

        date.setText(formatDate);

        // 페이지 수 출력
        page.setText(String.valueOf(total_pages) + " 페이지");



        addBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String set_title = title.getText().toString();
                String set_file = fileUriString;
                Date set_date = currentDate;
                int set_page = total_pages;
                String set_composer = composer.getText().toString();
                String set_genre = genre.getText().toString();

                CurrentUser user = (CurrentUser)getApplicationContext();
                String set_user = user.getCurrentUser();


                if (set_title.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    // DB (W)
                    db = helper.getWritableDatabase();
                    db.execSQL("INSERT INTO SHEET VALUES ('" +
                            set_title + "', '" + set_file + "', '" + set_user + "', '" +
                            set_date + "', '" + set_page + "', '" + set_composer + "', '" +
                            set_genre + "', 0, 0, 0);");
                    db.close();

                    // 완료 토스트 메시지
                    Toast.makeText(getApplicationContext(), "악보 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                    // 화면 전환
                    Intent intent = new Intent(AddInfoActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    private void _display(int _n) {
        if(renderer != null) {
            PdfRenderer.Page page = renderer.openPage(_n);
            Bitmap mBitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            showPdf.setImageBitmap(mBitmap);
            page.close();
        }
    }

}
