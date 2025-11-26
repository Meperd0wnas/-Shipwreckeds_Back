package com.arsw.shipwreckeds;

import com.arsw.shipwreckeds.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

/**
 * Comprehensive unit tests for the ChatMessage model class.
 * Tests constructors, getters, and string formatting.
 */
class ChatMessageTest {

    private ChatMessage message;
    private Date timestamp;

    @BeforeEach
    void setUp() {
        timestamp = new Date();
        message = new ChatMessage(1L, 5L, "Test message", timestamp);
    }

    @Test
    void testConstructor_createsWithCorrectValues() {
        assertEquals(1L, message.getId());
        assertEquals(5L, message.getSenderId());
        assertEquals("Test message", message.getText());
        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    void testSetterAndGetter_id() {
        message.setId(10L);
        assertEquals(10L, message.getId());
    }

    @Test
    void testSetterAndGetter_senderId() {
        message.setSenderId(99L);
        assertEquals(99L, message.getSenderId());
    }

    @Test
    void testSetterAndGetter_text() {
        message.setText("New text");
        assertEquals("New text", message.getText());
    }

    @Test
    void testSetterAndGetter_timestamp() {
        Date newTime = new Date();
        message.setTimestamp(newTime);
        assertEquals(newTime, message.getTimestamp());
    }

    @Test
    void testToString_formatsCorrectly() {
        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("Jugador 5"));
        assertTrue(result.contains("Test message"));
    }

    @Test
    void testToString_withEmptyText() {
        ChatMessage emptyMsg = new ChatMessage(1L, 1L, "", timestamp);
        String result = emptyMsg.toString();
        assertNotNull(result);
        assertTrue(result.contains("Jugador 1"));
    }

    @Test
    void testChatMessageWithNullText() {
        ChatMessage nullTextMsg = new ChatMessage(1L, 1L, null, timestamp);
        assertNull(nullTextMsg.getText());
    }

    @Test
    void testChatMessageWithLongText() {
        String longText = "A".repeat(1000);
        ChatMessage longMsg = new ChatMessage(1L, 1L, longText, timestamp);
        assertEquals(longText, longMsg.getText());
        assertTrue(longMsg.toString().contains(longText));
    }

    @Test
    void testMultipleChatMessagesIndependent() {
        Date time1 = new Date(System.currentTimeMillis() - 1000);
        Date time2 = new Date(System.currentTimeMillis());
        
        ChatMessage msg1 = new ChatMessage(1L, 1L, "First", time1);
        ChatMessage msg2 = new ChatMessage(2L, 2L, "Second", time2);
        
        assertNotEquals(msg1.getId(), msg2.getId());
        assertNotEquals(msg1.getSenderId(), msg2.getSenderId());
        assertNotEquals(msg1.getText(), msg2.getText());
    }
}
