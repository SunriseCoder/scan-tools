package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dto.Point;

class GeometryUtilsTest {

    @Test
    public void testDistanceBetweenTwoPoints() {
        // Same Points
        assertEquals(0, GeometryUtils.distanceBetweenTwoPoints(new Point(0, 0), new Point(0, 0)));
        // Horizontal
        assertEquals(1, GeometryUtils.distanceBetweenTwoPoints(new Point(0, 0), new Point(1, 0)));
        // Vertical
        assertEquals(1, GeometryUtils.distanceBetweenTwoPoints(new Point(0, 0), new Point(0, 1)));
        // Custom
        assertEquals(5, GeometryUtils.distanceBetweenTwoPoints(new Point(0, 0), new Point(3, 4)));
    }

    @Test
    public void testDistanceBetweenPointAndLine() {
        // Same coordinates of all points
        assertEquals(0, GeometryUtils.distanceBetweenPointAndLine(new Point(0, 0), new Point(0, 0), new Point(0, 0)));
        // Point is the one of the ends of the Line
        assertEquals(0, GeometryUtils.distanceBetweenPointAndLine(new Point(0, 0), new Point(0, 0), new Point(4, 0)));
        // Point in the middle of the Line
        assertEquals(0, GeometryUtils.distanceBetweenPointAndLine(new Point(2, 0), new Point(0, 0), new Point(4, 0)));
        // Point is near the middle of the Line
        assertEquals(1, GeometryUtils.distanceBetweenPointAndLine(new Point(2, 1), new Point(0, 0), new Point(4, 0)));
        // Same Line, but backwards
        assertEquals(1, GeometryUtils.distanceBetweenPointAndLine(new Point(2, 1), new Point(4, 0), new Point(0, 0)));
    }
}
