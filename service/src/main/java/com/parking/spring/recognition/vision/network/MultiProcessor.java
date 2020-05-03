package com.parking.spring.recognition.vision.network;


import com.parking.spring.recognition.vision.result.NetworkResult;
import com.parking.spring.recognition.vision.result.ObjectDetectionResult;
import com.parking.spring.recognition.vision.result.ResultList;
import org.bytedeco.opencv.opencv_core.Mat;
import processing.core.PImage;

import static com.parking.spring.recognition.vision.util.CvProcessingUtils.createValidROI;


public class MultiProcessor<R extends NetworkResult> implements MultiProcessingNetwork<R> {
    BaseNeuralNetwork<R> network;

    public MultiProcessor(BaseNeuralNetwork<R> network) {
        this.network = network;
    }

    @Override
    public ResultList<R> runByDetections(PImage image, ResultList<ObjectDetectionResult> detections) {
       // Mat frame = network.convertToMat(image);
        return null;
    }

    @Override
    public ResultList<R> runByDetections(Mat frame, ResultList<ObjectDetectionResult> detections) {
        ResultList<R> results = new ResultList<>();

        for (ObjectDetectionResult detection : detections) {
            Mat roi = new Mat(frame, createValidROI(frame.size(), detection.getX(), detection.getY(), detection.getWidth(), detection.getHeight()));
            R result = network.run(roi);
            results.add(result);
            roi.release();
        }

        return results;
    }

    @Override
    public boolean setup() {
        return network.setup();
    }

    @Override
    public R run(Mat frame) {
        return network.run(frame);
    }
}
