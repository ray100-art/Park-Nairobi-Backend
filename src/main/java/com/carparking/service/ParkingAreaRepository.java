package com.carparking.service;

import com.carparking.model.ParkingArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingAreaRepository extends JpaRepository<ParkingArea, Long> {

    // Find all active parking areas
    List<ParkingArea> findByActiveTrue();

    // Find nearest parking areas to driver using Haversine SQL
    @Query(value = """
        SELECT *,
            (6371 * ACOS(
                COS(RADIANS(:lat)) * COS(RADIANS(latitude)) *
                COS(RADIANS(longitude) - RADIANS(:lon)) +
                SIN(RADIANS(:lat)) * SIN(RADIANS(latitude))
            )) AS distance
        FROM parking_areas
        WHERE active = TRUE
        HAVING distance <= :radiusKm
        ORDER BY distance ASC
        LIMIT :maxResults
        """, nativeQuery = true)
    List<ParkingArea> findNearestAreas(@Param("lat")        double lat,
                                       @Param("lon")        double lon,
                                       @Param("radiusKm")   double radiusKm,
                                       @Param("maxResults") int    maxResults);
}