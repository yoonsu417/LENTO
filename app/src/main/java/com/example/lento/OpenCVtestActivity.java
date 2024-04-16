package com.example.lento;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.OpenCVLoader;

import android.view.View;

public class OpenCVtestActivity extends AppCompatActivity {
    private static final String TAG = "TEST_OPEN_CV_ANDROID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cvtest);

        // OpenCV 라이브러리 초기화
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV 초기화 실패!");
            return;
        } else {
            Log.d(TAG, "OpenCV 초기화 성공!!!!!");
        }

        ImageView OpenCVtest;
        OpenCVtest = findViewById(R.id.OpenCVtest);

        // 출력할 비트맵 객체 선언
        Bitmap Bitmapimage;
        OpenCVLoader.initDebug();


        // Mat 객체 불러오기 : JPG -> Mat
        Mat image = null;
        try {
            image = Utils.loadResource(getApplicationContext(), R.drawable.summer);
        } catch (Exception e) {
            Log.e(TAG, "이미지를 읽을 수 없습니다: " + e.getMessage());
            e.printStackTrace();
        }

        if (image == null || image.empty()) {
            Log.e(TAG, "이미지를 읽을 수 없습니다.");
            return;
        }

        Mat grayMat = new Mat();
        Mat binaryMat = new Mat();

        // 그레이스케일
        Imgproc.cvtColor(image, grayMat, Imgproc.COLOR_BGR2GRAY);

        // 이진화
        Imgproc.threshold(grayMat, binaryMat, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        // 비트맵 선언 + Mat 객체 -> 비트맵 변환
        Bitmapimage = Bitmap.createBitmap(binaryMat.cols(), binaryMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(binaryMat, Bitmapimage);

        // 비트맵 이미지 화면 출력
        OpenCVtest.setImageBitmap(Bitmapimage);

    }


}
