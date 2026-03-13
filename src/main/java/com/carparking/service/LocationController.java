package com.carparking.service;

import com.carparking.model.ParkingArea;
import com.carparking.model.ParkingSlot;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // ── GET /api/location/nearest-areas ───────────────
    // Find nearest parking areas to driver
    @GetMapping("/nearest-areas")
    public List<ParkingArea> nearestAreas(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "2.0") double radius,
            @RequestParam(defaultValue = "10")  int    max) {
        return locationService.findNearestAreas(lat, lon, radius, max);
    }

    // ── GET /api/location/nearest-slots ───────────────
    // Find nearest FREE slots to driver
    @GetMapping("/nearest-slots")
    public List<ParkingSlot> nearestSlots(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "2.0") double radius,
            @RequestParam(defaultValue = "10")  int    max) {
        return locationService.findNearestSlots(lat, lon, radius, max);
    }

    // ── GET /api/location/distance ────────────────────
    // Calculate distance between two GPS points
    @GetMapping("/distance")
    public double distance(
            @RequestParam double lat1,
            @RequestParam double lon1,
            @RequestParam double lat2,
            @RequestParam double lon2) {
        return locationService.calculateDistance(lat1, lon1, lat2, lon2);
    }
}