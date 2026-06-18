package com.carparking.service;

import com.carparking.algorithm.HaversineUtil;
import com.carparking.algorithm.SlotComparator;
import com.carparking.model.Booking;
import com.carparking.model.ParkingSlot;
import com.carparking.model.SlotStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ParkingSlotService {

    private static final Logger log = Logger.getLogger(ParkingSlotService.class.getName());

    public static final int    RESERVATION_TIMEOUT_MINUTES = 15;
    public static final double DEFAULT_SEARCH_RADIUS_KM    = 2.0;

    private final ConcurrentHashMap<String, ParkingSlot>   slotRegistry = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> slotLocks    = new ConcurrentHashMap<>();

    private final SlotRepository    slotRepository;
    private final BookingRepository bookingRepository;

    public ParkingSlotService(@Lazy SlotRepository slotRepository,
                              @Lazy BookingRepository bookingRepository) {
        this.slotRepository    = slotRepository;
        this.bookingRepository = bookingRepository;
    }

    // ── Register ──────────────────────────────────────────
    public ParkingSlot registerSlot(ParkingSlot slot) {
        slot.setStatus(SlotStatus.FREE);
        ParkingSlot existing = slotRegistry.putIfAbsent(slot.getSlotId(), slot);
        if (existing != null) {
            log.info("Slot already registered, skipping: " + slot.getSlotId());
            return existing;
        }
        slotLocks.putIfAbsent(slot.getSlotId(), new ReentrantLock());
        log.info("Slot registered: " + slot.getSlotId());
        return slot;
    }

    public void deregisterSlot(String slotId) {
        slotRegistry.remove(slotId);
        slotLocks.remove(slotId);
        log.info("Slot deregistered: " + slotId);
    }

    // ── Queries ───────────────────────────────────────────
    public ParkingSlot getSlot(String slotId) {
        ParkingSlot slot = slotRegistry.get(slotId);
        if (slot == null)
            throw new NoSuchElementException("Slot not found: " + slotId);
        return slot;
    }

    public List<ParkingSlot> getAllSlots() {
        return List.copyOf(slotRegistry.values());
    }

    public Map<SlotStatus, Long> getStatusSummary() {
        return slotRegistry.values().stream()
                .collect(Collectors.groupingBy(ParkingSlot::getStatus, Collectors.counting()));
    }

    // ── Nearest search ────────────────────────────────────
    public List<ParkingSlot> findNearestAvailableSlots(double driverLat, double driverLon,
                                                       double radiusKm, int maxResults) {
        return slotRegistry.values().stream()
                .filter(s -> SlotStatus.FREE.equals(s.getStatus()))
                .filter(s -> HaversineUtil.isWithinRadius(
                        driverLat, driverLon,
                        s.getLatitude(), s.getLongitude(), radiusKm))
                .peek(s -> s.setDistanceKm(
                        HaversineUtil.distanceKm(driverLat, driverLon,
                                s.getLatitude(), s.getLongitude())))
                .sorted(SlotComparator.INSTANCE)
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    public List<ParkingSlot> findNearestAvailableSlots(double lat, double lon) {
        return findNearestAvailableSlots(lat, lon, DEFAULT_SEARCH_RADIUS_KM, 10);
    }

    // ── Reserve ───────────────────────────────────────────
    public ParkingSlot reserveSlot(String slotId, String driverId) {
        ReentrantLock lock = getOrCreateLock(slotId);
        lock.lock();
        try {
            ParkingSlot slot = getSlot(slotId);
            if (!SlotStatus.FREE.equals(slot.getStatus()))
                throw new IllegalStateException("Slot " + slotId + " is not FREE");
            slot.setStatus(SlotStatus.RESERVED);
            slot.setReservedByDriverId(driverId);
            slot.setReservationExpiresAt(
                    LocalDateTime.now().plusMinutes(RESERVATION_TIMEOUT_MINUTES));
            log.info("Slot " + slotId + " reserved by " + driverId);
            return slot;
        } finally { lock.unlock(); }
    }

    // ── Occupy ────────────────────────────────────────────
    public ParkingSlot occupySlot(String slotId, String driverId) {
        ReentrantLock lock = getOrCreateLock(slotId);
        lock.lock();
        try {
            ParkingSlot slot = getSlot(slotId);
            if (!SlotStatus.RESERVED.equals(slot.getStatus()))
                throw new IllegalStateException("Slot " + slotId + " is not RESERVED");
            if (!driverId.equals(slot.getReservedByDriverId()))
                throw new IllegalStateException("Driver " + driverId + " does not hold this reservation");
            slot.setStatus(SlotStatus.OCCUPIED);
            slot.setReservationExpiresAt(null);
            log.info("Slot " + slotId + " occupied by " + driverId);
            return slot;
        } finally { lock.unlock(); }
    }

    // ── Release ───────────────────────────────────────────
    public ParkingSlot releaseSlot(String slotId) {
        ReentrantLock lock = getOrCreateLock(slotId);
        lock.lock();
        try {
            ParkingSlot slot = getSlot(slotId);
            slot.setStatus(SlotStatus.FREE);
            slot.setReservedByDriverId(null);
            slot.setReservationExpiresAt(null);
            slot.setDistanceKm(0);
            log.info("Slot " + slotId + " released");
            return slot;
        } finally { lock.unlock(); }
    }

    // ── Admin Override ────────────────────────────────────
    public ParkingSlot adminSetStatus(String slotId, SlotStatus newStatus) {
        ReentrantLock lock = getOrCreateLock(slotId);
        lock.lock();
        try {
            ParkingSlot slot = getSlot(slotId);
            slot.setStatus(newStatus);
            if (SlotStatus.FREE.equals(newStatus)) {
                slot.setReservedByDriverId(null);
                slot.setReservationExpiresAt(null);
            }
            log.info("Admin set slot " + slotId + " to " + newStatus);
            return slot;
        } finally { lock.unlock(); }
    }

    // ── Auto Expiry ───────────────────────────────────────
    @Scheduled(fixedDelay = 60_000)
    public void expireStaleReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<String> expired = slotRegistry.values().stream()
                .filter(s -> SlotStatus.RESERVED.equals(s.getStatus()))
                .filter(s -> s.getReservationExpiresAt() != null
                        && s.getReservationExpiresAt().isBefore(now))
                .map(ParkingSlot::getSlotId)
                .collect(Collectors.toList());

        expired.forEach(slotId -> {
            log.warning("Reservation expired for slot: " + slotId);
            releaseSlot(slotId);
            // Persist expiry to DB
            slotRepository.findById(slotId).ifPresent(dbSlot -> {
                dbSlot.setStatus(SlotStatus.FREE);
                dbSlot.setReservedByDriverId(null);
                dbSlot.setReservationExpiresAt(null);
                slotRepository.save(dbSlot);
            });
            // Cancel associated pending booking
            bookingRepository.findBySlotId(slotId).stream()
                    .filter(b -> "PENDING".equals(b.getStatus()))
                    .forEach(b -> {
                        b.setStatus("CANCELLED");
                        bookingRepository.save(b);
                    });
        });
    }

    // ── Sync from DB ──────────────────────────────────────
    public void syncSlot(ParkingSlot slot) {
        slot.setDistanceKm(0);
        slotRegistry.put(slot.getSlotId(), slot);
        slotLocks.putIfAbsent(slot.getSlotId(), new ReentrantLock());
    }

    // ── Helper ────────────────────────────────────────────
    private ReentrantLock getOrCreateLock(String slotId) {
        return slotLocks.computeIfAbsent(slotId, k -> new ReentrantLock());
    }
}