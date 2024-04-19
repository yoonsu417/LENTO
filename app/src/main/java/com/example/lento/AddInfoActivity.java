package com.example.lento;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddInfoActivity extends Activity {
    PdfRenderer renderer;
    int total_pages = 0;
    int display_page = 0;
    ImageView showPdf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addinfo);

        showPdf = (ImageView) findViewById(R.id.image);

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


        // 업로드 날짜 출력
        Date currentDate = new Date();
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formatDate = mFormat.format(currentDate);

        TextView date = (TextView)findViewById(R.id.inputDate);
        date.setText(formatDate);

        // 페이지 수 출력
        TextView page = (TextView)findViewById(R.id.inputCount);
        page.setText(String.valueOf(total_pages) + " 페이지");
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
