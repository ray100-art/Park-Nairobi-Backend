package com.carparking.service;

import com.carparking.model.ParkingSlot;
import com.carparking.model.SensorEvent;
import com.carparking.model.SlotStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class SensorService {

    private final SlotRepository  slotRepository;
    private final SlotBroadcaster slotBroadcaster;

    public SensorService(SlotRepository slotRepository,
                         SlotBroadcaster slotBroadcaster) {
        this.slotRepository  = slotRepository;
        this.slotBroadcaster = slotBroadcaster;
    }

    // Car entered the bay → OCCUPIED
    public Map<String, Object> handleEntry(SensorEvent event) {
        Optional<ParkingSlot> opt = slotRepository.findById(event.getSlotId());
        if (opt.isEmpty()) {
            return Map.of("success", false, "message",
                    "Slot not found: " + event.getSlotId());
        }
        ParkingSlot slot = opt.get();

        if (slot.getStatus() == SlotStatus.OCCUPIED) {
            return Map.of("success", false, "message",
                    "Slot is already OCCUPIED");
        }

        slot.setStatus(SlotStatus.OCCUPIED);
        slotRepository.save(slot);
        slotBroadcaster.broadcastSlotUpdate(slot);

        return Map.of(
                "success", true,
                "message", "Slot " + slot.getSlotId() + " marked as OCCUPIED",
                "slotId",  slot.getSlotId(),
                "status",  "OCCUPIED"
        );
    }

    // Car exited the bay → FREE
    public Map<String, Object> handleExit(SensorEvent event) {
        Optional<ParkingSlot> opt = slotRepository.findById(event.getSlotId());
        if (opt.isEmpty()) {
            return Map.of("success", false, "message",
                    "Slot not found: " + event.getSlotId());
        }
        ParkingSlot slot = opt.get();

        slot.setStatus(SlotStatus.FREE);
        slot.setReservedByDriverId(null);
        slot.setReservationExpiresAt(null);
        slotRepository.save(slot);
        slotBroadcaster.broadcastSlotUpdate(slot);

        return Map.of(
                "success", true,
                "message", "Slot " + slot.getSlotId() + " is now FREE",
                "slotId",  slot.getSlotId(),
                "status",  "FREE"
        );
    }

    // Check current status
    public Map<String, Object> getStatus(String slotId) {
        Optional<ParkingSlot> opt = slotRepository.findById(slotId);
        if (opt.isEmpty()) {
            return Map.of("success", false, "message",
                    "Slot not found: " + slotId);
        }
        ParkingSlot slot = opt.get();
        return Map.of(
                "success", true,
                "slotId",  slot.getSlotId(),
                "status",  slot.getStatus().name(),
                "area",    slot.getParkingAreaName() != null
                        ? slot.getParkingAreaName() : ""
        );
    }
}