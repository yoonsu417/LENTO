package com.example.lento;

import static com.example.lento.Modules.normalization;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ImageView;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.OpenCVLoader;

import android.view.View;

public class OpenCVtestActivity extends AppCompatActivity {
    private static final String TAG = "TEST_OPEN_CV_ANDROID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cvtest);

        /*
        // OpenCV 라이브러리 초기화
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV 초기화 실패!");
            return;
        } else {
            Log.d(TAG, "OpenCV 초기화 성공!!!!!");
        }
        */

        ImageView OpenCVtest;
        OpenCVtest = findViewById(R.id.OpenCVtest);

        // Mat 객체 불러오기 : JPG -> Mat
        Mat image = null;
        try {
            image = Utils.loadResource(getApplicationContext(), R.drawable.sheet);
        } catch (Exception e) {
            Log.e(TAG, "이미지를 읽을 수 없습니다: " + e.getMessage());
            e.printStackTrace();
        }

        if (image == null || image.empty()) {
            Log.e(TAG, "이미지를 읽을 수 없습니다.");
            return;
        }


        // 그레이 스케일
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);


        // 이진화
        Mat binaryImage = new Mat();
        Imgproc.threshold(grayImage, binaryImage, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binaryImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        // 보표로 추정되는 영역을 표시할 이미지 생성
        Mat extractedImage = Mat.zeros(binaryImage.size(), binaryImage.type());

        for (MatOfPoint contour : contours) {
            // 컨투어를 감싸는 최소 사각형(rectangle)을 구함
            Rect rect = Imgproc.boundingRect(contour);
            // 최소 사각형의 가로 길이가 이미지 가로 길이의 70% 이상이면 보표로 판단하고 표시할 이미지에 해당 부분을 그림
            if (rect.width >= binaryImage.cols() * 0.7 && rect.width != binaryImage.cols()) {
                // 보표로 판단된 영역을 표시할 이미지에 그림
                Imgproc.rectangle(extractedImage, rect.tl(), rect.br(), new Scalar(255, 255, 255), -1); // 흰색으로 채움
            }
        }

        // 추출된 보표 영역만을 남기고 나머지 부분을 검정색으로 만듬
        Core.bitwise_and(binaryImage, extractedImage, binaryImage);

        // 오선 제거
        Modules remove = new Modules();
        Pair<Mat, List<int[]>> result = remove.removeStaves(binaryImage);
        Mat imageWithoutStaves = result.first;
        List<int[]> stavesInfo = result.second;

        for (int[] stave : stavesInfo) { // 오선 좌표 제대로 가져오는지 확인 (Logcat)
            System.out.println(Arrays.toString(stave));
        }

        // 정규화
        Modules normal = new Modules();
        Pair<Mat, List<int[]>> normalizedResult = normal.normalization(imageWithoutStaves, stavesInfo, 10); // 오선 간격 10 픽셀
        Mat normalizedImage = normalizedResult.first;
        List<int[]> normalizedStaves = normalizedResult.second;

        for (int[] stave : normalizedStaves) { // 오선 좌표 제대로 가져오는지 확인 (Logcat)
            System.out.println(Arrays.toString(stave));
        }

        // 정규화 2

        // 비트맵 선언 + Mat 객체 -> 비트맵 변환
        Bitmap Bitmapimage;
        Bitmapimage = Bitmap.createBitmap(normalizedImage.cols(), normalizedImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(normalizedImage, Bitmapimage);

        // 비트맵 이미지 화면 출력
        OpenCVtest.setImageBitmap(Bitmapimage);



    }

    // 수평 히스토그램을 사용하여 오선을 삭제하는 메서드
    public static Pair<Mat, List<int[]>> removeStaves(Mat image) {
        int height = image.rows();
        int width = image.cols();
        List<int[]> staves = new ArrayList<>();

        // 오선 검출
        for (int row = 0; row < height; row++) {
            int pixels = 0;
            for (int col = 0; col < width; col++) {
                if (image.get(row, col)[0] == 255) { // 흰색 픽셀 개수 세기
                    pixels++;
                }
            }
            if (pixels >= width * 0.5) { // 이미지 너비의 50% 이상이라면
                if (staves.isEmpty() || Math.abs(staves.get(staves.size() - 1)[0] + staves.get(staves.size() - 1)[1] - row) > 1) { // 첫 오선이거나 이전에 검출된 오선과 다른 오선
                    staves.add(new int[] {row, 0}); // 오선 추가 [오선의 y 좌표][오선 높이]
                } else { // 이전에 검출된 오선과 같은 오선
                    staves.get(staves.size() - 1)[1]++; // 높이 업데이트
                }
            }
        }

        // 오선 제거
        for (int[] staff : staves) {
            int topPixel = staff[0]; // 오선의 최상단 y 좌표
            int botPixel = staff[0] + staff[1]; // 오선의 최하단 y 좌표 (오선의 최상단 y 좌표 + 오선 높이)
            for (int col = 0; col < width; col++) {
                if (image.get(topPixel - 1, col)[0] == 0 && image.get(botPixel + 1, col)[0] == 0) { // 오선 위, 아래로 픽셀이 있는지 탐색
                    for (int row = topPixel; row <= botPixel; row++) {
                        image.put(row, col, 0); // 오선을 지움
                    }
                }
            }
        }
        return new Pair<>(image,staves);
    }


    // 객체 검출
/*
    public static Pair<Mat, List<Pair<Integer, Rect>>> objectDetection(Mat image, List<Integer> staves) {
        int lines = (int) Math.ceil(staves.size() / 5.0);
        List<Pair<Integer, Rect>> objects = new ArrayList<>();

        Mat closingImage = new Mat();
        Imgproc.morphologyEx(image, closingImage, Imgproc.MORPH_CLOSE, new Mat());
        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();
        int cnt = Imgproc.connectedComponentsWithStats(closingImage, labels, stats, centroids);

        // 모든 객체 검출
        for (int i = 1; i < cnt; i++) {
            double x = stats.get(i, 0)[0];
            double y = stats.get(i, 1)[0];
            double w = stats.get(i, 2)[0];
            double h = stats.get(i, 3)[0];
            double area = stats.get(i, 4)[0];

            if (w >= getWeighted(5) && h >= getWeighted(5)) {
                double center = getCenter(y, h);
                for (int line = 0; line < lines; line++) {
                    double areaTop = staves.get(line * 5) - getWeighted(20);
                    double areaBot = staves.get((line + 1) * 5 - 1) + getWeighted(20);

                    if (areaTop <= center && center <= areaBot) {
                        objects.add(new Pair<>(line, new Rect((int) x, (int) y, (int) w, (int) h)));
                    }
                }
            }
        }

        Collections.sort(objects, (a, b) -> a.getFirst().compareTo(b.getFirst()));

        return new Pair<>(image, objects);
    }

    private static double getWeighted(int value) {
        int standard = 10;
        return (int)(value * (standard / 10));
    }

    private static double getCenter(double y, double h) {
        return y + y + h / 2;
    }

    private static class Pair<T, U> {
        private T first;
        private U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() {
            return first;
        }

        public U getSecond() {
            return second;
        }
    }
 */

}
