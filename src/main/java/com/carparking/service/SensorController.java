package com.carparking.service;

import com.carparking.model.SensorEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sensor")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    // POST /api/sensor/entry — car entered bay
    @PostMapping("/entry")
    public ResponseEntity<Map<String, Object>> carEntry(
            @RequestBody SensorEvent event) {
        Map<String, Object> result = sensorService.handleEntry(event);
        boolean success = (boolean) result.get("success");
        return success
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    // POST /api/sensor/exit — car exited bay
    @PostMapping("/exit")
    public ResponseEntity<Map<String, Object>> carExit(
            @RequestBody SensorEvent event) {
        Map<String, Object> result = sensorService.handleExit(event);
        boolean success = (boolean) result.get("success");
        return success
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    // GET /api/sensor/status/{slotId} — check slot status
    @GetMapping("/status/{slotId}")
    public ResponseEntity<Map<String, Object>> getStatus(
            @PathVariable String slotId) {
        Map<String, Object> result = sensorService.getStatus(slotId);
        boolean success = (boolean) result.get("success");
        return success
                ? ResponseEntity.ok(result)
                : ResponseEntity.notFound().build();
    }
}