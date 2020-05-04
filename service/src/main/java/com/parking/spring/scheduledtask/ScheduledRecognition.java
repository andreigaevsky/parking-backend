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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Component
public class ScheduledRecognition {
    private static final Logger log = LoggerFactory.getLogger(ScheduledRecognition.class);

    @Autowired
    private ParkingRepo parkingRepo;

    @Autowired
    private YoloService yoloService;


    @Scheduled(fixedDelay = 60000L)
    public void getCurrentSlotsState() {
        log.info("New Parking Update");
       /* List<Parking> parkings = parkingRepo.findAll();
        log.info("New Parking Update -- "+parkings.size());
        for(Parking parking : parkings) {
            fillData(parking);
        }*/
    }

    private void fillData (Parking parking) {
        try {
            System.out.println("111111111111"+parking.getAddress());
            BufferedImage image = getImage(parking.getUrl());
            System.out.println("111111111111"+parking.getAddress());
            List<ObjectDetectionResult> results = yoloService.recognize(image);
            int freeCount = (int) results.stream().filter(slot -> slot.getClassName().equals("free")).count();
            parking.setFreeSlotsCount(freeCount);
            parking.setAllSlotsCount(results.size());
            System.out.println("2222222222222");
            parkingRepo.save(parking);
            log.info("UPDATED STATE IN "+parking.getAddress());
        }catch (IOException e) {
            System.out.println("33333333333333333333333333");
            log.error(e.getMessage());
        }catch (NullPointerException e){
            System.out.println("444444444444444444444444444444");
            log.error("NULL POINTER - "+e.getMessage());
        }
    }

    private BufferedImage getImage(String url) throws IOException {
        BufferedImage image;
        if(url.contains(".jpg")) {
            URL urlClass = new URL(url);
            image = ImageIO.read(urlClass);
        } else{
            IPCameraFrameGrabber grabber = new IPCameraFrameGrabber(url,-1,-1,null);
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
