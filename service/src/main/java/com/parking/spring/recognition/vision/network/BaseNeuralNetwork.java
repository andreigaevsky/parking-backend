package com.parking.spring.recognition.vision.network;


import com.parking.spring.recognition.vision.result.NetworkResult;
import com.parking.spring.recognition.vision.util.CvProcessingUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import processing.core.PImage;

import java.awt.image.BufferedImage;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC4;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_RGBA2RGB;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

public abstract class BaseNeuralNetwork<R extends NetworkResult> implements NeuralNetwork<R> {

    public abstract boolean setup();

    public R run(BufferedImage image) {
        // prepare frame
        Mat frame = convertToMat(image);

        // inference
        return run(frame);
    }

    public abstract R run(Mat frame);

    protected Mat convertToMat(BufferedImage image) {
        Mat frame = new Mat(image.getHeight(), image.getWidth(), CV_8UC4);
        CvProcessingUtils.toCv(image, frame);
        cvtColor(frame, frame, COLOR_RGBA2RGB);
        return frame;
    }
}
