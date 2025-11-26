package com.arsw.shipwreckeds;

import com.arsw.shipwreckeds.model.Player;
import com.arsw.shipwreckeds.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Player model class.
 * Covers constructors, getters/setters, and all public methods.
 */
class PlayerTest {

    private Player player;
    private Position position;

    @BeforeEach
    void setUp() {
        position = new Position(10.0, 20.0);
        player = new Player(1L, "testPlayer", "skin1", position);
    }

    @Test
    void testConstructor_createsPlayerWithCorrectInitialValues() {
        assertEquals(1L, player.getId());
        assertEquals("testPlayer", player.getUsername());
        assertEquals("skin1", player.getSkinId());
        assertEquals(position, player.getPosition());
        assertFalse(player.isInfiltrator());
        assertTrue(player.isAlive());
    }

    @Test
    void testSetterAndGetter_id() {
        player.setId(5L);
        assertEquals(5L, player.getId());
    }

    @Test
    void testSetterAndGetter_username() {
        player.setUsername("newUsername");
        assertEquals("newUsername", player.getUsername());
    }

    @Test
    void testSetterAndGetter_skinId() {
        player.setSkinId("newSkin");
        assertEquals("newSkin", player.getSkinId());
    }

    @Test
    void testSetterAndGetter_position() {
        Position newPosition = new Position(30.0, 40.0);
        player.setPosition(newPosition);
        assertEquals(newPosition, player.getPosition());
    }

    @Test
    void testSetterAndGetter_isInfiltrator() {
        assertFalse(player.isInfiltrator());
        player.setInfiltrator(true);
        assertTrue(player.isInfiltrator());
    }

    @Test
    void testSetterAndGetter_isAlive() {
        assertTrue(player.isAlive());
        player.setAlive(false);
        assertFalse(player.isAlive());
    }

    @Test
    void testClick_executesSuccessfully() {
        assertDoesNotThrow(() -> player.click());
    }

    @Test
    void testMoveTo_updatesPlayerPosition() {
        Position newPosition = new Position(50.0, 60.0);
        player.moveTo(newPosition);
        assertEquals(newPosition, player.getPosition());
        assertEquals(50.0, newPosition.getX());
        assertEquals(60.0, newPosition.getY());
    }

    @Test
    void testActivateTask_executesSuccessfully() {
        assertDoesNotThrow(() -> player.activateTask(100L));
    }

    @Test
    void testCastVote_executesSuccessfully() {
        assertDoesNotThrow(() -> player.castVote(200L));
    }

    @Test
    void testPlayerWithNullPosition() {
        Player playerWithoutPosition = new Player(2L, "noPos", "skin2", null);
        assertNull(playerWithoutPosition.getPosition());
        
        Position newPos = new Position(5.0, 5.0);
        playerWithoutPosition.moveTo(newPos);
        assertEquals(newPos, playerWithoutPosition.getPosition());
    }

    @Test
    void testMultipleStateChanges() {
        player.setInfiltrator(true);
        player.setAlive(false);
        assertTrue(player.isInfiltrator());
        assertFalse(player.isAlive());
        
        player.setInfiltrator(false);
        player.setAlive(true);
        assertFalse(player.isInfiltrator());
        assertTrue(player.isAlive());
    }
}
