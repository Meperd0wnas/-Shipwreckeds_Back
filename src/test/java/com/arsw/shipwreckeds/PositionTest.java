package com.arsw.shipwreckeds;

import com.arsw.shipwreckeds.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Position model class.
 * Tests constructor, getters/setters, and all public methods.
 */
class PositionTest {

    private Position position;

    @BeforeEach
    void setUp() {
        position = new Position(10.0, 20.0);
    }

    @Test
    void testConstructor_createsPositionWithCorrectValues() {
        assertEquals(10.0, position.getX());
        assertEquals(20.0, position.getY());
    }

    @Test
    void testSetterAndGetter_x() {
        position.setX(50.0);
        assertEquals(50.0, position.getX());
    }

    @Test
    void testSetterAndGetter_y() {
        position.setY(60.0);
        assertEquals(60.0, position.getY());
    }

    @Test
    void testMoveTo_updatesCoordinates() {
        position.moveTo(100.0, 200.0);
        assertEquals(100.0, position.getX());
        assertEquals(200.0, position.getY());
    }

    @Test
    void testToString_formatsCorrectly() {
        String result = position.toString();
        assertEquals("(10.0, 20.0)", result);
    }

    @Test
    void testToString_withZeroCoordinates() {
        Position zeroPos = new Position(0.0, 0.0);
        assertEquals("(0.0, 0.0)", zeroPos.toString());
    }

    @Test
    void testToString_withNegativeCoordinates() {
        Position negPos = new Position(-15.5, -25.5);
        assertEquals("(-15.5, -25.5)", negPos.toString());
    }

    @Test
    void testMoveToNegativeCoordinates() {
        position.moveTo(-100.0, -200.0);
        assertEquals(-100.0, position.getX());
        assertEquals(-200.0, position.getY());
    }

    @Test
    void testPositionWithLargeCoordinates() {
        Position largePos = new Position(Double.MAX_VALUE / 2, Double.MAX_VALUE / 2);
        assertEquals(Double.MAX_VALUE / 2, largePos.getX());
        assertEquals(Double.MAX_VALUE / 2, largePos.getY());
    }

    @Test
    void testDataAnnotations_equalsAndHashCode() {
        Position pos1 = new Position(10.0, 20.0);
        Position pos2 = new Position(10.0, 20.0);
        assertEquals(pos1, pos2);
        assertEquals(pos1.hashCode(), pos2.hashCode());
    }

    @Test
    void testDataAnnotations_notEqual() {
        Position pos1 = new Position(10.0, 20.0);
        Position pos2 = new Position(10.0, 25.0);
        assertNotEquals(pos1, pos2);
    }

    @Test
    void testSequentialMoves() {
        position.moveTo(5.0, 5.0);
        assertEquals(5.0, position.getX());
        assertEquals(5.0, position.getY());
        
        position.moveTo(15.0, 25.0);
        assertEquals(15.0, position.getX());
        assertEquals(25.0, position.getY());
    }
}
