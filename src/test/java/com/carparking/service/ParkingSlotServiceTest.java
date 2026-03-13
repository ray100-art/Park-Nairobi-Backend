package com.carparking.service;

import com.carparking.model.ParkingSlot;
import com.carparking.model.SlotStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

/**
 * EPIC 01 – Unit tests for ParkingSlotService
 */
class ParkingSlotServiceTest {

    private ParkingSlotService service;

    @BeforeEach
    void setUp() {
        service = new ParkingSlotService();
        // Register two test slots near Nairobi CBD
        service.registerSlot(slot("S-01", -1.2864, 36.8172));
        service.registerSlot(slot("S-02", -1.2865, 36.8173));
        service.registerSlot(slot("S-03", -1.2900, 36.8200)); // slightly further
    }

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

    @Test
    void register_newSlot_statusIsFree() {
        ParkingSlot s = service.getSlot("S-01");
        assertThat(s.getStatus()).isEqualTo(SlotStatus.FREE);
    }

    @Test
    void register_duplicateSlotId_throwsException() {
        assertThatThrownBy(() -> service.registerSlot(slot("S-01", 0, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    // ------------------------------------------------------------------
    // Reserve
    // ------------------------------------------------------------------

    @Test
    void reserve_freeSlot_statusBecomesReserved() {
        service.reserveSlot("S-01", "driver-42");
        assertThat(service.getSlot("S-01").getStatus()).isEqualTo(SlotStatus.RESERVED);
        assertThat(service.getSlot("S-01").getReservedByDriverId()).isEqualTo("driver-42");
        assertThat(service.getSlot("S-01").getReservationExpiresAt()).isNotNull();
    }

    @Test
    void reserve_alreadyReservedSlot_throwsIllegalState() {
        service.reserveSlot("S-01", "driver-42");
        assertThatThrownBy(() -> service.reserveSlot("S-01", "driver-99"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not FREE");
    }

    // ------------------------------------------------------------------
    // Occupy
    // ------------------------------------------------------------------

    @Test
    void occupy_reservedSlot_statusBecomesOccupied() {
        service.reserveSlot("S-01", "driver-42");
        service.occupySlot("S-01", "driver-42");
        assertThat(service.getSlot("S-01").getStatus()).isEqualTo(SlotStatus.OCCUPIED);
        assertThat(service.getSlot("S-01").getReservationExpiresAt()).isNull();
    }

    @Test
    void occupy_wrongDriver_throwsIllegalState() {
        service.reserveSlot("S-01", "driver-42");
        assertThatThrownBy(() -> service.occupySlot("S-01", "driver-99"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not hold");
    }

    // ------------------------------------------------------------------
    // Release
    // ------------------------------------------------------------------

    @Test
    void release_occupiedSlot_statusReturnsFree() {
        service.reserveSlot("S-01", "driver-42");
        service.occupySlot("S-01", "driver-42");
        service.releaseSlot("S-01");
        assertThat(service.getSlot("S-01").getStatus()).isEqualTo(SlotStatus.FREE);
        assertThat(service.getSlot("S-01").getReservedByDriverId()).isNull();
    }

    // ------------------------------------------------------------------
    // Find nearest
    // ------------------------------------------------------------------

    @Test
    void findNearest_driverNearCBD_returnsFreeSlots() {
        // Driver is at Nairobi CBD
        List<ParkingSlot> results = service.findNearestAvailableSlots(-1.2864, 36.8172, 2.0, 10);
        assertThat(results).hasSize(3);
        assertThat(results).allMatch(ParkingSlot::isFree);
        // First result should be the closest slot (S-01 or S-02)
        assertThat(results.get(0).getDistanceKm()).isLessThan(results.get(1).getDistanceKm());
    }

    @Test
    void findNearest_reservedSlotsExcluded() {
        service.reserveSlot("S-01", "driver-42");
        List<ParkingSlot> results = service.findNearestAvailableSlots(-1.2864, 36.8172, 2.0, 10);
        assertThat(results).noneMatch(s -> "S-01".equals(s.getSlotId()));
    }

    @Test
    void findNearest_driverFarAway_returnsEmpty() {
        // Driver is in Mombasa – 400 km away
        List<ParkingSlot> results = service.findNearestAvailableSlots(-4.0435, 39.6682, 2.0, 10);
        assertThat(results).isEmpty();
    }

    // ------------------------------------------------------------------
    // Admin override
    // ------------------------------------------------------------------

    @Test
    void adminSetStatus_forceOccupied() {
        service.adminSetStatus("S-01", SlotStatus.OCCUPIED);
        assertThat(service.getSlot("S-01").getStatus()).isEqualTo(SlotStatus.OCCUPIED);
    }

    // ------------------------------------------------------------------
    // Summary
    // ------------------------------------------------------------------

    @Test
    void getStatusSummary_allFree_countsCorrect() {
        var summary = service.getStatusSummary();
        assertThat(summary.get(SlotStatus.FREE)).isEqualTo(3L);
    }

    // ------------------------------------------------------------------
    // Unknown slot
    // ------------------------------------------------------------------

    @Test
    void getSlot_unknownId_throwsNoSuchElement() {
        assertThatThrownBy(() -> service.getSlot("UNKNOWN"))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private ParkingSlot slot(String id, double lat, double lon) {
        return ParkingSlot.builder()
                .slotId(id)
                .label(id)
                .parkingAreaName("Test Lot")
                .latitude(lat)
                .longitude(lon)
                .floor(0)
                .build();
    }
}
