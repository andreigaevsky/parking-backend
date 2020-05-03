package com.parking.spring.recognition.vision.network;


import com.parking.spring.recognition.vision.result.NetworkResult;
import org.bytedeco.opencv.opencv_core.Mat;

public interface NeuralNetwork<R extends NetworkResult> {
    boolean setup();

    R run(Mat frame);
}
