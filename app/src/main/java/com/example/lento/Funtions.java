package com.example.lento;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Funtions {

    public static Mat closing(Mat image) {
        int kernelSize = 5;
        Mat kernel = Mat.ones(kernelSize, kernelSize, CvType.CV_8U);
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_CLOSE, kernel);
        return image;
    }

}


