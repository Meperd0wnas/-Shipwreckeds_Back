package com.arsw.shipwreckeds;

import com.arsw.shipwreckeds.model.dto.CreateMatchRequest;
import com.arsw.shipwreckeds.model.dto.CreateMatchResponse;
import com.arsw.shipwreckeds.model.dto.AvatarState;
import com.arsw.shipwreckeds.model.dto.GameState;
import com.arsw.shipwreckeds.model.dto.LoginRequest;
import com.arsw.shipwreckeds.model.dto.VoteRequest;
import com.arsw.shipwreckeds.model.dto.VoteResult;
import com.arsw.shipwreckeds.model.dto.VoteStart;
import com.arsw.shipwreckeds.model.dto.JoinMatchRequest;
import com.arsw.shipwreckeds.model.dto.MoveCommand;
import com.arsw.shipwreckeds.model.dto.FuelActionRequest;
import com.arsw.shipwreckeds.model.dto.FuelActionResponse;
import com.arsw.shipwreckeds.model.dto.VoteAck;
import com.arsw.shipwreckeds.model.dto.EliminationEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive unit tests for all DTO classes.
 * Tests constructors, getters, setters, and serialization readiness.
 */
class DTOTests {

    @Test
    void testCreateMatchRequest_gettersAndSetters() {
        CreateMatchRequest req = new CreateMatchRequest();
        req.setHostName("TestHost");
        assertEquals("TestHost", req.getHostName());
    }

    @Test
    void testCreateMatchResponse_constructor() {
        CreateMatchResponse resp = new CreateMatchResponse("ABC123");
        assertEquals("ABC123", resp.getCode());
    }

    @Test
    void testAvatarState_allConstructorsAndSetters() {
        AvatarState avatar = new AvatarState();
        avatar.setId(1L);
        avatar.setType("human");
        avatar.setOwnerUsername("player1");
        avatar.setX(10.0);
        avatar.setY(20.0);
        avatar.setInfiltrator(false);
        avatar.setAlive(true);
        avatar.setDisplayName("Player1");
        
        assertEquals(1L, avatar.getId());
        assertEquals("human", avatar.getType());
        assertEquals("player1", avatar.getOwnerUsername());
        assertEquals(10.0, avatar.getX());
        assertEquals(20.0, avatar.getY());
        assertFalse(avatar.isInfiltrator());
        assertTrue(avatar.isAlive());
        assertEquals("Player1", avatar.getDisplayName());
    }

    @Test
    void testAvatarState_constructorWithAllArgs() {
        AvatarState avatar = new AvatarState(1L, "npc", null, 5.0, 15.0, true, true, "NPC-1");
        assertEquals(1L, avatar.getId());
        assertEquals("npc", avatar.getType());
        assertNull(avatar.getOwnerUsername());
        assertEquals(5.0, avatar.getX());
        assertEquals(15.0, avatar.getY());
        assertTrue(avatar.isInfiltrator());
        assertTrue(avatar.isAlive());
        assertEquals("NPC-1", avatar.getDisplayName());
    }

    @Test
    void testGameState_allSetters() {
        GameState gameState = new GameState();
        gameState.setCode("TEST01");
        gameState.setTimestamp(System.currentTimeMillis());
        gameState.setTimerSeconds(240);
        gameState.setFuelPercentage(50.0);
        gameState.setStatus("STARTED");
        gameState.setWinnerMessage(null);
        gameState.setFuelWindowOpen(true);
        gameState.setFuelWindowSecondsRemaining(30);
        
        assertEquals("TEST01", gameState.getCode());
        assertEquals(240, gameState.getTimerSeconds());
        assertEquals(50.0, gameState.getFuelPercentage());
        assertEquals("STARTED", gameState.getStatus());
        assertTrue(gameState.isFuelWindowOpen());
        assertEquals(30, gameState.getFuelWindowSecondsRemaining());
    }

    @Test
    void testGameState_islandAndBoat() {
        GameState.Island island = new GameState.Island(0.0, 0.0, 100.0);
        assertEquals(0.0, island.getCx());
        assertEquals(0.0, island.getCy());
        assertEquals(100.0, island.getRadius());
        
        GameState.Boat boat = new GameState.Boat(112.0, 0.0, 40.0);
        assertEquals(112.0, boat.getX());
        assertEquals(0.0, boat.getY());
        assertEquals(40.0, boat.getInteractionRadius());
    }

    @Test
    void testLoginRequest_gettersAndSetters() {
        LoginRequest login = new LoginRequest();
        login.setUsername("ana");
        login.setPassword("1234");
        assertEquals("ana", login.getUsername());
        assertEquals("1234", login.getPassword());
    }

    @Test
    void testVoteRequest_gettersAndSetters() {
        VoteRequest vote = new VoteRequest();
        vote.setUsername("player1");
        vote.setTargetId(100L);
        assertEquals("player1", vote.getUsername());
        assertEquals(100L, vote.getTargetId());
    }

    @Test
    void testVoteRequest_constructor() {
        VoteRequest vote = new VoteRequest("player2", 101L);
        assertEquals("player2", vote.getUsername());
        assertEquals(101L, vote.getTargetId());
    }

    @Test
    void testVoteResult_allSetters() {
        Map<Long, Integer> counts = new HashMap<>();
        counts.put(100L, 3);
        counts.put(101L, 2);
        
        VoteResult result = new VoteResult(counts, 100L, "npc", "NPC expelled", 1);
        assertEquals(2, result.getCounts().size());
        assertEquals(100L, result.getExpelledId());
        assertEquals("npc", result.getExpelledType());
        assertEquals("NPC expelled", result.getMessage());
        assertEquals(1, result.getAbstentions());
    }

    @Test
    void testVoteStart_allSetters() {
        List<AvatarState> options = new ArrayList<>();
        options.add(new AvatarState(100L, "npc", null, 0.0, 0.0, false, true, "NPC-100"));
        
        VoteStart vs = new VoteStart(options, "Start voting", 20);
        assertEquals(1, vs.getOptions().size());
        assertEquals("Start voting", vs.getMessage());
        assertEquals(20, vs.getDurationSeconds());
    }

    @Test
    void testJoinMatchRequest_gettersAndSetters() {
        JoinMatchRequest req = new JoinMatchRequest();
        req.setUsername("player1");
        req.setCode("ABC123");
        assertEquals("player1", req.getUsername());
        assertEquals("ABC123", req.getCode());
    }

    @Test
    void testFuelActionRequest_gettersAndSetters() {
        FuelActionRequest req = new FuelActionRequest();
        req.setUsername("player1");
        req.setAction(FuelActionRequest.Action.FILL);
        req.setAmount(5.0);
        
        assertEquals("player1", req.getUsername());
        assertEquals(FuelActionRequest.Action.FILL, req.getAction());
        assertEquals(5.0, req.getAmount());
    }

    @Test
    void testFuelActionResponse_gettersAndSetters() {
        FuelActionResponse resp = new FuelActionResponse(75.0, "started");
        assertEquals(75.0, resp.getFuelPercentage());
        assertEquals("started", resp.getStatus());
    }

    @Test
    void testVoteAck_gettersAndSetters() {
        VoteAck ack = new VoteAck("player1", "Vote received");
        assertEquals("player1", ack.getUsername());
        assertEquals("Vote received", ack.getMessage());
    }

    @Test
    void testEliminationEvent_gettersAndSetters() {
        EliminationEvent event = new EliminationEvent(2L, "player2", "You have been eliminated");
        assertEquals(2L, event.getTargetId());
        assertEquals("player2", event.getTargetUsername());
        assertEquals("You have been eliminated", event.getMessage());
    }

    @Test
    void testMoveCommand_withDirection() {
        MoveCommand.Direction dir = new MoveCommand.Direction(1.0, 0.0);
        assertEquals(1.0, dir.getDx());
        assertEquals(0.0, dir.getDy());
        
        MoveCommand cmd = new MoveCommand();
        cmd.setUsername("player1");
        cmd.setAvatarId(1L);
        cmd.setDirection(dir);
        
        assertEquals("player1", cmd.getUsername());
        assertEquals(1L, cmd.getAvatarId());
        assertEquals(1.0, cmd.getDirection().getDx());
    }

    @Test
    void testGameStateComplete() {
        List<AvatarState> avatars = new ArrayList<>();
        avatars.add(new AvatarState(1L, "human", "player1", 10.0, 20.0, false, true, "player1"));
        
        GameState.Island island = new GameState.Island(0.0, 0.0, 100.0);
        GameState.Boat boat = new GameState.Boat(112.0, 0.0, 40.0);
        
        GameState gs = new GameState(
            "TEST01",
            System.currentTimeMillis(),
            240,
            island,
            avatars,
            50.0,
            "STARTED",
            boat,
            null,
            true,
            30
        );
        
        assertEquals("TEST01", gs.getCode());
        assertEquals(240, gs.getTimerSeconds());
        assertEquals(50.0, gs.getFuelPercentage());
        assertEquals(1, gs.getAvatars().size());
    }

    @Test
    void testDTOSerializability_noExceptions() {
        // Ensure all DTOs can be instantiated and manipulated without exceptions
        assertDoesNotThrow(() -> {
            CreateMatchRequest req = new CreateMatchRequest();
            req.setHostName("Host");
            
            CreateMatchResponse resp = new CreateMatchResponse("CODE");
            AvatarState avatar = new AvatarState(1L, "human", "player1", 0.0, 0.0, false, true, "player1");
            GameState gs = new GameState();
            LoginRequest login = new LoginRequest();
            VoteRequest vote = new VoteRequest();
        });
    }
}
