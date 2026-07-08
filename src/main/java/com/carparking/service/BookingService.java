package com.carparking.service;

import com.carparking.model.Booking;
import com.carparking.model.ParkingSlot;
import com.carparking.model.SlotStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

@Service
public class BookingService {

    private static final Logger log =
            Logger.getLogger(BookingService.class.getName());

    private static final int MAX_ACTIVE_BOOKINGS_PER_USER = 3;

    private final BookingRepository  bookingRepository;
    private final SlotRepository     slotRepository;
    private final SlotBroadcaster    slotBroadcaster;
    private final ParkingSlotService parkingSlotService;
    private final PricingService     pricingService;

    public BookingService(BookingRepository bookingRepository,
                          SlotRepository slotRepository,
                          SlotBroadcaster slotBroadcaster,
                          PricingService pricingService,
                          @Lazy ParkingSlotService parkingSlotService) {
        this.bookingRepository  = bookingRepository;
        this.slotRepository     = slotRepository;
        this.slotBroadcaster    = slotBroadcaster;
        this.pricingService     = pricingService;
        this.parkingSlotService = parkingSlotService;
    }

    @Transactional
    public Map<String, Object> createBooking(Long userId, String slotId,
                                             String vehiclePlate) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (vehiclePlate == null || vehiclePlate.isBlank()) {
                response.put("success", false);
                response.put("message", "Vehicle plate is required");
                return response;
            }

            long activeBookings = bookingRepository.countByUserIdAndStatusIn(
                    userId, List.of("PENDING", "ACTIVE"));
            if (activeBookings >= MAX_ACTIVE_BOOKINGS_PER_USER) {
                response.put("success", false);
                response.put("message", "Booking limit reached. Cancel an existing booking first.");
                return response;
            }

            ParkingSlot slot = slotRepository.findByIdForUpdate(slotId).orElse(null);
            if (slot == null) {
                response.put("success", false);
                response.put("message", "Slot not found");
                return response;
            }
            if (!SlotStatus.FREE.equals(slot.getStatus())) {
                response.put("success", false);
                response.put("message", "Slot is not available");
                return response;
            }

            slot.setStatus(SlotStatus.RESERVED);
            slot.setReservedByDriverId(String.valueOf(userId));
            slot.setReservationExpiresAt(LocalDateTime.now().plusMinutes(15));
            slotRepository.save(slot);
            parkingSlotService.syncSlot(slot);

            Booking booking = new Booking(userId, slotId,
                    slot.getParkingAreaId(), vehiclePlate.trim(),
                    LocalDateTime.now().plusMinutes(15));
            booking.setTotalAmount(pricingService.getParkingFee());
            bookingRepository.save(booking);

            slotBroadcaster.broadcastSlotUpdate(slot);
            log.info("Booking created: user=" + userId + " slot=" + slotId);

            response.put("success",   true);
            response.put("message",   "Slot reserved successfully");
            response.put("bookingId", booking.getId());
            response.put("slotId",    slotId);
            response.put("expiresAt", booking.getExpiresAt().toString());

        } catch (Exception e) {
            log.severe("Booking error: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Unable to complete booking");
        }
        return response;
    }

    public Map<String, Object> cancelBooking(Long bookingId, Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                response.put("success", false);
                response.put("message", "Booking not found");
                return response;
            }
            if (!booking.getUserId().equals(userId)) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return response;
            }
            if (!"PENDING".equals(booking.getStatus())) {
                response.put("success", false);
                response.put("message", "Only pending bookings can be cancelled");
                return response;
            }

            ParkingSlot slot = slotRepository.findById(booking.getSlotId()).orElse(null);
            if (slot != null) {
                String owner = slot.getReservedByDriverId();
                if (owner == null || !owner.equals(String.valueOf(userId))) {
                    response.put("success", false);
                    response.put("message", "Slot is no longer reserved by you");
                    return response;
                }
                slot.setStatus(SlotStatus.FREE);
                slot.setReservedByDriverId(null);
                slot.setReservationExpiresAt(null);
                slotRepository.save(slot);
                parkingSlotService.syncSlot(slot);
                slotBroadcaster.broadcastSlotUpdate(slot);
            }

            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);

            response.put("success", true);
            response.put("message", "Booking cancelled");

        } catch (Exception e) {
            log.severe("Cancel booking error: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Unable to cancel booking");
        }
        return response;
    }

    public List<Booking> getUserBookings(Long userId) {
        return enrichBookings(bookingRepository.findByUserId(userId));
    }

    public List<Booking> getAllBookings() {
        return enrichBookings(bookingRepository.findAll());
    }

    private List<Booking> enrichBookings(List<Booking> bookings) {
        bookings.forEach(b -> slotRepository.findById(b.getSlotId())
                .ifPresent(slot -> b.setParkingAreaName(slot.getParkingAreaName())));
        return bookings;
    }
}
