package com.arsw.shipwreckeds;

import com.arsw.shipwreckeds.model.Position;
import com.arsw.shipwreckeds.model.RepairCrackTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the RepairCrackTask model class.
 * Tests task activation, progress updates, and completion logic.
 */
class RepairCrackTaskTest {

    private RepairCrackTask task;
    private Position position;

    @BeforeEach
    void setUp() {
        position = new Position(25.0, 35.0);
        task = new RepairCrackTask(1L, position, 50);
    }

    @Test
    void testConstructor_createsTaskWithCorrectValues() {
        assertEquals(1L, task.getId());
        assertEquals(position, task.getLocation());
        assertFalse(task.isActive());
        assertEquals(0, task.getProgressPercent());
        assertEquals(50, task.getRequiredSpeed());
    }

    @Test
    void testStartBy_activatesTask() {
        assertFalse(task.isActive());
        task.startBy(1L);
        assertTrue(task.isActive());
    }

    @Test
    void testProgressUpdate_beforeTaskStart_doesNotProgress() {
        task.progressUpdate(1L, 10.0);
        assertEquals(0, task.getProgressPercent());
    }

    @Test
    void testProgressUpdate_afterTaskStart_incrementsProgress() {
        task.startBy(1L);
        task.progressUpdate(1L, 25.0);
        assertEquals(25, task.getProgressPercent());
    }

    @Test
    void testProgressUpdate_multipleUpdates() {
        task.startBy(1L);
        task.progressUpdate(1L, 30.0);
        task.progressUpdate(1L, 20.0);
        assertEquals(50, task.getProgressPercent());
    }

    @Test
    void testProgressUpdate_exceeding100_clampedTo100() {
        task.startBy(1L);
        task.progressUpdate(1L, 150.0);
        assertEquals(100, task.getProgressPercent());
        assertTrue(!task.isActive()); // task becomes inactive when complete
    }

    @Test
    void testProgressUpdate_completesTaskAt100() {
        task.startBy(1L);
        task.progressUpdate(1L, 100.0);
        assertEquals(100, task.getProgressPercent());
        assertFalse(task.isActive());
    }

    @Test
    void testAddProgress_helperMethod() {
        task.startBy(1L);
        task.addProgress(1L, 25);
        assertEquals(25, task.getProgressPercent());
    }

    @Test
    void testAddProgress_multipleInvocations() {
        task.startBy(1L);
        task.addProgress(1L, 20);
        task.addProgress(1L, 30);
        task.addProgress(1L, 50);
        assertEquals(100, task.getProgressPercent());
    }

    @Test
    void testSettersAndGetters_id() {
        task.setId(5L);
        assertEquals(5L, task.getId());
    }

    @Test
    void testSettersAndGetters_location() {
        Position newLoc = new Position(50.0, 60.0);
        task.setLocation(newLoc);
        assertEquals(newLoc, task.getLocation());
    }

    @Test
    void testSettersAndGetters_active() {
        task.setActive(true);
        assertTrue(task.isActive());
    }

    @Test
    void testSettersAndGetters_progressPercent() {
        task.setProgressPercent(75);
        assertEquals(75, task.getProgressPercent());
    }

    @Test
    void testSettersAndGetters_requiredSpeed() {
        task.setRequiredSpeed(100);
        assertEquals(100, task.getRequiredSpeed());
    }

    @Test
    void testTaskLifecycle_complete() {
        assertFalse(task.isActive());
        task.startBy(1L);
        assertTrue(task.isActive());
        
        task.progressUpdate(1L, 50.0);
        assertTrue(task.isActive());
        
        task.progressUpdate(1L, 50.0);
        assertFalse(task.isActive());
        assertEquals(100, task.getProgressPercent());
    }

    @Test
    void testProgressUpdate_afterCompleted_doesNotAdd() {
        task.startBy(1L);
        task.progressUpdate(1L, 100.0);
        assertFalse(task.isActive());
        
        task.progressUpdate(1L, 50.0);
        assertEquals(100, task.getProgressPercent());
    }

    @Test
    void testNegativeProgress_notHandledButAllowedByImplementation() {
        task.startBy(1L);
        task.progressUpdate(1L, 50.0);
        task.progressUpdate(1L, -25.0);
        assertEquals(25, task.getProgressPercent());
    }
}
