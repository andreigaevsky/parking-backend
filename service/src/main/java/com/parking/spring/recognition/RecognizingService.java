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
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class RecognizingService extends PApplet {

    private static String IMAGE_NAME = "image.jpg";

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
        String f = "D:\\downloads\\1.jpg";
        IMAGE_NAME = "D:\\downloads\\1.jpg";
       // BufferedImage image = ImageIO.read(new File(f));

        BufferedImage image = getAndSaveImageFromImageUrl("http://89.250.150.72:90/webcapture.jpg?command=snap&channel=1");
        //"http://80.117.204.186:90/cgi-bin/snapshot.cgi?chn=0&u=admin&p=&q=0"
        //http://64.138.207.98/cgi-bin/camera?resolution=640&amp;quality=1&amp;Language=0&amp;
        //BufferedImage image = getAndSaveImageFromMediaUrl("http://96.56.250.139:8200/mjpg/video.mjpg");
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        //testImage = loadImage(f);
        //VideoCapture camera = new VideoCapture();

        // camera.open("http://94.72.19.56/mjpg/video.mjpg");

        testImage = loadImage(sketchPath(IMAGE_NAME));
        println("creating network...");
        yolo = vision.createYOLOv3();

        println("loading model...");
        yolo.setup();

        yolo.setConfidenceThreshold(0.1f);

        println("inferencing...");
        detections = yolo.run(newImage);
        System.out.println(new Date());
        println("done!");

        float confidenceSum = 0;
        calculateDEt();
        //detections = getIntersections(detections.get(0).getRectangle());
        for (ObjectDetectionResult detection : detections) {
            System.out.println(detection.getClassName() + "\t[" + detection.getConfidence() + "]");

            confidenceSum += detection.getConfidence();
        }
        size(testImage.width, testImage.height);
        println("found " + detections.size() + " objects. avg conf: " + nf(confidenceSum / detections.size(), 0, 2));
    }

    void calculateDEt() {
        Set<ObjectDetectionResult> toShow = new HashSet<>();

        while (!detections.isEmpty()) {
            ObjectDetectionResult object = detections.get(0);
            detections.remove(object);
            List<ObjectDetectionResult> intersections = getIntersections(object.getRectangle());

            if (!intersections.isEmpty()) {
                System.out.println("NEXT OBJECT");
                intersections = intersections.stream().filter(i -> {
                    Rectangle r = object.getRectangle().intersection(i.getRectangle());
                    double intr = r.getHeight() * r.getWidth() / (object.getRectangle().getWidth() * object.getRectangle().getHeight());
                    return intr > 0.3 && ((int) intr) != 1;
                }).collect(Collectors.toList());

                if (!intersections.isEmpty()) {
                    detections.removeAll(intersections);
                    Map<String, List<ObjectDetectionResult>> grouped = intersections.stream().collect(Collectors.groupingBy(ObjectDetectionResult::getClassName));
                    int x = intersections.stream().max(Comparator.comparing(ObjectDetectionResult::getX)).get().getX();
                    int y = intersections.stream().max(Comparator.comparing(ObjectDetectionResult::getY)).get().getY();
                    int width = intersections.stream().max(Comparator.comparing(ObjectDetectionResult::getWidth)).get().getWidth();
                    int height = intersections.stream().max(Comparator.comparing(ObjectDetectionResult::getHeight)).get().getHeight();
                    ObjectDetectionResult res = null;
                    Iterator<Map.Entry<String, List<ObjectDetectionResult>>> i = grouped.entrySet().iterator();
                    List<ObjectDetectionResult> firstList = i.next().getValue();
                    List<ObjectDetectionResult> secondList = new ArrayList<>();
                    if (i.hasNext()) {
                        secondList = i.next().getValue();
                    }
                    res = firstList.size() > secondList.size() ? firstList.get(0) : secondList.get(0);
                    toShow.add(object);
                    //toShow.add(new ObjectDetectionResult(res.getClassId(), res.getClassName(), res.getConfidence(), x, y, width, height));
                } else{
                    toShow.add(object);
                }
            } else{
                toShow.add(object);
            }
        }
        detections = new ArrayList<>(toShow);
    }

    List<ObjectDetectionResult> getIntersections(Rectangle r) {
        return detections.stream().filter(d -> !d.getRectangle().intersection(r).isEmpty()).collect(Collectors.toList());
    }


    private BufferedImage getAndSaveImageFromImageUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage image = ImageIO.read(url);
        File outputfile = new File(IMAGE_NAME);//http://109.236.111.203:90/mjpg/video.mjpg
        ImageIO.write(image, "jpg", outputfile);

        return image;
    }

    private BufferedImage getAndSaveImageFromMediaUrl(String mediaUrl) throws IOException {
        IPCameraFrameGrabber grabber = new IPCameraFrameGrabber(mediaUrl, -1, -1, null);
        grabber.start();
        BufferedImage image = grabber.grabBufferedImage();
        grabber.stop();
        File outputfile = new File(IMAGE_NAME);
        ImageIO.write(image, "jpg", outputfile);

        return image;
    }


    @Override
    public void draw() {
        background(55);
//PImage img = new PImage(testImage.width, testImage.height);

        image(testImage, 0, 0);

        noFill();
        strokeWeight(2f);

        for (ObjectDetectionResult detection : detections) {
            stroke(round(360.0f * (float) detection.getClassId() / yolo.getLabels().size()), 75, 100);
            rect(detection.getX(), detection.getY(), detection.getWidth(), detection.getHeight());

            textSize(15);
            text(detection.getClassName(), detection.getX(), detection.getY());
        }
        saveFrame("ino.jpg");
    }

    private BufferedImage getImage() {
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
                        if (jpgOut != null) {
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
