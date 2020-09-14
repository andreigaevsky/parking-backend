package com.parking.spring.recognition;


import com.parking.spring.recognition.vision.DeepVision;
import com.parking.spring.recognition.vision.YOLONetwork;
import com.parking.spring.recognition.vision.result.ObjectDetectionResult;
import lombok.SneakyThrows;
import org.bytedeco.javacv.IPCameraFrameGrabber;
import processing.core.PApplet;
import processing.core.PImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;


public class RecognizingService extends PApplet {

    public static void main(String... args) {
        RecognizingService sketch = new RecognizingService();
      sketch.runSketch();
    }

    PImage testImage;

    DeepVision vision = new DeepVision(this);
    YOLONetwork yolo;
    List<ObjectDetectionResult> detections;

    @SneakyThrows
    @Override
    public void setup() {
        colorMode(HSB, 360, 100, 100);

        System.out.println(new Date());
       /* String f = "D:\\best.jpg";
      BufferedImage image = ImageIO.read(new File(f));*/

      /* URL url = new URL("http://89.250.150.72:90/webcapture.jpg?command=snap&channel=1");
        BufferedImage image = ImageIO.read(url);
        File outputfile = new File("image.jpg");http://109.236.111.203:90/mjpg/video.mjpg
        ImageIO.write(image, "jpg", outputfile);*/

        IPCameraFrameGrabber grabber = new IPCameraFrameGrabber("http://109.236.111.203:90/mjpg/video.mjpg",-1,-1,null);
       grabber.start();
       BufferedImage image = grabber.grabBufferedImage();
       grabber.stop();
        File outputfile = new File("image.jpg");
        ImageIO.write(image, "jpg", outputfile);
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);


        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        //testImage = loadImage(f);
        //VideoCapture camera = new VideoCapture();

       // camera.open("http://94.72.19.56/mjpg/video.mjpg");

       testImage = loadImage(sketchPath("image.jpg"));
        println("creating network...");
        yolo = vision.createYOLOv3();

        println("loading model...");
        yolo.setup();

        yolo.setConfidenceThreshold(0.3f);

        println("inferencing...");
       detections = yolo.run(newImage);
        System.out.println(new Date());
        println("done!");

        float confidenceSum = 0;
        for (ObjectDetectionResult detection : detections) {
            System.out.println(detection.getClassName() + "\t[" + detection.getConfidence() + "]");
            confidenceSum += detection.getConfidence();
        }
        size(testImage.width, testImage.height);
        println("found " + detections.size() + " objects. avg conf: " + nf(confidenceSum / detections.size(), 0, 2));
    }


    @Override
    public void draw() {
        background(55);

        image(testImage, 0, 0);

        noFill();
        strokeWeight(2f);

        for (ObjectDetectionResult detection : detections) {
            stroke(round(360.0f * (float) detection.getClassId() / yolo.getLabels().size()), 75, 100);
            rect(detection.getX(), detection.getY(), detection.getWidth(), detection.getHeight());

            textSize(15);
            text(detection.getClassName(), detection.getX(), detection.getY());
        }

    }


    private BufferedImage getImage(){
        byte[] curFrame = new byte[0];
        boolean frameAvailable = false;
        HttpURLConnection conn;
        BufferedInputStream httpIn;
        ByteArrayOutputStream jpgOut = null;
        try {
            URL url = new URL("http://94.72.19.56/mjpg/video.mjpg");
            conn = (HttpURLConnection) url.openConnection();
            httpIn = new BufferedInputStream(conn.getInputStream(), 8192);

            int prev = 0;
            int cur = 0;

            try {
                while ((cur = httpIn.read()) >= 0 && !frameAvailable) {
                    if (prev == 0xFF && cur == 0xD8) {
                        jpgOut = new ByteArrayOutputStream(8192);
                        jpgOut.write((byte) prev);
                        jpgOut.write((byte) cur);
                    }

                    if (prev == 0xFF && cur == 0xD9) {
                        if(jpgOut != null) {
                            curFrame = jpgOut.toByteArray();
                            frameAvailable = true;
                            jpgOut.close();
                        }
                    }
                    prev = cur;
                }
            } catch (IOException e) {
                System.err.println("I/O Error: " + e.getMessage());
            }

            ByteArrayInputStream jpgIn = new ByteArrayInputStream(curFrame);
            return ImageIO.read(jpgIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



}
