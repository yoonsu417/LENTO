package com.example.lento;

import android.util.Pair;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Modules {


    /*
    // 노이즈 제거
    public static Mat remove_noise(Mat image){
        Funtions noiseImage = new Funtions();
        Mat afterImage = new Mat();
        afterImage = noiseImage.threshold(image);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(afterImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        // 보표로 추정되는 영역을 표시할 이미지 생성
        Mat extractedImage = Mat.zeros(afterImage.size(), afterImage.type());

        for (MatOfPoint contour : contours) {
            // 컨투어를 감싸는 최소 사각형(rectangle)을 구함
            Rect rect = Imgproc.boundingRect(contour);
            // 최소 사각형의 가로 길이가 이미지 가로 길이의 70% 이상이면 보표로 판단하고 표시할 이미지에 해당 부분을 그림
            if (rect.width >= afterImage.cols() * 0.7 && rect.width != afterImage.cols()) {
                // 보표로 판단된 영역을 표시할 이미지에 그림
                Imgproc.rectangle(extractedImage, rect.tl(), rect.br(), new Scalar(255, 255, 255), -1); // 흰색으로 채움
            }
        }

        // 추출된 보표 영역만을 남기고 나머지 부분을 검정색으로 만듬
        Core.bitwise_and(afterImage, extractedImage, afterImage);

        return afterImage;
    }

    */

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

    // 정규화
    public static Pair<Mat, List<int[]>> normalization(Mat image, List<int[]> staves, int standard) {
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


        List<int[]> normalizedStaves = new ArrayList<>();
        for (int[] staff : staves) {
            normalizedStaves.add(new int[]{(int) (staff[0] * weight), (int) (staff[1] * weight)});
        }

        return new Pair<>(resizedImage, normalizedStaves);
    }

}
