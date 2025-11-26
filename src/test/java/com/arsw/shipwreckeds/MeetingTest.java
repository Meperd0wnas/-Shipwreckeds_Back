package com.arsw.shipwreckeds;

import com.arsw.shipwreckeds.model.ChatMessage;
import com.arsw.shipwreckeds.model.Meeting;
import com.arsw.shipwreckeds.model.Player;
import com.arsw.shipwreckeds.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

/**
 * Comprehensive unit tests for the Meeting model class.
 * Tests meeting lifecycle, chat management, and vote tallying.
 */
class MeetingTest {

    private Meeting meeting;
    private Player caller;

    @BeforeEach
    void setUp() {
        caller = new Player(1L, "Caller", "skin1", new Position(0.0, 0.0));
        meeting = new Meeting(1L, caller, 60);
    }

    @Test
    void testConstructor_createsWithCorrectValues() {
        assertEquals(1L, meeting.getId());
        assertEquals(caller, meeting.getCalledBy());
        assertEquals(60, meeting.getDurationSeconds());
        assertTrue(meeting.getChatMessages().isEmpty());
        assertTrue(meeting.getVotes().isEmpty());
    }

    @Test
    void testStart_logsCorrectly() {
        assertDoesNotThrow(() -> meeting.start());
    }

    @Test
    void testAddChat_addsSingleMessage() {
        Date now = new Date();
        ChatMessage msg = new ChatMessage(1L, 2L, "Hello!", now);
        meeting.addChat(msg);
        assertEquals(1, meeting.getChatMessages().size());
        assertEquals(msg, meeting.getChatMessages().get(0));
    }

    @Test
    void testAddChat_multipleMessages() {
        Date now = new Date();
        ChatMessage msg1 = new ChatMessage(1L, 2L, "First", now);
        ChatMessage msg2 = new ChatMessage(2L, 3L, "Second", now);
        ChatMessage msg3 = new ChatMessage(3L, 1L, "Third", now);
        
        meeting.addChat(msg1);
        meeting.addChat(msg2);
        meeting.addChat(msg3);
        
        assertEquals(3, meeting.getChatMessages().size());
    }

    @Test
    void testCastVote_registersVoteCorrectly() {
        meeting.castVote(1L, 100L);
        assertEquals(100L, (long) meeting.getVotes().get(1L));
    }

    @Test
    void testCastVote_multipleVotes() {
        meeting.castVote(1L, 100L);
        meeting.castVote(2L, 101L);
        meeting.castVote(3L, 100L);
        
        assertEquals(3, meeting.getVotes().size());
        assertEquals(100L, (long) meeting.getVotes().get(1L));
        assertEquals(101L, (long) meeting.getVotes().get(2L));
    }

    @Test
    void testTallyVotes_emptyVotes_returnsNull() {
        assertNull(meeting.tallyVotes());
    }

    @Test
    void testTallyVotes_singleVote() {
        meeting.castVote(1L, 100L);
        assertEquals(100L, (long) meeting.tallyVotes());
    }

    @Test
    void testTallyVotes_clearWinner() {
        meeting.castVote(1L, 100L);
        meeting.castVote(2L, 100L);
        meeting.castVote(3L, 101L);
        
        assertEquals(100L, (long) meeting.tallyVotes());
    }

    @Test
    void testTallyVotes_tie_returnsNull() {
        meeting.castVote(1L, 100L);
        meeting.castVote(2L, 101L);
        assertNull(meeting.tallyVotes());
    }

    @Test
    void testTallyVotes_complexScenario() {
        meeting.castVote(1L, 100L);
        meeting.castVote(2L, 100L);
        meeting.castVote(3L, 100L);
        meeting.castVote(4L, 101L);
        meeting.castVote(5L, 101L);
        
        assertEquals(100L, (long) meeting.tallyVotes());
    }

    @Test
    void testTallyVotes_multipleResults() {
        meeting.castVote(1L, 100L);
        Long first = meeting.tallyVotes();
        
        meeting.castVote(2L, 101L);
        Long second = meeting.tallyVotes();
        
        assertNull(second); // now it's a tie
    }

    @Test
    void testSettersAndGetters_id() {
        meeting.setId(5L);
        assertEquals(5L, meeting.getId());
    }

    @Test
    void testSettersAndGetters_calledBy() {
        Player newCaller = new Player(2L, "NewCaller", "skin2", new Position(5.0, 5.0));
        meeting.setCalledBy(newCaller);
        assertEquals(newCaller, meeting.getCalledBy());
    }

    @Test
    void testSettersAndGetters_durationSeconds() {
        meeting.setDurationSeconds(120);
        assertEquals(120, meeting.getDurationSeconds());
    }

    @Test
    void testSettersAndGetters_chatMessages() {
        meeting.getChatMessages().clear();
        Date now = new Date();
        ChatMessage msg = new ChatMessage(1L, 1L, "Test", now);
        meeting.getChatMessages().add(msg);
        assertEquals(1, meeting.getChatMessages().size());
    }

    @Test
    void testSettersAndGetters_votes() {
        meeting.getVotes().clear();
        meeting.getVotes().put(1L, 100L);
        assertEquals(100L, (long) meeting.getVotes().get(1L));
    }

    @Test
    void testMeetingCompleteFlow() {
        meeting.start();
        
        Date now = new Date();
        ChatMessage msg1 = new ChatMessage(1L, 1L, "I think you're suspect", now);
        ChatMessage msg2 = new ChatMessage(2L, 2L, "No, I'm not!", now);
        meeting.addChat(msg1);
        meeting.addChat(msg2);
        
        meeting.castVote(1L, 200L);
        meeting.castVote(2L, 200L);
        meeting.castVote(3L, 200L);
        
        Long mostVoted = meeting.tallyVotes();
        assertEquals(200L, (long) mostVoted);
    }
}
