package com.arsw.shipwreckeds;

import com.arsw.shipwreckeds.model.FuelWarTask;
import com.arsw.shipwreckeds.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;

/**
 * Comprehensive unit tests for the FuelWarTask model class.
 * Tests task lifecycle, click registration, and winner determination.
 */
class FuelWarTaskTest {

    private FuelWarTask task;
    private Position position;

    @BeforeEach
    void setUp() {
        position = new Position(50.0, 50.0);
        task = new FuelWarTask(1L, position, 1L);
    }

    @Test
    void testConstructor_createsTaskWithCorrectValues() {
        assertEquals(1L, task.getId());
        assertEquals(position, task.getLocation());
        assertFalse(task.isActive());
        assertFalse(task.isContested());
        assertEquals(1L, task.getInitiatorId());
        assertTrue(task.getClickCounts().isEmpty());
    }

    @Test
    void testStartBy_activatesTask() {
        assertFalse(task.isActive());
        task.startBy(1L);
        assertTrue(task.isActive());
        assertTrue(task.isContested());
    }

    @Test
    void testRegisterClick_beforeTaskStart_doesNotRegister() {
        task.registerClick(1L);
        assertEquals(0, task.getClickCounts().size());
    }

    @Test
    void testRegisterClick_afterTaskStart_registersClick() {
        task.startBy(1L);
        task.registerClick(2L);
        assertEquals(1, (int) task.getClickCounts().get(2L));
    }

    @Test
    void testRegisterClick_multipleClicksSamePlayer() {
        task.startBy(1L);
        task.registerClick(2L);
        task.registerClick(2L);
        task.registerClick(2L);
        assertEquals(3, (int) task.getClickCounts().get(2L));
    }

    @Test
    void testRegisterClick_multiplePlayers() {
        task.startBy(1L);
        task.registerClick(1L);
        task.registerClick(2L);
        task.registerClick(3L);
        task.registerClick(2L);
        
        assertEquals(1, (int) task.getClickCounts().get(1L));
        assertEquals(2, (int) task.getClickCounts().get(2L));
        assertEquals(1, (int) task.getClickCounts().get(3L));
    }

    @Test
    void testProgressUpdate_delegatesToRegisterClick() {
        task.startBy(1L);
        task.progressUpdate(2L, 0.0); // delta is ignored
        assertEquals(1, (int) task.getClickCounts().get(2L));
    }

    @Test
    void testDetermineWinner_withEmptyClicks_returnsNull() {
        task.startBy(1L);
        assertNull(task.determineWinner());
    }

    @Test
    void testDetermineWinner_singlePlayer_returnsPlayer() {
        task.startBy(1L);
        task.registerClick(2L);
        task.registerClick(2L);
        assertEquals(2L, (long) task.determineWinner());
    }

    @Test
    void testDetermineWinner_clearWinner() {
        task.startBy(1L);
        task.registerClick(1L);
        task.registerClick(2L);
        task.registerClick(2L);
        task.registerClick(2L);
        assertEquals(2L, (long) task.determineWinner());
    }

    @Test
    void testDetermineWinner_withTie_returnsNull() {
        task.startBy(1L);
        task.registerClick(1L);
        task.registerClick(1L);
        task.registerClick(2L);
        task.registerClick(2L);
        assertNull(task.determineWinner());
    }

    @Test
    void testDetermineWinner_complexScenario() {
        task.startBy(1L);
        task.registerClick(1L);
        task.registerClick(1L);
        task.registerClick(1L);
        task.registerClick(2L);
        task.registerClick(2L);
        task.registerClick(3L);
        assertEquals(1L, (long) task.determineWinner());
    }

    @Test
    void testSettersAndGetters_id() {
        task.setId(5L);
        assertEquals(5L, task.getId());
    }

    @Test
    void testSettersAndGetters_location() {
        Position newLoc = new Position(100.0, 100.0);
        task.setLocation(newLoc);
        assertEquals(newLoc, task.getLocation());
    }

    @Test
    void testSettersAndGetters_active() {
        task.setActive(true);
        assertTrue(task.isActive());
    }

    @Test
    void testSettersAndGetters_contested() {
        task.setContested(false);
        assertFalse(task.isContested());
    }

    @Test
    void testSettersAndGetters_initiatorId() {
        task.setInitiatorId(99L);
        assertEquals(99L, task.getInitiatorId());
    }

    @Test
    void testSettersAndGetters_clickCounts() {
        HashMap<Long, Integer> newCounts = new HashMap<>();
        newCounts.put(1L, 5);
        task.setClickCounts(newCounts);
        assertEquals(5, (int) task.getClickCounts().get(1L));
    }
}
