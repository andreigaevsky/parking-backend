package com.parking.spring.recognition.vision.network;

import org.bytedeco.opencv.opencv_dnn.Net;

public interface NetworkFactory {
    Net createNetwork();
}
