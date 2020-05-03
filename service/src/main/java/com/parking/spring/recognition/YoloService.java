package com.parking.spring.recognition;

import com.parking.spring.recognition.vision.YOLONetwork;
import com.parking.spring.recognition.vision.result.ObjectDetectionResult;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class YoloService {

    public static final int SIZE = 608;
    public static final Path model = Paths.get ("networks\\yolov3.cfg");
    public static final Path weight = Paths.get("networks\\yolov3.weights");
    public static final Path names = Paths.get("networks\\coco.names");


    private YOLONetwork yolo;

    public YoloService() {

        yolo =  new YOLONetwork(
                model,
                weight,
                SIZE, SIZE
        );
        yolo.loadLabels(names);
        yolo.setConfidenceThreshold(0.35f);
        yolo.setup();
    }

    public void setConfidenceThreshold(float num) {
        if(num > 0 && num < 1){
            yolo.setConfidenceThreshold(num);
        }
    }

    public float getConfidenceThreshold() {
        return yolo.getConfidenceThreshold();
    }

    public List<ObjectDetectionResult> recognize(BufferedImage image) {

        /*List<ObjectDetectionResult> detections = yolo.run(image);

        float confidenceSum = 0;
        for (ObjectDetectionResult detection : detections) {
            System.out.println(detection.getClassName() + "\t[" + detection.getConfidence() + "]");
            confidenceSum += detection.getConfidence();
        }*/
       return yolo.run(image);
    }

}
