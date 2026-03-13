package com.carparking.model;

/**
 * EPIC 01 – Parking Slot Algorithm Engine
 *
 * Represents all possible states a parking slot can be in.
 * Used as the value type in the core HashMap<String, SlotStatus>.
 */
public enum SlotStatus {

    /**
     * Slot is empty and available for booking or walk-in.
     */
    FREE,

    /**
     * A vehicle is physically present in the slot
     * (confirmed by sensor or manual admin update).
     */
    OCCUPIED,

    /**
     * A driver has reserved the slot but has not yet arrived.
     * Auto-expires after RESERVATION_TIMEOUT_MINUTES via ScheduledExecutorService.
     */
    RESERVED
}
