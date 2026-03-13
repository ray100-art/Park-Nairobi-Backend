package com.carparking.service;

import com.carparking.model.ParkingSlot;
import com.carparking.model.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}