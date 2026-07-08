package com.carparking.service;

import com.carparking.model.ParkingSlot;
import com.carparking.model.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<ParkingSlot, String> {

    // Find slots by status
    List<ParkingSlot> findByStatus(SlotStatus status);

    // Find slots by parking area
    List<ParkingSlot> findByParkingAreaId(Long parkingAreaId);

    // Find nearest FREE slots using Haversine SQL
    @Query(value = """
        SELECT *,
            (6371 * ACOS(
                COS(RADIANS(:lat)) * COS(RADIANS(latitude)) *
                COS(RADIANS(longitude) - RADIANS(:lon)) +
                SIN(RADIANS(:lat)) * SIN(RADIANS(latitude))
            )) AS distance_km
        FROM slots
        WHERE status = 'FREE'
        HAVING distance_km <= :radiusKm
        ORDER BY distance_km ASC
        LIMIT :maxResults
        """, nativeQuery = true)
    List<ParkingSlot> findNearestFreeSlots(@Param("lat")        double lat,
                                           @Param("lon")        double lon,
                                           @Param("radiusKm")   double radiusKm,
                                           @Param("maxResults") int    maxResults);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ParkingSlot s WHERE s.slotId = :slotId")
    Optional<ParkingSlot> findByIdForUpdate(@Param("slotId") String slotId);
}