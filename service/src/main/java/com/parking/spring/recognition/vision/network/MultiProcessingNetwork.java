package com.parking.spring.recognition.vision.network;


import com.parking.spring.recognition.vision.result.NetworkResult;
import com.parking.spring.recognition.vision.result.ObjectDetectionResult;
import com.parking.spring.recognition.vision.result.ResultList;
import org.bytedeco.opencv.opencv_core.Mat;
import processing.core.PImage;

public interface MultiProcessingNetwork<R extends NetworkResult> extends NeuralNetwork<R> {
    ResultList<R> runByDetections(PImage image, ResultList<ObjectDetectionResult> detections);

    ResultList<R> runByDetections(Mat frame, ResultList<ObjectDetectionResult> detections);
}
