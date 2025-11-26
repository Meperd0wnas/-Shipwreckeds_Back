package com.arsw.shipwreckeds;

import com.arsw.shipwreckeds.model.Match;
import com.arsw.shipwreckeds.model.MatchStatus;
import com.arsw.shipwreckeds.model.Npc;
import com.arsw.shipwreckeds.model.Player;
import com.arsw.shipwreckeds.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive unit tests for the Match model class.
 * Covers constructors, getters/setters, and core game logic methods.
 */
class MatchTest {

    private Match match;
    private Player player1;
    private Player player2;
    private Player player3;

    @BeforeEach
    void setUp() {
        match = new Match(1L, "TEST01");
        player1 = new Player(1L, "Player1", "skin1", new Position(0.0, 0.0));
        player2 = new Player(2L, "Player2", "skin1", new Position(5.0, 5.0));
        player3 = new Player(3L, "Player3", "skin1", new Position(10.0, 10.0));
    }

    @Test
    void testConstructor_createsMatchWithCorrectInitialValues() {
        assertEquals(1L, match.getId());
        assertEquals("TEST01", match.getCode());
        assertEquals(MatchStatus.WAITING, match.getStatus());
        assertEquals(0, match.getTimerSeconds());
        assertEquals(0.0, match.getFuelPercentage());
        assertNull(match.getInfiltrator());
        assertFalse(match.isVotingActive());
        assertTrue(match.getPlayers().isEmpty());
        assertTrue(match.getNpcs().isEmpty());
        assertTrue(match.getTasks().isEmpty());
    }

    @Test
    void testAddPlayer_successfullyAddsPlayerToMatch() {
        match.addPlayer(player1);
        assertEquals(1, match.getPlayers().size());
        assertEquals(player1, match.getPlayers().get(0));
    }

    @Test
    void testAddPlayer_multiplePlayersBeforeStart() {
        match.addPlayer(player1);
        match.addPlayer(player2);
        match.addPlayer(player3);
        assertEquals(3, match.getPlayers().size());
    }

    @Test
    void testAddPlayer_afterMatchStarted_preventsNewPlayers() {
        match.setStatus(MatchStatus.STARTED);
        int sizeBeforeAdd = match.getPlayers().size();
        match.addPlayer(player1);
        assertEquals(sizeBeforeAdd, match.getPlayers().size());
    }

    @Test
    void testAddPlayer_withNullPlayer_doesNotAdd() {
        match.addPlayer(null);
        assertEquals(0, match.getPlayers().size());
    }

    @Test
    void testAddNpc_successfullyAddsNpc() {
        Npc npc = new Npc(100L, "npc-skin", new Position(0.0, 0.0), 0.5, false);
        match.addNpc(npc);
        assertEquals(1, match.getNpcs().size());
        assertEquals(npc, match.getNpcs().get(0));
    }

    @Test
    void testStartMatch_withEnoughPlayers_transitionsToStarted() {
        for (int i = 0; i < 5; i++) {
            Player p = new Player((long) i, "Player" + i, "skin", new Position(0.0, 0.0));
            match.addPlayer(p);
        }
        match.startMatch();
        assertEquals(MatchStatus.STARTED, match.getStatus());
        assertEquals(Match.MATCH_DURATION_SECONDS, match.getTimerSeconds());
    }

    @Test
    void testStartMatch_withInsufficientPlayers_staysInWaiting() {
        match.addPlayer(player1);
        match.addPlayer(player2);
        match.startMatch();
        assertEquals(MatchStatus.WAITING, match.getStatus());
    }

    @Test
    void testAssignInfiltrator_selectsOnePlayer() {
        match.addPlayer(player1);
        match.addPlayer(player2);
        match.addPlayer(player3);
        match.assignInfiltrator();
        
        assertNotNull(match.getInfiltrator());
        assertTrue(match.getInfiltrator().isInfiltrator());
        assertEquals(1, (int) match.getPlayers().stream()
                .filter(Player::isInfiltrator).count());
    }

    @Test
    void testAssignInfiltrator_withEmptyPlayerList_doesNothing() {
        match.assignInfiltrator();
        assertNull(match.getInfiltrator());
    }

    @Test
    void testTickTimer_decrementsTimerWhenStarted() {
        match.setStatus(MatchStatus.STARTED);
        match.setTimerSeconds(100);
        match.tickTimer();
        assertEquals(99, match.getTimerSeconds());
    }

    @Test
    void testTickTimer_withTimerZero_endsMatch() {
        match.setStatus(MatchStatus.STARTED);
        match.setTimerSeconds(1);
        match.setInfiltrator(player1);
        player1.setInfiltrator(true);
        player1.setAlive(true);
        match.tickTimer();
        assertEquals(MatchStatus.FINISHED, match.getStatus());
    }

    @Test
    void testAdjustFuel_increasesPercentage() {
        double result = match.adjustFuel(10.0);
        assertEquals(10.0, result);
        assertEquals(10.0, match.getFuelPercentage());
    }

    @Test
    void testAdjustFuel_clampsToMax100() {
        match.adjustFuel(150.0);
        assertEquals(100.0, match.getFuelPercentage());
    }

    @Test
    void testAdjustFuel_clampsToMin0() {
        match.setFuelPercentage(50.0);
        match.adjustFuel(-100.0);
        assertEquals(0.0, match.getFuelPercentage());
    }

    @Test
    void testResetFuel_setsToZero() {
        match.setFuelPercentage(80.0);
        match.resetFuel();
        assertEquals(0.0, match.getFuelPercentage());
    }

    @Test
    void testIsFuelWindowOpenNow_whenNotStarted_returnsFalse() {
        match.setStatus(MatchStatus.WAITING);
        assertFalse(match.isFuelWindowOpenNow());
    }

    @Test
    void testIsFuelWindowOpenNow_whenTimerZero_returnsFalse() {
        match.setStatus(MatchStatus.STARTED);
        match.setTimerSeconds(0);
        assertFalse(match.isFuelWindowOpenNow());
    }

    @Test
    void testIsFuelWindowOpenNow_duringOddCycle_returnsTrue() {
        match.setStatus(MatchStatus.STARTED);
        match.setTimerSeconds(Match.MATCH_DURATION_SECONDS - 90); // elapsed ~90s, cycle 1 (odd)
        assertTrue(match.isFuelWindowOpenNow());
    }

    @Test
    void testGetFuelWindowSecondsRemaining_returnsCorrectValue() {
        match.setStatus(MatchStatus.STARTED);
        match.setTimerSeconds(Match.MATCH_DURATION_SECONDS - 30);
        int remaining = match.getFuelWindowSecondsRemaining();
        assertTrue(remaining > 0);
    }

    @Test
    void testStartVoting_initializesVotingState() {
        match.startVoting();
        assertTrue(match.isVotingActive());
        assertNotNull(match.getVotesByPlayer());
    }

    @Test
    void testRecordVote_storesPlayerVote() {
        match.startVoting();
        match.recordVote("player1", 100L);
        assertEquals(100L, match.getVotesByPlayer().get("player1"));
    }

    @Test
    void testStopVoting_disablesVoting() {
        match.startVoting();
        match.stopVoting();
        assertFalse(match.isVotingActive());
    }

    @Test
    void testCountHumanAlivePlayers_countsCorrectly() {
        match.addPlayer(player1);
        match.addPlayer(player2);
        match.addPlayer(player3);
        player1.setInfiltrator(true);
        assertEquals(2, match.countHumanAlivePlayers());
    }

    @Test
    void testCountHumanAlivePlayers_excludesDeadPlayers() {
        match.addPlayer(player1);
        match.addPlayer(player2);
        match.addPlayer(player3);
        player1.setInfiltrator(true);
        player2.setAlive(false);
        assertEquals(1, match.countHumanAlivePlayers());
    }

    @Test
    void testAllHumansVoted_whenAllVoted_returnsTrue() {
        match.addPlayer(player1);
        match.addPlayer(player2);
        player1.setInfiltrator(true); // only player2 is human
        match.startVoting();
        match.recordVote("Player2", 100L);
        assertTrue(match.allHumansVoted());
    }

    @Test
    void testAllHumansVoted_whenNotAllVoted_returnsFalse() {
        match.addPlayer(player1);
        match.addPlayer(player2);
        player1.setInfiltrator(true);
        match.startVoting();
        assertFalse(match.allHumansVoted());
    }

    @Test
    void testEndMatch_transitionsToFinished() {
        match.setStatus(MatchStatus.STARTED);
        match.endMatch();
        assertEquals(MatchStatus.FINISHED, match.getStatus());
    }

    @Test
    void testTriggerMeeting_whenNotStarted_doesNothing() {
        match.setStatus(MatchStatus.WAITING);
        match.triggerMeeting(player1);
        assertEquals(MatchStatus.WAITING, match.getStatus());
    }

    @Test
    void testGettersAndSetters_id() {
        match.setId(5L);
        assertEquals(5L, match.getId());
    }

    @Test
    void testGettersAndSetters_code() {
        match.setCode("NEWCODE");
        assertEquals("NEWCODE", match.getCode());
    }

    @Test
    void testGettersAndSetters_infiltrator() {
        match.setInfiltrator(player1);
        assertEquals(player1, match.getInfiltrator());
    }

    @Test
    void testGettersAndSetters_winnerMessage() {
        String msg = "Test winner";
        match.setWinnerMessage(msg);
        assertEquals(msg, match.getWinnerMessage());
    }
}
