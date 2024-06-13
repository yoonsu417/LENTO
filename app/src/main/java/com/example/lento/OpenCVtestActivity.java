package com.example.lento;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ImageView;
import android.util.Log;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


public class OpenCVtestActivity {
    private static final String TAG = "TEST_OPEN_CV_ANDROID";

    private List<int[]> beatPitch;

    protected void processImage(Context context, int drawbleResource) {
        // OpenCV 라이브러리 초기화
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV 초기화 실패!");
            return;
        } else {
            Log.d(TAG, "OpenCV 초기화 성공!!!!!");
        }


        // Mat 객체 불러오기 : JPG -> Mat
        Mat image = null;
        try {
            image = Utils.loadResource(context, drawbleResource);
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
            System.out.print(Arrays.toString(stave) + ", ");
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
        Pair<Mat, List<Object[]>> analysis = objectAnalysis(detectionImage, detectionObjects);
        Mat analysisImage = analysis.first;
        List<Object[]> analysisObjects = analysis.second;
        System.out.println("객체 분석 후"); //확인 용도(Logcat)
        System.out.println("방향 포함 출력"); // 확인 용도 (Logcat)
        for (Object[] obj : analysisObjects) {
            int[] stats = (int[]) obj[1];
            int x = stats[0];
            int y = stats[1];
            int w = stats[2];
            int h = stats[3];
            int area = stats[4];

            List<int[]> stems = (List<int[]>) obj[2];
            if (stems.size() > 0) {
                int numberOfStems = stems.size();
                //put_text(analysisImage, String.valueOf(numberOfStems), new Point(x, y + h + 20));

                // 보표
                System.out.print("[" + obj[0] + ", ");

                // 객체
                int[] intArray = (int[]) obj[1];
                System.out.print(Arrays.toString(intArray) + ", ");

                //직선
                List<int[]> stemList = (List<int[]>) obj[2];
                System.out.print("[");
                for (int i = 0; i < stemList.size(); i++) {
                    int[] stem = stemList.get(i);
                    System.out.print(Arrays.toString(stem));
                    if (i < stemList.size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.print("], ");

                // 방향
                boolean direction = (boolean) obj[3];
                System.out.println(direction + "]");
                ;

            }

        }

        // 7. 조표 인식
        RecognitionResult rcResult = recognition(analysisImage, normalizedStaves, analysisObjects);
        Mat recognitionImage = rcResult.getImage();
        beatPitch = rcResult.getBeatPitch();
        //System.out.println(beatPitch);
        System.out.println("key = " + rcResult.getKey());

        /*
        System.out.println("박자, 계이름 출력");
        for (int[] list : beatPitch) {
            System.out.println(Arrays.toString(list));
        }


         */

        /*
        // 비트맵 선언 + Mat 객체 -> 비트맵 변환
        Bitmap Bitmapimage;
        Bitmapimage = Bitmap.createBitmap(imageWithoutStaves.cols(), imageWithoutStaves.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageWithoutStaves, Bitmapimage);


         */

    }

    public List<int[]> getBeatPitchStat() {
        return beatPitch;
    }



    // ---------------------------------------- funtions ------------------------------------------

    private static int getWeighted(int value) {
        int standard = 10;
        return (int) (value * (standard / 10));
    }

    private static double getCenter(int y, int h) {
        return (y + y + h) / 2;
    }

    public static Mat closing(Mat image) {
        Mat kernel = Mat.ones(getWeighted(5), getWeighted(5), CvType.CV_8U);
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_CLOSE, kernel);
        return image;
    }

    public static void put_text(Mat image, String text, Point loc) {
        double fontScale = 0.6;
        int thickness = 2;
        Scalar color = new Scalar(255, 0, 0);
        Imgproc.putText(image, text, loc, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, color, thickness);
    }


    public static List<int[]> getLine(Mat image, boolean axis, int axis_value, int start, int end, int length) {

        List<int[]> points = new ArrayList<>();

        // 수직이면
        if (axis) {
            for (int i = start; i < end; i++) {
                int[] point = {i, axis_value};
                points.add(point);
            }
        } else {
            for (int i = start; i < end; i++) {
                int[] point = {axis_value, i};
                points.add(point);
            }
        }
        int pixels = 0;
        int y = 0;
        int x = 0;
        for (int i = 0; i < points.size(); i++) {
            int[] point = points.get(i);
            y = point[0];
            x = point[1];
            pixels += (image.get(y, x)[0] == 255) ? 1 : 0; //흰색 픽셀 개수

            int next_y = axis ? y + 1 : y;
            int next_x = axis ? x : x + 1;
            int next_point = (int) image.get(next_y, next_x)[0];  // 다음 탐색할 지점

            if (next_point == 0 || i == points.size() - 1) {
                if (pixels >= getWeighted(length)) {
                    break;
                } else {
                    pixels = 0;
                }
            }
        }

        return new ArrayList<>(List.of(new int[]{axis ? y : x, pixels}));
    }


    // 기둥 검출 함수
    public static List<int[]> stemDetection(Mat image, int[] stats, int length) {
        final boolean VERTICAL = true;
        final boolean HORIZONTAL = false;

        int x = stats[0];
        int y = stats[1];
        int w = stats[2];
        int h = stats[3];
        int area = stats[4];

        List<int[]> stems = new ArrayList<>();

        for (int col = x; col < x + w; col++) {
            List<int[]> lineInfo = getLine(image, VERTICAL, col, y, y + h, length);
            int end = lineInfo.get(0)[0];
            int pixels = lineInfo.get(0)[1];

            if (pixels > 0) {
                if (stems.isEmpty() || Math.abs(stems.get(stems.size() - 1)[0] + stems.get(stems.size() - 1)[2] - col) >= 1) {
                    int[] stem = new int[]{col, end - pixels + 1, 1, pixels};
                    stems.add(stem);
                } else {
                    stems.get(stems.size() - 1)[2]++;
                }
            }
        }

        return stems;
    }

    public static int count_rect_pixels(Mat image, int[] rect) {
        int x = rect[0];
        int y = rect[1];
        int w = rect[2];
        int h = rect[3];
        int pixels = 0;
        for (int row = y; row < y + h; row++) {
            for (int col = x; col < x + w; col++) {
                double[] pixelValue = image.get(row, col);
                if (pixelValue[0] == 255) {
                    pixels++;
                }
            }
        }
        return pixels;
    }

    public static int countPixels(Mat image, int top, int bottom, int col){
        int cnt = 0;
        boolean flag = false;

        for(int row = top; row < bottom; row++){
            if(!flag && image.get(row,col)[0] == 255){
                flag = true;
                cnt += 1;
            } else if(flag && image.get(row,col)[0]==0){
                flag = false;
            }
        }

        return cnt;
    }


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
                    staves.add(new int[]{row, 0}); // 오선 추가 [오선의 y 좌표][오선 높이]
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
        return new Pair<>(image, staves);
    }


    // 4. 정규화
    public static Pair<Mat, List<double[]>> normalization(Mat image, List<int[]> staves, int standard) {
        double avgDistance = 0;
        int lines = staves.size() / 5; // 보표 개수 구하기

        for (int line = 0; line < lines; line++) { // 평균 간격
            for (int staff = 0; staff < 4; staff++) {
                int staffAbove = staves.get(line * 5 + staff)[0];
                int staffBelow = staves.get(line * 5 + staff + 1)[0];
                avgDistance += Math.abs(staffAbove - staffBelow); // 절댓값
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
            normalizedStaves.add(new double[]{(staff[0] * weight), (staff[1] * weight)});
        }

        return new Pair<>(resizedImage, normalizedStaves);
    }


    // 5. 객체 검출
    public static Pair<Mat, List<Object[]>> objectDetection(Mat image, List<double[]> staves) {
        int lines = (int) Math.ceil(staves.size() / 5.0);
        List<Object[]> objects = new ArrayList<>();
        //Mat closeImage = closing(image);

        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();
        int cnt = Imgproc.connectedComponentsWithStats(image, labels, stats, centroids);

        for (int i = 1; i < cnt; i++) {
            int x = (int) stats.get(i, 0)[0];
            int y = (int) stats.get(i, 1)[0];
            int w = (int) stats.get(i, 2)[0];
            int h = (int) stats.get(i, 3)[0];
            int area = (int) stats.get(i, 4)[0];
            if (w >= getWeighted(11) && h >= getWeighted(11)) {
                double center = getCenter(y, h);
                for (int line = 0; line < lines; line++) {
                    double areaTop = staves.get(line * 5)[0] - getWeighted(20);
                    double areaBot = staves.get((line + 1) * 5 - 1)[0] + getWeighted(20);
                    //Rect rect = new Rect(x, y, w, h);
                    //Imgproc.rectangle(image, rect, new Scalar(255, 0, 0), 1);

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


    // 6. 객체 분석
    public static Pair<Mat, List<Object[]>> objectAnalysis(Mat image, List<Object[]> objects) {
        List<Object[]> updatedObjects = new ArrayList<>();
        for (Object[] obj : objects) {
            int[] stats = (int[]) obj[1];
            List<int[]> stems = stemDetection(image, stats, 30); // 객체 내의 모든 직선들을 검출함
            Object[] updatedObj = new Object[4];
            System.arraycopy(obj, 0, updatedObj, 0, obj.length); // 이전 객체의 내용을 새로운 배열에 복사
            updatedObj[2] = stems; // 새로운 배열에 직선 리스트를 추가

            boolean direction = false;
            if (stems.size() > 0) { // 직선이 1개 이상 존재함
                if (stems.get(0)[0] - stats[0] >= getWeighted(8)) { // 직선이 나중에 발견되면
                    direction = true; // 정 방향 음표
                } else { // 직선이 일찍 발견되면
                    direction = false; // 역 방향 음표
                }
            }
            updatedObj[3] = direction;
            updatedObjects.add(updatedObj); // 객체 배열에 음표 방향을 추가

        }
        return new Pair<>(image, updatedObjects);
    }


    // 7. 조표 인식
    public static RecognitionResult recognition(Mat image, List<double[]> staves, List<Object[]> objects) {
        int key = 0;
        boolean time_signature = false;
        List<int[]> beats = new ArrayList<>();
        List<int[]> pitches = new ArrayList<>();
        ArrayList<int[]> beatPitch = new ArrayList<>();

        for (int i = 1; i < objects.size(); i++) {
            Object[] obj = objects.get(i);
            int line = (int) obj[0];
            int[] stats = (int[]) obj[1];
            List<int[]> stems = (List<int[]>) obj[2];
            boolean direction = (boolean) obj[3];

            int x = (int) stats[0];
            int y = (int) stats[1];
            int w = (int) stats[2];
            int h = (int) stats[3];
            int area = (int) stats[4];
            double[] staff = new double[5];
            for (int j = line * 5; j < line * 5 + 5; j++) {
                staff[j - line * 5] = staves.get(j)[0];
            }

            if (!time_signature) {
                Object[] result = recognizeKey(image, staff, stats);
                time_signature = (boolean) result[0];
                int temp_key = (int) result[1];
                key += temp_key;
            } else {
                Object[] result = recognize_note(image, staff, stats, stems, direction);
                List<int[]> statList = (List<int[]>) result[0];
                beats = (List<int[]>) result[1];
                pitches = (List<int[]>) result[2];

                /*
                System.out.println("객체 확인");
                for (int[] statArray : statList) {
                    System.out.print(Arrays.toString(statArray) + "객체 끝");
                }
                for (int[] beatArray : beats) {
                    System.out.print(Arrays.toString(beatArray) + "박자");
                }
                for (int[] pitchArray : pitches) {
                    System.out.println(Arrays.toString(pitchArray) + "계이름");
                }

                 */


                if(!beats.isEmpty() && !pitches.isEmpty()){
                    for(int j =0; j<beats.size(); j++){
                        int[] currentBeat = beats.get(j);
                        int[] currentPitch = pitches.get(j);
                        int[] currentStat = statList.get(0);

                        int[] beatPitchPair = new int[7];
                        beatPitchPair[0] = currentBeat[0];
                        beatPitchPair[1] = currentPitch[0];
                        for(int k = 2; k < 7; k++){
                            beatPitchPair[k] = currentStat[k-2];
                        }
                        beatPitch.add(beatPitchPair);
                    }
                }
            }

            //Imgproc.rectangle(image, new Point(x, y), new Point(x + w, y + h), new Scalar(255, 0, 0), 1);
            //put_text(image, String.valueOf(i), new Point(x, y - getWeighted(30)));

        }

        return new RecognitionResult(image, key, beatPitch);
    }


    // --------------------------- recognition Modules --------------------------

    public static Object[] recognizeKey(Mat image, double[] staves, int[] stats) {
        int x = stats[0], y = stats[1], w = stats[2], h = stats[3], area = stats[4];

        boolean tsConditions = (
                (staves[0] + getWeighted(5) >= y && y >= staves[0] - getWeighted(5)) && // 상단 위치 조건
                        (staves[4] + getWeighted(5) >= y + h && y + h >= staves[4] - getWeighted(5)) && // 하단 위치 조건
                        (staves[2] + getWeighted(5) >= getCenter(y, h) && getCenter(y, h) >= staves[2] - getWeighted(5)) && // 중단 위치 조건
                        (getWeighted(18) >= w && w >= getWeighted(10)) && // 넓이 조건
                        (getWeighted(45) >= h && h >= getWeighted(35)) // 높이 조건
        );

        if (tsConditions) { // 박자표 조건이 맞으면
            return new Object[]{true, 0}; // true는 박자표, 0은 조표 없음
        } else { // 박자표가 아니고 조표가 있는 경우
            List<int[]> stems = stemDetection(image, stats, 20); // 음표 기둥 확인 (기둥의 x, y, w, h 배열 반환)
            int key;
            if (stems.get(0)[0] - x >= getWeighted(3)) { // 직선이 나중에 발견되면
                key = 10 * stems.size() / 2; // 샾 (10)
            } else { // 직선이 일찍 발견되면
                key = 100 * stems.size(); // 플랫 (100)
            }

            return new Object[]{false, key}; // false는 박자표 아님, key는 계산된 조표
        }
    }

    public static Object[] recognize_note(Mat image, double[] staff, int[] stats, List<int[]> stems, boolean direction) {
        int x = stats[0];
        int y = stats[1];
        int w = stats[2];
        int h = stats[3];
        int area = stats[4];

        // 넓이, 높이, 픽셀 수 확인 (최소 10, 35, 120)
        //put_text(image, String.valueOf(w), new Point(x, y + h + getWeighted(30)));
        //put_text(image, String.valueOf(h), new Point(x, y + h + getWeighted(70)));
        //put_text(image, String.valueOf(count_rect_pixels(image,new int[]{x,y,w,h})), new Point(x, y + h + getWeighted(95)));

        List<int[]> notes = new ArrayList<>();
        List<int[]> pitches = new ArrayList<>();
        List<int[]> statList = new ArrayList<>();
        statList.add(stats);

        /*
        System.out.println("리스트가 잘 들어갔는지 먼저 확인");
        for(int[] a: statList){
            for (int value : a) {
                System.out.print(value + " ");
            }
            System.out.println();
        }

         */
        boolean noteCondition = (
                !stems.isEmpty() &&
                        w >= getWeighted(12) &&  // 넓이 조건
                        h >= getWeighted(30) &&  // 높이 조건
                        area >= getWeighted(120)  // 픽셀 갯수 조건
        );

        if (noteCondition) {
            for(int i = 0; i< stems.size(); i++){
                int[] stem = stems.get(i);
                NoteHeadResult rcResult = recongnize_note_head(image, stem, direction);
                boolean headExist = rcResult.isHeadExist();
                boolean headFill = rcResult.isHeadFill();
                int headCenter = rcResult.getHeadCenter();

                if(headExist){
                    int tail = tailRecognize(image, i, stem, direction);
                    boolean dot = dotRecognize(image, stem, direction, stems.size(), tail);

                    List<Integer> noteClassification = new ArrayList<>();
                    noteClassification.add(!headFill && tail == 0 && !dot ? 2 : 0);
                    noteClassification.add(!headFill && tail == 0 && dot ? -2 : 0);
                    noteClassification.add(headFill && tail == 0 && !dot ? 4 : 0);
                    noteClassification.add(headFill && tail == 0 && dot ? -4 : 0);
                    noteClassification.add(headFill && tail == 1 && !dot ? 8 : 0);
                    noteClassification.add(headFill && tail == 1 && dot ? -8 : 0);
                    noteClassification.add(headFill && tail == 2 && !dot ? 16 : 0);
                    noteClassification.add(headFill && tail == 2 && dot ? -16 : 0);
                    noteClassification.add(headFill && tail == 3 && !dot ? 32 : 0);
                    noteClassification.add(headFill && tail == 3 && dot ? -32 : 0);

                    for (int note : noteClassification) {
                        if (note != 0) {
                            int pitch = pitchRecognize(image, staff, headCenter);
                            notes.add(new int[]{note});
                            pitches.add(new int[]{pitch});

                            //put_text(image, String.valueOf(note), new Point(stem[0] - getWeighted(10), stem[1] + stem[3] + getWeighted(30)));
                            //put_text(image, String.valueOf(pitch), new Point(stem[0] - getWeighted(10), stem[1] + stem[3] + getWeighted(30)));
                            break;
                        }
                    }
                }
            }
        }


        return new Object[] {statList, notes, pitches};
    }

    public static NoteHeadResult recongnize_note_head(Mat image, int[] stem, boolean direction) {
        final boolean VERTICAL = true;
        final boolean HORIZONTAL = false;

        int x = stem[0];
        int y = stem[1];
        int w = stem[2];
        int h = stem[3];

        int areaTop, areaBot, areaLeft, areaRight;

        if (direction) {
            areaTop = y + h - getWeighted(15);  // 음표 머리를 탐색할 위치 (상단)
            areaBot = y + h + getWeighted(7);  // 음표 머리를 탐색할 위치 (하단)
            areaLeft = x - getWeighted(11);  // 음표 머리를 탐색할 위치 (좌측)
            areaRight = x + getWeighted(4);  // 음표 머리를 탐색할 위치 (우측)
        } else {  // 역 방향 음표
            areaTop = y - getWeighted(9);  // 음표 머리를 탐색할 위치 (상단)
            areaBot = y + getWeighted(11);  // 음표 머리를 탐색할 위치 (하단)
            areaLeft = x + w - getWeighted(2);  // 음표 머리를 탐색할 위치 (좌측)
            areaRight = x + w + getWeighted(9);  // 음표 머리를 탐색할 위치 (우측)
        }

        //Imgproc.rectangle(image, new Point(areaLeft, areaTop), new Point(areaRight, areaBot), new Scalar(255, 0, 0), 1);

        int cnt = 0;
        int cntMax = 0;
        int headCenter = 0;
        int pixelCnt = count_rect_pixels(image, new int[]{areaLeft, areaTop, areaRight - areaLeft, areaBot - areaTop});


        for (int row = areaTop; row < areaBot; row++) {
            List<int[]> lineInfo = getLine(image, HORIZONTAL, row, areaLeft, areaRight, 5);
            int end = lineInfo.get(0)[0];
            int pixels = lineInfo.get(0)[1];
            pixels += 1;

            if (pixels > 5) {
                cnt += 1;
                cntMax = Math.max(cntMax, pixels);
                headCenter += row;
            }
        }

        //put_text(image, String.valueOf(cnt), new Point(x - getWeighted(10), y + h + getWeighted(30)));
        //put_text(image, String.valueOf(cntMax), new Point(x - getWeighted(10), y + h + getWeighted(60)));
        //put_text(image, String.valueOf(pixelCnt), new Point(x - getWeighted(10), y + h + getWeighted(90)));


        boolean headExist = (cnt >= 3 && pixelCnt >= 50);
        boolean headFill;
        if(direction){
            headFill = (cnt >= 8 && cntMax >= 9 && pixelCnt > 149);
        } else{
            headFill = (cnt >= 8 && cntMax >= 9 && pixelCnt > 115);
        }

        if (cnt != 0) {
            headCenter /= cnt;
        } else {
            headCenter = 0;
        }

        //put_text(image, String.valueOf(headCenter), new Point(x, y + h+ getWeighted(80)));

        return new NoteHeadResult(headExist, headFill, headCenter);

    }

    public static int tailRecognize(Mat image, int i, int[] stem, boolean direction){
        int x = stem[0];
        int y = stem[1];
        int w = stem[2];
        int h = stem[3];

        int top, bottom, col;

        if (direction) {  // 정 방향 음표
            top = y;  // 음표 꼬리를 탐색할 위치 (상단)
            bottom = y + h - getWeighted(20);  // 음표 꼬리를 탐색할 위치 (하단)
        } else {  // 역 방향 음표
            top = y + getWeighted(15);  // 음표 꼬리를 탐색할 위치 (상단)
            bottom = y + h;  // 음표 꼬리를 탐색할 위치 (하단)
        }

        if (i != 0) {
            col = x - getWeighted(4);  // 음표 꼬리를 탐색할 위치 (열)
        } else {
            col = x + w + getWeighted(4);  // 음표 꼬리를 탐색할 위치 (열)
        }

        if (i>0){
            col = x - getWeighted(8);
        } else {
            col = x + w + getWeighted(8);
        }

        int cnt = 0;

        cnt = countPixels(image, top, bottom, col);

        //put_text(image, String.valueOf(cnt), new Point(x - getWeighted(10), y + h + getWeighted(20)));

        return cnt;
    }

    public static boolean dotRecognize(Mat image, int[] stem, boolean direction, int tail_cnt, int stems_cnt){
        int x = stem[0];
        int y = stem[1];
        int w = stem[2];
        int h = stem[3];

        int top, bottom, left, right;

        if(direction){
            top = y + h - getWeighted(5);
            bottom = y + h - getWeighted(2);
            left = x + w + getWeighted(2);
            right = x + w + getWeighted(12);
        } else {
            top = y - getWeighted(10);
            bottom = y + getWeighted(5);
            left = x + w + getWeighted(14);
            right = x + w + getWeighted(24);
        }


        //Rect rect = new Rect(left, top, right - left, bottom - top);
        int pixels = count_rect_pixels(image, new int[]{left, top, right- left, bottom-top});
        //put_text(image, String.valueOf(pixels), new Point(x, y + h+ getWeighted(20)));
        //Imgproc.rectangle(image, rect, new Scalar(255, 0, 0), 1);

        if (direction && stems_cnt == 1){
            return pixels >= getWeighted(20);
        }else if(!direction && stems_cnt == 1){
            return pixels >= getWeighted(20);
        } else{
            return pixels >= getWeighted(5);
        }

    }

    public static int pitchRecognize(Mat image, double[] staff, int head_center){
        List<Integer> pitches = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            int line = (int) (staff[4] + getWeighted(40) - getWeighted(5) * i);
            pitches.add(line);
        }

        for(int i = 0; i<pitches.size(); i++){
            int line = pitches.get(i);
            if(line + getWeighted(3) > head_center && head_center >= line - getWeighted(2)){
                return i;
            }
        }
        return -1;
    }

    // return 값 4개 처리 위한 클래스
    public static class RecognitionResult {
        private Mat image;
        private int key;
        private List<int[]> beatPitch = new ArrayList<>();

        public RecognitionResult(Mat image, int key, List<int[]> beatPitch) {
            this.image = image;
            this.key = key;
            this.beatPitch = beatPitch;
        }

        public Mat getImage() {
            return image;
        }

        public int getKey() {
            return key;
        }

        public List<int[]> getBeatPitch() {
            return beatPitch;
        }


    }


    // return 값 3개 처리 클래스
    public static class NoteHeadResult {
        boolean headExist;
        boolean headFill;
        int headCenter;

        public NoteHeadResult(boolean headExist, boolean headFill, int headCenter) {
            this.headExist = headExist;
            this.headFill = headFill;
            this.headCenter = headCenter;
        }

        public boolean isHeadExist() {
            return headExist;
        }

        public boolean isHeadFill() {
            return headFill;
        }

        public int getHeadCenter() {
            return headCenter;
        }
    }
}