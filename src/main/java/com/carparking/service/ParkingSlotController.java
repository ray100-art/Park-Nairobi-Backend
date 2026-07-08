package com.carparking.service;

import com.carparking.algorithm.HaversineUtil;
import com.carparking.model.ParkingSlot;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/slots")
public class ParkingSlotController {

    private final SlotRepository     slotRepository;
    private final ParkingSlotService parkingSlotService;
    private final SlotBroadcaster    slotBroadcaster;

    public ParkingSlotController(SlotRepository slotRepository,
                                 ParkingSlotService parkingSlotService,
                                 SlotBroadcaster slotBroadcaster) {
        this.slotRepository     = slotRepository;
        this.parkingSlotService = parkingSlotService;
        this.slotBroadcaster    = slotBroadcaster;
    }

    // GET /api/slots — all slots (admin + fallback)
    @GetMapping
    public ResponseEntity<List<ParkingSlot>> getAllSlots() {
        return ResponseEntity.ok(slotRepository.findAll());
    }

    // GET /api/slots/nearby?lat=-1.29&lon=36.82&radius=5
    @GetMapping("/nearby")
    public ResponseEntity<List<Map<String, Object>>> getNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5") double radius) {

        List<Map<String, Object>> result = slotRepository.findAll().stream()
                .filter(s -> s.getLatitude() != 0.0 && s.getLongitude() != 0.0)
                .map(s -> {
                    double dist = HaversineUtil.distanceKm(lat, lon, s.getLatitude(), s.getLongitude());
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("slotId",          s.getSlotId());
                    m.put("parkingAreaName", s.getParkingAreaName());
                    m.put("status",          s.getStatus());
                    m.put("floor",           s.getFloor());
                    m.put("latitude",        s.getLatitude());
                    m.put("longitude",       s.getLongitude());
                    m.put("distanceKm",      Math.round(dist * 100.0) / 100.0);
                    return m;
                })
                .filter(m -> (double) m.get("distanceKm") <= radius)
                .sorted(Comparator.comparingDouble(m -> (double) m.get("distanceKm")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // GET /api/slots/summary
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getSummary() {
        return ResponseEntity.ok(
                slotRepository.findAll().stream()
                        .collect(Collectors.groupingBy(
                                s -> s.getStatus().name(),
                                Collectors.counting()
                        ))
        );
    }

    // PUT /api/slots/{slotId}/free
    @PutMapping("/{slotId}/free")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> freeSlot(@PathVariable String slotId) {
        return slotRepository.findById(slotId).map(slot -> {
            slot.setStatus(com.carparking.model.SlotStatus.FREE);
            slot.setReservedByDriverId(null);
            slot.setReservationExpiresAt(null);
            slotRepository.save(slot);
            parkingSlotService.syncSlot(slot);
            slotBroadcaster.broadcastSlotUpdate(slot);
            return ResponseEntity.ok(Map.of("message", "Slot freed", "slotId", slotId));
        }).orElse(ResponseEntity.notFound().build());
    }

}
