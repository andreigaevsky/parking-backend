package com.parking.spring.recognition;

import org.bytedeco.javacv.IPCameraFrameGrabber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;


@Service
public class RecService {

    @Autowired
    private YoloService yoloService;


    private BufferedImage GrabImage(String url) throws IOException {
        IPCameraFrameGrabber grabber = new IPCameraFrameGrabber("http://94.72.19.56/mjpg/video.mjpg", -1, -1, null);
        grabber.start();
        BufferedImage image = grabber.grabBufferedImage();
        grabber.stop();
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return newImage;
    }


}
