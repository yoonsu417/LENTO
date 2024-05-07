package com.example.lento;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ImageView;
import android.util.Log;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
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


        // 1. 그레이 스케일
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);


        // 2. 이진화
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

        // 3. 오선 제거
        Pair<Mat, List<int[]>> result = removeStaves(binaryImage);
        Mat imageWithoutStaves = result.first;
        List<int[]> stavesInfo = result.second;

        System.out.println("오선 좌표");
        for (int[] stave : stavesInfo) { // 오선 좌표 제대로 가져오는지 확인 (Logcat)
            System.out.println(Arrays.toString(stave));
        }


        // 4. 정규화
        Pair<Mat, List<double[]>> normalizedResult = normalization(imageWithoutStaves, stavesInfo, 10); // 오선 간격 10 픽셀
        Mat normalizedImage = normalizedResult.first;
        List<double[]> normalizedStaves = normalizedResult.second;

        System.out.println("정규화 후 오선 좌표");
        for (double[] stave : normalizedStaves) { // 오선 좌표 제대로 가져오는지 확인 (Logcat)
            System.out.println(Arrays.toString(stave));
        }


        // 5. 객체 검출
        Pair<Mat, List<Object[]>> detect = objectDetection(normalizedImage, normalizedStaves);
        Mat detectionImage = detect.first;
        List<Object[]> detectionObjects = detect.second;
        System.out.println("객체 검출 후"); // 확인 용도 (Logcat)
        for (Object[] obj : detectionObjects) {
            System.out.print("[" + obj[0] + ", ");
            int[] intArray = (int[]) obj[1];
            System.out.println(Arrays.toString(intArray) + "]");
        }


        // 6. 객체 분석




        // 비트맵 선언 + Mat 객체 -> 비트맵 변환
        Bitmap Bitmapimage;
        Bitmapimage = Bitmap.createBitmap(detectionImage.cols(), detectionImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(detectionImage, Bitmapimage);

        // 비트맵 이미지 화면 출력
        OpenCVtest.setImageBitmap(Bitmapimage);

    }









    // ---------------------------------------- funtions ------------------------------------------

    private static int getWeighted(int value) {
        int standard = 10;
        return (int)(value * (standard / 10));
    }

    private static double getCenter(int y, int h) {
        return ( y + y + h ) / 2;
    }

    public static Mat closing(Mat image) {
        Mat kernel = Mat.ones(getWeighted(5), getWeighted(5), CvType.CV_8U);
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_CLOSE, kernel);
        return image;
    }

    public static void put_text(Mat image, String text, Point loc){
        double fontScale = 0.6;
        int thickness = 2;
        Scalar color = new Scalar(255, 0, 0);
        Imgproc.putText(image, text, loc, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, color, thickness);
    }

    final boolean VERTICAL = true;
    final boolean HORIZONTAL = false;

    public static List<int[]> getLine(Mat image, boolean axis, int axis_value, int start, int end, int length){

        List<int[]> points = new ArrayList<>();

        // 수직이면
        if (axis) {
            for(int i = start; i < end; i++){
                int[] point = {i, axis_value};
                points.add(point);
            }
        }else{
            for(int i = start; i< end; i++){
                int[] point = {axis_value, i};
                points.add(point);
            }
        }
        int pixels = 0;
        int y = 0;
        int x = 0;
        for(int i = 0; i<points.size(); i++){
            int[] point = points.get(i);
            y = point[0];
            x = point[1];
            pixels += (image.get(y,x)[0] == 255) ? 1 : 0; //흰색 픽셀 개수

            int next_y = axis ? y + 1 : y;
            int next_x = axis ? x : x + 1;
            int next_point = (int)image.get(next_y, next_x)[0];  // 다음 탐색할 지점

            if(next_point == 0 || i == points.size() - 1){
                if (pixels >= getWeighted(length)){
                    break;
                }else{
                    pixels = 0;
                }
            }
        }

        return new ArrayList<>(List.of(new int[]{axis? y: x, pixels}));
    }

    /*
    // 기둥 검출 함수
    public static List<int[]> stemDetection(Mat image, int[] stats, int length){

    }

     */













    // -------------------------------------  Modules ---------------------------------------------

    // 3. (수평 히스토그램을 사용하여) 오선을 삭제하는 메서드
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
                    /*
                    System.out.println("if 구문");
                    for (int[] stave : staves) { // 오선 좌표 제대로 가져오는지 확인 (Logcat)
                        System.out.println(Arrays.toString(stave));
                    }
                     */
                } else { // 이전에 검출된 오선과 같은 오선
                    staves.get(staves.size() - 1)[1]++; // 높이 업데이트
                    /*
                    for (int[] stave : staves) { // 오선 좌표 제대로 가져오는지 확인 (Logcat)
                        System.out.println("else구문");
                        System.out.println(Arrays.toString(stave));
                    }
                    */
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



    // 4. 정규화
    public static Pair<Mat, List<double[]>> normalization(Mat image, List<int[]> staves, int standard) {
        double avgDistance = 0;
        int lines = staves.size() / 5; // 보표 개수 구하기

        for (int line = 0; line < lines; line++) { // 평균 간격
            for (int staff = 0; staff < 4; staff++) {
                int staffAbove = staves.get(line * 5 + staff)[0];
                int staffBelow = staves.get(line * 5 + staff + 1)[0];
                avgDistance += Math.abs(staffAbove - staffBelow);
            }
        }
        avgDistance /= staves.size() - lines;

        int height = image.rows();
        int width = image.cols();

        double weight = standard / avgDistance; // standard : 오선 간격
        int newWidth = (int) (width * weight);
        int newHeight = (int) (height * weight);

        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new org.opencv.core.Size(newWidth, newHeight));

        resizedImage.convertTo(resizedImage, org.opencv.core.CvType.CV_8UC1);

        Imgproc.threshold(resizedImage, resizedImage, 127, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);


        List<double[]> normalizedStaves = new ArrayList<>();
        for (int[] staff : staves) {
            normalizedStaves.add(new double[]{(staff[0] * weight),(staff[1] * weight)});
        }

        return new Pair<>(resizedImage, normalizedStaves);
    }


    // 5. 객체 검출
    public static Pair<Mat, List<Object[]>> objectDetection(Mat image, List<double[]> staves) {
        int lines = (int) Math.ceil(staves.size() / 5.0);
        List<Object[]> objects = new ArrayList<>();
        Mat closeImage = closing(image);

        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();
        int cnt = Imgproc.connectedComponentsWithStats(closeImage, labels, stats, centroids);

        for(int i = 1; i< cnt; i++){
            int x = (int) stats.get(i, 0)[0];
            int y = (int) stats.get(i, 1)[0];
            int w = (int) stats.get(i, 2)[0];
            int h = (int) stats.get(i, 3)[0];
            int area = (int) stats.get(i, 4)[0];
            if (w >= getWeighted(5) && h >= getWeighted(5)) {
                double center = getCenter(y, h);
                for (int line = 0; line < lines; line++) {
                    double areaTop = staves.get(line * 5)[0] - getWeighted(20);
                    double areaBot = staves.get((line + 1) * 5 - 1)[0] + getWeighted(20);
                    Rect rect = new Rect(x, y, w, h);
                    Imgproc.rectangle(image, rect, new Scalar(255, 0, 0), 1);

                    if (areaTop <= center && center <= areaBot) {
                        objects.add(new Object[]{line, new int[]{x, y, w, h, (int) area}});
                    }
                }
            }

            /*
            // 구성요소들의 넓이
            Point loc1 = new Point(x, y+ h + 30);
            Point loc2 = new Point(x, y+ h + 60);
            put_text(normalizedImage, String.valueOf(w), loc1);
            put_text(normalizedImage, String.valueOf(h), loc2);

             */

}

            /*
            System.out.println("정렬 전");
            // objects 구성요소 확인하기 위한 출력(Logcat)
            for (Object[] obj : objects) {
            System.out.print("[" + obj[0] + ", ");
            int[] intArray = (int[]) obj[1];
            System.out.println(Arrays.toString(intArray) + "]");
            }

            */
        objects.sort(Comparator.comparing((Object[] o) -> (Integer) o[0]).thenComparingInt(o -> ((int[]) o[1])[0]));
        return new Pair<>(image, objects);
    }



    /*

    // 5. 객체 검출 + 표시
    public static dPair<Mat, List<dPair<Integer, Rect>>> objectDetection(Mat image, List<int[]> staves) {
        int lines = (int) Math.ceil(staves.size() / 5.0);
        List<dPair<Integer, Rect>> objects = new ArrayList<>();


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

            Rect testRect = new Rect((int) x, (int) y, (int) w, (int) h);

            if (w >= getWeighted(5) && h >= getWeighted(5)) {
                double center = getCenter(y, h);
                for (int line = 0; line < lines; line++) {
                    double areaTop = staves.get(line * 5)[0] - getWeighted(20);
                    double areaBot = staves.get((line + 1) * 5 - 1)[0] + getWeighted(20);

                    if (areaTop <= center && center <= areaBot) {
                        objects.add(new dPair<>(line, testRect));
                    }
                }
            }
            // 정작 인식한 객체를 그리는 코드가 없더라구요... 아마 그래서 안된게 아닌가 싶습니다 :)
            // image에 양 지점 포인터 영역에 해당하는 컨투어 그리기 (두께 자유)
            Imgproc.rectangle(image, testRect.tl(), testRect.br(), new Scalar(255, 0, 0), 2);
            // test용 출력
            //System.out.print(x);    System.out.print(y);    System.out.print(w);    System.out.println(h);

        }

        Collections.sort(objects, (a, b) -> a.getFirst().compareTo(b.getFirst()));


        return new dPair<>(image, objects);
    }

 */
}
