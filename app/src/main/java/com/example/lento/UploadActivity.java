package com.example.lento;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;

public class UploadActivity extends Activity {

    PdfRenderer renderer;
    int total_pages = 0;
    int display_page = 0;

    TextView showPages;
    ImageView showPdf;

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
