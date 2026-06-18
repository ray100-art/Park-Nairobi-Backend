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

    private final BookingRepository  bookingRepository;
    private final SlotRepository     slotRepository;
    private final UserRepository     userRepository;
    private final SlotBroadcaster    slotBroadcaster;
    private final ParkingSlotService parkingSlotService;

    public BookingService(BookingRepository bookingRepository,
                          SlotRepository slotRepository,
                          UserRepository userRepository,
                          SlotBroadcaster slotBroadcaster,
                          @Lazy ParkingSlotService parkingSlotService) {
        this.bookingRepository  = bookingRepository;
        this.slotRepository     = slotRepository;
        this.userRepository     = userRepository;
        this.slotBroadcaster    = slotBroadcaster;
        this.parkingSlotService = parkingSlotService;
    }

    @Transactional
    public Map<String, Object> createBooking(Long userId, String slotId,
                                             String vehiclePlate) {
        Map<String, Object> response = new HashMap<>();
        try {
            ParkingSlot slot = slotRepository.findById(slotId).orElse(null);
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

            // Reserve the slot in DB
            slot.setStatus(SlotStatus.RESERVED);
            slot.setReservedByDriverId(String.valueOf(userId));
            slot.setReservationExpiresAt(LocalDateTime.now().plusMinutes(15));
            slotRepository.save(slot);

            // Keep in-memory map in sync
            parkingSlotService.syncSlot(slot);

            // Create booking record
            Booking booking = new Booking(userId, slotId,
                    slot.getParkingAreaId(), vehiclePlate,
                    LocalDateTime.now().plusMinutes(15));
            bookingRepository.save(booking);

            // Broadcast live update
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
            response.put("message", "Error: " + e.getMessage());
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

            // Free the slot
            ParkingSlot slot = slotRepository.findById(booking.getSlotId())
                    .orElse(null);
            if (slot != null) {
                slot.setStatus(SlotStatus.FREE);
                slot.setReservedByDriverId(null);
                slot.setReservationExpiresAt(null);
                slotRepository.save(slot);

                // 📡 Broadcast live update
                slotBroadcaster.broadcastSlotUpdate(slot);
            }

            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);

            response.put("success", true);
            response.put("message", "Booking cancelled");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
}