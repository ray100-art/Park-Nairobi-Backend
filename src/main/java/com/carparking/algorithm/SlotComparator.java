package com.carparking.algorithm;

import com.carparking.model.ParkingSlot;
import com.carparking.model.SlotStatus;

import java.util.Comparator;

/**
 * EPIC 01 – Parking Slot Algorithm Engine
 *
 * Java Comparator that sorts parking slots for presentation to a driver.
 *
 * Ordering rules (applied in sequence):
 *   1. FREE slots first, then RESERVED, then OCCUPIED  (status priority)
 *   2. Within the same status group, nearest slot first (distance ascending)
 *   3. Tie-break: alphabetical by slotId                (stable ordering)
 */
public class SlotComparator implements Comparator<ParkingSlot> {

    /** Singleton instance – stateless, safe to share */
    public static final SlotComparator INSTANCE = new SlotComparator();

    @Override
    public int compare(ParkingSlot a, ParkingSlot b) {
        // 1. Status priority: FREE < RESERVED < OCCUPIED
        int statusCompare = statusPriority(a.getStatus()) - statusPriority(b.getStatus());
        if (statusCompare != 0) return statusCompare;

        // 2. Distance ascending (nearest first)
        int distCompare = Double.compare(a.getDistanceKm(), b.getDistanceKm());
        if (distCompare != 0) return distCompare;

        // 3. Stable tie-break
        return a.getSlotId().compareTo(b.getSlotId());
    }

    /**
     * Maps SlotStatus to a sort priority integer.
     * Lower number = higher priority (appears first in list).
     */
    private int statusPriority(SlotStatus status) {
        return switch (status) {
            case FREE     -> 0;
            case RESERVED -> 1;
            case OCCUPIED -> 2;
        };
    }
}
