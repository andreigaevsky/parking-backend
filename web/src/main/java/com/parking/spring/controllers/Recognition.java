package com.parking.spring.controllers;

import com.parking.spring.dto.ParkingDto;
import com.parking.spring.recognition.YoloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recognition")
public class Recognition {

    @Autowired
    private YoloService yoloService;

    @PostMapping
    public ResponseEntity<String> setThreshold(@RequestParam float threshold) {
        yoloService.setConfidenceThreshold(threshold);
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/threshold")
    public ResponseEntity<String> setThreshold() {
        return ResponseEntity.ok(Float.toString(yoloService.getConfidenceThreshold()));
    }

}
