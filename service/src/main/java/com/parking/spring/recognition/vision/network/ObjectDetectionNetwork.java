package com.parking.spring.recognition.vision.network;


import com.parking.spring.recognition.vision.result.ObjectDetectionResult;
import com.parking.spring.recognition.vision.result.ResultList;

public abstract class ObjectDetectionNetwork extends LabeledNetwork<ResultList<ObjectDetectionResult>> {
    private float confidenceThreshold = 0.5f;

    public float getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(float confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }
}
