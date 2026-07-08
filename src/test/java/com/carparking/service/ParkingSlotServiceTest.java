package com.carparking.service;

import com.carparking.model.ParkingSlot;
import com.carparking.model.SlotStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingSlotServiceTest {

    @Mock private SlotRepository     slotRepository;
    @Mock private BookingRepository  bookingRepository;

    private ParkingSlotService service;

    @BeforeEach
    void setUp() {
        service = new ParkingSlotService(slotRepository, bookingRepository, null);
        when(slotRepository.existsById(anyString())).thenReturn(false);
        when(slotRepository.save(any(ParkingSlot.class))).thenAnswer(inv -> inv.getArgument(0));
        service.registerSlot(slot("S-01", -1.2864, 36.8172));
        service.registerSlot(slot("S-02", -1.2865, 36.8173));
        service.registerSlot(slot("S-03", -1.2900, 36.8200));
    }

    @Test
    void register_newSlot_statusIsFree() {
        ParkingSlot s = service.getSlot("S-01");
        assertThat(s.getStatus()).isEqualTo(SlotStatus.FREE);
    }

    @Test
    void register_duplicateSlotId_returnsExisting() {
        ParkingSlot existing = service.getSlot("S-01");
        when(slotRepository.existsById("S-01")).thenReturn(true);
        when(slotRepository.findById("S-01")).thenReturn(Optional.of(existing));
        ParkingSlot result = service.registerSlot(slot("S-01", 0, 0));
        assertThat(result.getSlotId()).isEqualTo("S-01");
    }

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

    @Test
    void release_occupiedSlot_statusReturnsFree() {
        service.reserveSlot("S-01", "driver-42");
        service.occupySlot("S-01", "driver-42");
        service.releaseSlot("S-01");
        assertThat(service.getSlot("S-01").getStatus()).isEqualTo(SlotStatus.FREE);
        assertThat(service.getSlot("S-01").getReservedByDriverId()).isNull();
    }

    @Test
    void findNearest_driverNearCBD_returnsFreeSlots() {
        List<ParkingSlot> results = service.findNearestAvailableSlots(-1.2864, 36.8172, 2.0, 10);
        assertThat(results).hasSize(3);
        assertThat(results).allMatch(ParkingSlot::isFree);
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
        List<ParkingSlot> results = service.findNearestAvailableSlots(-4.0435, 39.6682, 2.0, 10);
        assertThat(results).isEmpty();
    }

    @Test
    void adminSetStatus_forceOccupied() {
        service.adminSetStatus("S-01", SlotStatus.OCCUPIED);
        assertThat(service.getSlot("S-01").getStatus()).isEqualTo(SlotStatus.OCCUPIED);
    }

    @Test
    void getStatusSummary_allFree_countsCorrect() {
        var summary = service.getStatusSummary();
        assertThat(summary.get(SlotStatus.FREE)).isEqualTo(3L);
    }

    @Test
    void getSlot_unknownId_throwsNoSuchElement() {
        assertThatThrownBy(() -> service.getSlot("UNKNOWN"))
                .isInstanceOf(NoSuchElementException.class);
    }

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
