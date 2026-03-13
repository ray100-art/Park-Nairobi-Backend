package com.carparking.service;

import com.carparking.algorithm.HaversineUtil;
import com.carparking.model.ParkingArea;
import com.carparking.model.ParkingSlot;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class LocationService {

    private static final Logger log = Logger.getLogger(LocationService.class.getName());

    private static final double DEFAULT_RADIUS_KM  = 2.0;
    private static final int    DEFAULT_MAX_RESULTS = 10;

    private final ParkingAreaRepository areaRepository;
    private final SlotRepository        slotRepository;

    public LocationService(ParkingAreaRepository areaRepository,
                           SlotRepository slotRepository) {
        this.areaRepository = areaRepository;
        this.slotRepository = slotRepository;
    }

    // ── Find nearest parking AREAS to driver ──────────
    public List<ParkingArea> findNearestAreas(double driverLat, double driverLon,
                                              double radiusKm,  int maxResults) {
        log.info("Searching areas near (" + driverLat + ", " + driverLon + ")");
        return areaRepository.findNearestAreas(driverLat, driverLon, radiusKm, maxResults);
    }

    public List<ParkingArea> findNearestAreas(double driverLat, double driverLon) {
        return findNearestAreas(driverLat, driverLon, DEFAULT_RADIUS_KM, DEFAULT_MAX_RESULTS);
    }

    // ── Find nearest FREE SLOTS to driver ─────────────
    public List<ParkingSlot> findNearestSlots(double driverLat, double driverLon,
                                              double radiusKm,  int maxResults) {
        log.info("Searching slots near (" + driverLat + ", " + driverLon + ")");
        List<ParkingSlot> slots = slotRepository
                .findNearestFreeSlots(driverLat, driverLon, radiusKm, maxResults);

        // Annotate each slot with computed distance
        slots.forEach(s -> s.setDistanceKm(
                HaversineUtil.distanceKm(driverLat, driverLon,
                        s.getLatitude(), s.getLongitude())));
        return slots;
    }

    public List<ParkingSlot> findNearestSlots(double driverLat, double driverLon) {
        return findNearestSlots(driverLat, driverLon, DEFAULT_RADIUS_KM, DEFAULT_MAX_RESULTS);
    }

    // ── Calculate distance between two GPS points ─────
    public double calculateDistance(double lat1, double lon1,
                                    double lat2, double lon2) {
        return HaversineUtil.distanceKm(lat1, lon1, lat2, lon2);
    }
}