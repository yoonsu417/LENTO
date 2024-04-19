package com.example.lento;

import android.app.Activity;
import android.content.Intent;
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

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.IOException;

public class UploadActivity extends Activity {

    PdfRenderer renderer;
    int total_pages = 0;
    int display_page = 0;

    TextView showPages;
    ImageView showPdf;

    Button next;

    private static final String TAG = "TEST_OPEN_CV_ANDROID";

    // 파일 받아서 activity_upload.xml에 출력
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Button before = findViewById(R.id.before);
        Button after = findViewById(R.id.after);

        showPdf = (ImageView)findViewById(R.id.showPdf);
        showPages = (TextView)findViewById(R.id.showPages);


        String fileUriString = getIntent().getStringExtra("fileUri");
        Uri fileUri = Uri.parse(fileUriString);

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

        //이전 페이지
        before.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(display_page > 0) {
                    display_page--;
                    _display(display_page);
                }
            }
        });

        // 다음 페이지
        after.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(display_page < (total_pages -1)) {
                    display_page++;
                    _display(display_page);
                }
            }
        });

        // 다음으로 누르면 정보 입력 페이지 이동
        next = (Button)findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent nextIntent = new Intent(UploadActivity.this, AddInfoActivity.class);
                nextIntent.putExtra("fileUri", fileUri.toString());
                startActivity(nextIntent);
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
            showPages.setText((_n + 1) + "/" + total_pages);
        }
    }
}
