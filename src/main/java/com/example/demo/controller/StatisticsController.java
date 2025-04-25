package com.example.demo.controller;

import com.example.demo.dto.dto.StatisticsDTO;
import com.example.demo.service.impl.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/booking")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/statistics/{doctorId}")
    public ResponseEntity<StatisticsDTO> getStatistics(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {
        StatisticsDTO stats = statisticsService.getStatistics(doctorId, startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}
