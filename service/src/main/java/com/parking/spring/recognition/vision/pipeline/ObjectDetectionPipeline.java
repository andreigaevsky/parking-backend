package com.parking.spring.recognition.vision.pipeline;


import com.parking.spring.recognition.vision.network.MultiProcessingNetwork;
import com.parking.spring.recognition.vision.network.ObjectDetectionNetwork;
import com.parking.spring.recognition.vision.result.DetectionPipelineResult;
import com.parking.spring.recognition.vision.result.NetworkResult;
import com.parking.spring.recognition.vision.result.ObjectDetectionResult;
import com.parking.spring.recognition.vision.result.ResultList;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectDetectionPipeline<R extends NetworkResult> extends NetworkPipeline<ResultList<DetectionPipelineResult<R>>> {
    private ObjectDetectionNetwork detectionNetwork;
    private MultiProcessingNetwork<R>[] postProcessors;

    public ObjectDetectionPipeline(ObjectDetectionNetwork detectionNetwork, MultiProcessingNetwork<R>... postProcessors) {
        this.detectionNetwork = detectionNetwork;
        this.postProcessors = postProcessors;
    }

    @Override
    public boolean setup() {
        // todo: implement check or remove it from setup
        detectionNetwork.setup();

        for (MultiProcessingNetwork<R> network : postProcessors) {
            network.setup();
        }

        return true;
    }

    @Override
    public ResultList<DetectionPipelineResult<R>> run(Mat frame) {
        // detect objects
        ResultList<ObjectDetectionResult> detections = detectionNetwork.run(frame);

        // run post processors on detections
        List<ResultList<R>> results = new ArrayList<>();
        for (MultiProcessingNetwork<R> network : postProcessors) {
            results.add(network.runByDetections(frame, detections));
        }

        // create pipeline results
        ResultList<DetectionPipelineResult<R>> pipelineResults = new ResultList<>();

        for (int i = 0; i < detections.size(); i++) {
            final int index = i;
            ObjectDetectionResult detection = detections.get(index);
            ResultList<R> combinedResults = new ResultList<>(results.stream()
                    .map(e -> e.get(index))
                    .collect(Collectors.toList())
            );
            pipelineResults.add(new DetectionPipelineResult<>(detection, combinedResults));
        }

        return pipelineResults;
    }
}
