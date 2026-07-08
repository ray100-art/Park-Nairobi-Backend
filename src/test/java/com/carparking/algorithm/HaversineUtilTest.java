package com.carparking.algorithm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * EPIC 01 – Unit tests for HaversineUtil
 */
class HaversineUtilTest {

    /** Known distance: Nairobi CBD to Westlands ≈ 2.3 km */
    @Test
    void distance_NairobiCBD_to_Westlands() {
        double dist = HaversineUtil.distanceKm(
                -1.2864, 36.8172,   // Nairobi CBD
                -1.2673, 36.8094);  // Westlands
        assertThat(dist).isCloseTo(2.29, within(0.5));
    }

    /** Same point should return 0 */
    @Test
    void distance_samePoint_returnsZero() {
        double dist = HaversineUtil.distanceKm(-1.2864, 36.8172, -1.2864, 36.8172);
        assertThat(dist).isEqualTo(0.0);
    }

    /** Within-radius check for a very close point */
    @Test
    void isWithinRadius_closePoint_returnsTrue() {
        boolean result = HaversineUtil.isWithinRadius(
                -1.2864, 36.8172,
                -1.2865, 36.8173,
                0.1);  // 100 metres
        assertThat(result).isTrue();
    }

    /** Within-radius check for a far point */
    @Test
    void isWithinRadius_farPoint_returnsFalse() {
        boolean result = HaversineUtil.isWithinRadius(
                -1.2864, 36.8172,
                -1.2673, 36.8094,
                1.0);  // 1 km – Westlands is ~2.3 km away
        assertThat(result).isFalse();
    }
}
