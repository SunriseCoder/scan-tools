package utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathUtilsTest {

    @Test
    public void testCalculateDistance() {
        assertEquals(5, MathUtils.calculateDistance(3, 4), 0.0001);
    }
}
