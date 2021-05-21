package com.parking.spring.scheduledtask;

import com.parking.spring.dao.entity.Parking;
import com.parking.spring.dao.repos.ParkingRepo;
import com.parking.spring.recognition.YoloService;
import com.parking.spring.recognition.vision.result.ObjectDetectionResult;
import org.bytedeco.javacv.IPCameraFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ScheduledRecognition {
    private static final Logger log = LoggerFactory.getLogger(ScheduledRecognition.class);

    @Autowired
    private ParkingRepo parkingRepo;

    @Autowired
    private YoloService yoloService;


    @Scheduled(fixedRate = 20000)
    public void getCurrentSlotsState() {
        log.info("UPDATING PARKINGS STARTING...");
        List<Parking> parkings = parkingRepo.findAll();
        for (Parking parking : parkings) {
            if (parking.isConfirmed()) {
                fillData(parking);
            }
        }
    }

    private void fillData(Parking parking) {
        try {
            BufferedImage image = getImage(parking.getUrl());
            List<ObjectDetectionResult> detectionResults = yoloService.recognize(image);
            List<ObjectDetectionResult> results = applyCorrections(detectionResults);


            int freeCount = (int) results.stream().filter(slot -> slot.getClassName().equals("free")).count();
            parking.setFreeSlotsCount(freeCount);
            if(parking.getAllSlotsCount() == 0) {
                parking.setAllSlotsCount(results.size());
            }
            parking.setImage(createImage(results, image));
            parkingRepo.save(parking);
            log.info("UPDATED STATE IN " + parking.getAddress());
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (NullPointerException e) {
            log.error("NULL POINTER - " + e.getMessage());
        }
    }

    private List<ObjectDetectionResult> applyCorrections(List<ObjectDetectionResult> detections) {
        long startTime = System.nanoTime();
        log.info("CORRECTION STARTED");
        List<ObjectDetectionResult> results = filterOut(detections);
        eliminateIntersections(results);
        setAverageYPos(results);
        log.info("CORRECTION ENDED IN " + (startTime - System.nanoTime())/ 1000000 +"ms");
        return results;
    }

    private void eliminateIntersections(List<ObjectDetectionResult> results) {
        results.forEach(r -> {
            List<ObjectDetectionResult> intersections = getIntersections(results, r.getRectangle());
            intersections.forEach(object -> {
                Rectangle intersection = object.getRectangle().intersection(r.getRectangle());
                if (!intersection.isEmpty()) {
                    if (intersection.width > r.getWidth() / 2) {
                        int differ = (int) Math.ceil(intersection.height / 2.0);
                        if (object.getRectangle().y > r.getRectangle().y) {
                            object.setY(object.getY() + differ);
                            object.getRectangle().y = object.getY() + differ;
                            r.setHeight(r.getHeight() - differ);
                            r.getRectangle().height = r.getHeight() - differ;
                        } else {
                            r.setY(r.getY() + differ);
                            r.getRectangle().y = r.getY() + differ;
                            object.setHeight(object.getHeight() - differ);
                            object.getRectangle().height = object.getHeight() - differ;
                        }
                    } else {
                        int differ = (int) Math.ceil(intersection.width / 2.0);
                        if (object.getRectangle().x > r.getRectangle().x) {
                            object.setX(object.getX() + differ);
                            object.getRectangle().x = object.getX() + differ;
                            r.setWidth(r.getWidth() - differ);
                            r.getRectangle().width = r.getWidth() - differ;
                        } else {
                            r.setX(r.getX() + differ);
                            r.getRectangle().x = r.getX() + differ;
                            object.setWidth(object.getWidth() - differ);
                            object.getRectangle().width = object.getWidth() - differ;
                        }
                    }
                }
            });
        });
    }

    private void setAverageYPos(List<ObjectDetectionResult> results) {
        List<ObjectDetectionResult> copy = new ArrayList<>(results);
        while (!copy.isEmpty()) {
            ObjectDetectionResult top = copy.get(0);
            List<ObjectDetectionResult> line = copy.stream().filter(r -> (r.getY() <= top.getY() && r.getY() + r.getHeight() >= top.getY())
                  || (r.getY() >= top.getY() && r.getY() <= top.getY() +top.getHeight() )
            ).collect(Collectors.toList());
            int max = line.stream().mapToInt(ObjectDetectionResult::getHeight).max().getAsInt();
            line = copy.stream().filter(r -> (r.getY() <= top.getY() && r.getY() + max>= top.getY())
                  || (r.getY() >= top.getY() && r.getY() <= top.getY() +max )
            ).collect(Collectors.toList());
            line.forEach(r -> {
                r.setY(top.getY());
                r.getRectangle().y = top.getY();
            });
            copy.removeAll(line);
        }
    }

    private List<ObjectDetectionResult> filterOut(List<ObjectDetectionResult> detections) {
        Set<ObjectDetectionResult> toShow = new HashSet<>();

        while (!detections.isEmpty()) {
            ObjectDetectionResult object = detections.get(0);
            detections.remove(object);
            List<ObjectDetectionResult> intersections = getIntersections(detections, object.getRectangle());

            if (!intersections.isEmpty()) {
                intersections = intersections.stream().filter(i -> {
                    Rectangle r = object.getRectangle().intersection(i.getRectangle());
                    double intr = r.getHeight() * r.getWidth() / (object.getRectangle().getWidth() * object.getRectangle().getHeight());
                    return intr > 0.3 && ((int) intr) != 1;
                }).collect(Collectors.toList());

                if (!intersections.isEmpty()) {
                    detections.removeAll(intersections);
                    Map<String, List<ObjectDetectionResult>> grouped = intersections.stream().collect(Collectors.groupingBy(ObjectDetectionResult::getClassName));
                    //int x = intersections.stream().max(Comparator.comparing(ObjectDetectionResult::getX)).get().getX();
                    //int y = intersections.stream().max(Comparator.comparing(ObjectDetectionResult::getY)).get().getY();
                   // int width = intersections.stream().max(Comparator.comparing(ObjectDetectionResult::getWidth)).get().getWidth();
                   // int height = intersections.stream().max(Comparator.comparing(ObjectDetectionResult::getHeight)).get().getHeight();
                    ObjectDetectionResult res = null;
                    Iterator<Map.Entry<String, List<ObjectDetectionResult>>> i = grouped.entrySet().iterator();
                    List<ObjectDetectionResult> firstList = i.next().getValue();
                    List<ObjectDetectionResult> secondList = new ArrayList<>();
                    if (i.hasNext()) {
                        secondList = i.next().getValue();
                    }
                    //res = firstList.size() > secondList.size() ? firstList.get(0) : secondList.get(0);
                    toShow.add(object);
                    //toShow.add(new ObjectDetectionResult(res.getClassId(), res.getClassName(), res.getConfidence(), x, y, width, height));
                } else {
                    toShow.add(object);
                }
            } else {
                toShow.add(object);
            }
        }
        return new ArrayList<>(toShow);
    }

    List<ObjectDetectionResult> getIntersections(List<ObjectDetectionResult> detections, Rectangle r) {
        return detections.stream().filter(d -> !d.getRectangle().intersection(r).isEmpty()).collect(Collectors.toList());
    }

    private byte[] createImage(List<ObjectDetectionResult> results, BufferedImage image) throws IOException{
        long startTime = System.nanoTime();
        log.info("CREATING IMAGE STARTED");
        int width = (int)(1.1*results.stream().map(e -> e.getX()+e.getWidth()).max(Integer::compareTo).orElseGet(image::getWidth));
        int height = (int)(1.1*results.stream().map(e -> e.getY()+e.getHeight()).max(Integer::compareTo).orElseGet(image::getHeight));


        int carWidth = (int) results.stream().mapToInt(ObjectDetectionResult::getHeight).summaryStatistics().getAverage();
        ImageResizer.resize(carWidth*100*2.5/293.0);
        double ww = 293 * carWidth*2.5/293.0;
        double hh = 556 * carWidth*2.5/293.0;
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        var img = ImageIO.read(  new File("resized.png"));

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.lightGray);
        g2d.fillRect(0, 0, width, height);
        g2d.setStroke(new BasicStroke(4f));
        for (ObjectDetectionResult r : results) {
            boolean free = r.getClassName().equals("free");
            if(free) {
                g2d.setColor(Color.green);
            g2d.draw(new Rectangle2D.Double(r.getX(), r.getY() + (r.getHeight()/2.0),
                    ww,
                    hh));
            } else {
                g2d.drawImage(img, r.getX(), r.getY(), null);
            }
        }
        g2d.dispose();
        log.info("CREATING IMAGE IN " + (startTime - System.nanoTime())/ 1000000 +"ms");
        return encodeToBytes(bufferedImage, "png");
    }

    public static void main(String[] args) {
        try
        {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            var img = ImageIO.read(  classloader.getResourceAsStream("images/car.png"));

            System.out.println(img);
        }
        catch ( IOException exc )
        {
            System.out.println(exc);
        }
    }

    public static byte[] encodeToBytes(BufferedImage image, String type) {
            try {
                ByteArrayOutputStream byteOutStream=new ByteArrayOutputStream();
                ImageIO.write(image, type, byteOutStream);
                return byteOutStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return null;
    }

    private static String encodeToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            Base64.Encoder encoder = Base64.getEncoder();
            imageString = encoder.encodeToString(imageBytes);

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }

    private BufferedImage getImage(String url) throws IOException {
        BufferedImage image;
        if (url.contains(".jpg")) {
            URL urlClass = new URL(url);
            image = ImageIO.read(urlClass);
        } else {
            IPCameraFrameGrabber grabber = new IPCameraFrameGrabber(url, 10, 10, TimeUnit.SECONDS);
            grabber.start();
            image = grabber.grabBufferedImage();
            grabber.stop();
        }

        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return newImage;
    }
}
