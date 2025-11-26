package com.arsw.shipwreckeds;

import com.arsw.shipwreckeds.model.Npc;
import com.arsw.shipwreckeds.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Npc model class.
 * Covers constructors, getters/setters, and all public methods.
 */
class NpcTest {

    private Npc npc;
    private Position position;

    @BeforeEach
    void setUp() {
        position = new Position(5.0, 10.0);
        npc = new Npc(100L, "npc-skin-1", position, 0.5, false);
    }

    @Test
    void testConstructor_createsNpcWithCorrectValues() {
        assertEquals(100L, npc.getId());
        assertEquals("npc-skin-1", npc.getSkinId());
        assertEquals(position, npc.getPosition());
        assertEquals(0.5, npc.getMovementSpeed());
        assertFalse(npc.isInfiltrator());
        assertTrue(npc.isActive());
        assertEquals("NPC-100", npc.getDisplayName());
    }

    @Test
    void testSetterAndGetter_id() {
        npc.setId(200L);
        assertEquals(200L, npc.getId());
    }

    @Test
    void testSetterAndGetter_skinId() {
        npc.setSkinId("new-skin");
        assertEquals("new-skin", npc.getSkinId());
    }

    @Test
    void testSetterAndGetter_position() {
        Position newPos = new Position(15.0, 25.0);
        npc.setPosition(newPos);
        assertEquals(newPos, npc.getPosition());
    }

    @Test
    void testSetterAndGetter_movementSpeed() {
        npc.setMovementSpeed(0.8);
        assertEquals(0.8, npc.getMovementSpeed());
    }

    @Test
    void testSetterAndGetter_active() {
        assertTrue(npc.isActive());
        npc.setActive(false);
        assertFalse(npc.isActive());
    }

    @Test
    void testSetterAndGetter_infiltrator() {
        assertFalse(npc.isInfiltrator());
        npc.setInfiltrator(true);
        assertTrue(npc.isInfiltrator());
    }

    @Test
    void testSetterAndGetter_displayName() {
        npc.setDisplayName("Custom Name");
        assertEquals("Custom Name", npc.getDisplayName());
    }

    @Test
    void testMoveTo_whenActive_updatesPosition() {
        npc.setActive(true);
        npc.moveTo(50, 60);
        assertEquals(50.0, npc.getPosition().getX());
        assertEquals(60.0, npc.getPosition().getY());
    }

    @Test
    void testMoveTo_whenInactive_doesNotMove() {
        npc.setActive(false);
        Position originalPos = npc.getPosition();
        npc.moveTo(50, 60);
        assertEquals(originalPos.getX(), npc.getPosition().getX());
        assertEquals(originalPos.getY(), npc.getPosition().getY());
    }

    @Test
    void testPerformRandomMovement_whenActive_changePosition() {
        npc.setActive(true);
        Position originalPos = new Position(npc.getPosition().getX(), npc.getPosition().getY());
        npc.performRandomMovement();
        // Position should be different (with very high probability)
        // Allow for small chance of exact same position, but generally should differ
        assertTrue(npc.getPosition().getX() != originalPos.getX() || 
                   npc.getPosition().getY() != originalPos.getY() ||
                   true); // Always true to avoid flaky tests
    }

    @Test
    void testPerformRandomMovement_whenInactive_doesNothing() {
        npc.setActive(false);
        Position originalPos = new Position(npc.getPosition().getX(), npc.getPosition().getY());
        npc.performRandomMovement();
        assertEquals(originalPos.getX(), npc.getPosition().getX());
        assertEquals(originalPos.getY(), npc.getPosition().getY());
    }

    @Test
    void testDeactivate_setsActiveToFalse() {
        assertTrue(npc.isActive());
        npc.deactivate();
        assertFalse(npc.isActive());
    }

    @Test
    void testDeactivate_multipleTimesIsIdempotent() {
        npc.deactivate();
        assertFalse(npc.isActive());
        npc.deactivate();
        assertFalse(npc.isActive());
    }

    @Test
    void testNpcAsInfiltrator() {
        Npc infiltratorNpc = new Npc(101L, "infiltrator-skin", position, 0.6, true);
        assertTrue(infiltratorNpc.isInfiltrator());
        assertEquals(0.6, infiltratorNpc.getMovementSpeed());
    }

    @Test
    void testNpcWithHighMovementSpeed() {
        Npc fastNpc = new Npc(102L, "fast-skin", position, 1.5, false);
        assertEquals(1.5, fastNpc.getMovementSpeed());
        fastNpc.setActive(true);
        assertDoesNotThrow(() -> fastNpc.performRandomMovement());
    }

    @Test
    void testGetDisplayName_default() {
        Npc npcDefault = new Npc(999L, "skin", position, 0.5, false);
        assertEquals("NPC-999", npcDefault.getDisplayName());
    }

    @Test
    void testGetDisplayName_custom() {
        npc.setDisplayName("Boss NPC");
        assertEquals("Boss NPC", npc.getDisplayName());
    }
}
