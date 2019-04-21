package utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathUtilsTest {

    @Test
    public void testCalculateDistance() {
        assertEquals(5, MathUtils.calculateDistance(3, 4), 0.0001);
    }

    @Test
    public void testCeilToInt() {
        assertEquals(5, MathUtils.ceilToInt(5));
        assertEquals(5, MathUtils.ceilToInt(4.0001));
    }

    @Test
    public void testFloorToInt() {
        assertEquals(5, MathUtils.floorToInt(5));
        assertEquals(5, MathUtils.floorToInt(5.999));
    }

    @Test
    public void testRoundToInt() {
        assertEquals(5, MathUtils.roundToInt((double) 5));
        assertEquals(5, MathUtils.roundToInt((double) 5.3));
        assertEquals(5, MathUtils.roundToInt((double) 4.5));
    }

    @Test
    public void testAdjustValue() {
        assertEquals(10, MathUtils.adjustValue(10, 1, 100));
        assertEquals(10, MathUtils.adjustValue(1, 10, 100));
        assertEquals(100, MathUtils.adjustValue(150, 1, 100));
    }
}
