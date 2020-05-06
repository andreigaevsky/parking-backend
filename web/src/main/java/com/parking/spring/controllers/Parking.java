package com.parking.spring.controllers;

import com.parking.spring.dto.ParkingCreateDto;
import com.parking.spring.dto.ParkingDto;
import com.parking.spring.impl.ParkingService;
import com.parking.spring.recognition.RecService;
import com.parking.spring.recognition.YoloService;
import com.parking.spring.recognition.vision.result.ObjectDetectionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.validation.Valid;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/parking")
public class Parking {

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private YoloService yoloService;


    @GetMapping("/all")
    public ResponseEntity<List<ParkingDto>> getAll() {
        return ResponseEntity.ok(parkingService.getAllParkingInfo());
    }

    @PostMapping("/new")
    public ResponseEntity<ParkingDto> createNew(@RequestBody @Valid ParkingCreateDto newParking) {
        return  ResponseEntity.ok(parkingService.createNew(newParking));
    }

    @GetMapping("/rec")
    public ResponseEntity<List<ObjectDetectionResult>> getRec(@RequestParam String f) throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\"+f));
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return ResponseEntity.ok(yoloService.recognize(newImage));
    }



}
