package com.arsw.shipwreckeds;

import com.arsw.shipwreckeds.controller.MatchController;
import com.arsw.shipwreckeds.controller.WebSocketController;
import com.arsw.shipwreckeds.model.*;
import com.arsw.shipwreckeds.model.dto.*;
import com.arsw.shipwreckeds.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para MatchController.
 *
 *
 * @author Daniel Ruge
 * @version 2025-10-29
 */
@ExtendWith(MockitoExtension.class)
class MatchControllerTest {

    @Mock
    private MatchService matchService;

    @Mock
    private AuthService authService;

    @Mock
    private WebSocketController webSocketController;

    @Mock
    private RoleService roleService;

    @Mock
    private NpcService npcService;

    @Mock
    private GameEngine gameEngine;

    @InjectMocks
    private MatchController matchController;

    @BeforeEach
    void setUp() {
        // inyectado por @InjectMocks
    }

    @Test
    void createMatch_hostNotConnected_returnsBadRequest() {
        CreateMatchRequest req = new CreateMatchRequest();
        req.setHostName("noHost");
        when(authService.getPlayer("noHost")).thenReturn(null);

        ResponseEntity<?> resp = matchController.createMatch(req);

        assertEquals(400, resp.getStatusCode().value());
        assertTrue(((String) resp.getBody()).toLowerCase().contains("inicia sesión") || ((String) resp.getBody()).toLowerCase().contains("inicia sesion"));
        verify(matchService, never()).createMatch(any());
    }

    @Test
    void joinMatch_success_broadcastsAndReturnsMatch() {
        JoinMatchRequest req = new JoinMatchRequest();
        req.setCode("C1");
        req.setUsername("playerA");

        Player player = mock(Player.class);
        Match returnedMatch = mock(Match.class);

        when(authService.getPlayer("playerA")).thenReturn(player);
        when(matchService.joinMatch("C1", player)).thenReturn(returnedMatch);

        ResponseEntity<?> resp = matchController.joinMatch(req);

        assertEquals(200, resp.getStatusCode().value());
        assertSame(returnedMatch, resp.getBody());
        verify(matchService, times(1)).joinMatch("C1", player);
        verify(webSocketController, times(1)).broadcastLobbyUpdate(returnedMatch);
    }

    @Test
    void joinMatch_playerNotConnected_returnsBadRequest() {
        JoinMatchRequest req = new JoinMatchRequest();
        req.setCode("C2");
        req.setUsername("ghost");

        when(authService.getPlayer("ghost")).thenReturn(null);

        ResponseEntity<?> resp = matchController.joinMatch(req);

        assertEquals(400, resp.getStatusCode().value());
        verify(matchService, never()).joinMatch(anyString(), any());
    }

    @Test
    void startMatch_notFound_returnsBadRequest() {
        when(matchService.getMatchByCode("X")).thenReturn(null);

        ResponseEntity<?> resp = matchController.startMatch("X", "host");

        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void startMatch_onlyHostCanStart_returnsForbidden() {
        Match match = mock(Match.class);
        Player first = mock(Player.class);
        when(matchService.getMatchByCode("M")).thenReturn(match);
        when(match.getPlayers()).thenReturn(List.of(first)); // only one player
        when(first.getUsername()).thenReturn("otherHost");

        ResponseEntity<?> resp = matchController.startMatch("M", "notHost");

        assertEquals(403, resp.getStatusCode().value());
    }


    @Test
    void startMatch_success_assignsRolesGeneratesNpcsBroadcastsAndStartsTicker() {
        Match match = mock(Match.class);
        // 5 players required
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);
        Player p3 = mock(Player.class);
        Player p4 = mock(Player.class);
        Player p5 = mock(Player.class);
        List<Player> players = List.of(p1, p2, p3, p4, p5);

        when(matchService.getMatchByCode("MOK")).thenReturn(match);
        when(match.getPlayers()).thenReturn(players);
        when(p1.getUsername()).thenReturn("host123"); // host must be first
        // return empty npc list initially
        when(match.getNpcs()).thenReturn(new ArrayList<>());

        // act
        ResponseEntity<?> resp = matchController.startMatch("MOK", "host123");

        // assert
        assertEquals(200, resp.getStatusCode().value());
        verify(roleService, times(1)).assignHumanRoles(match);
        verify(npcService, times(1)).generateNpcs(match);
        verify(match, times(1)).startMatch();
        verify(webSocketController, times(1)).broadcastLobbyUpdate(match);
        verify(webSocketController, times(1)).broadcastGameState(eq(match.getCode()), any());
        verify(gameEngine, times(1)).startMatchTicker(match);
    }

    @Test
    void eliminate_outOfRange_returnsForbidden() {
        Match match = mock(Match.class);
        Player killer = mock(Player.class);
        Player target = mock(Player.class);

        when(matchService.getMatchByCode("EL")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);

        when(match.getPlayers()).thenReturn(List.of(killer, target));

        when(killer.getUsername()).thenReturn("killer");
        when(killer.isAlive()).thenReturn(true);
        when(killer.isInfiltrator()).thenReturn(true);

        when(target.getId()).thenReturn(2L);
        when(target.isAlive()).thenReturn(true);
        when(target.isInfiltrator()).thenReturn(false);

        // set positions far apart: killer at (0,0), target at (1000, 1000)
        when(killer.getPosition()).thenReturn(new Position(0.0, 0.0));
        when(target.getPosition()).thenReturn(new Position(1000.0, 1000.0));

        VoteRequest req = new VoteRequest();
        req.setUsername("killer");
        req.setTargetId(2L);

        ResponseEntity<?> resp = matchController.eliminate("EL", req);

        assertEquals(403, resp.getStatusCode().value());
        verify(target, never()).setAlive(false);
    }

    @Test
    void eliminate_success_killerIsInfiltrator_eliminatesAndBroadcasts() {
        Match match = mock(Match.class);
        Player killer = mock(Player.class);
        Player target = mock(Player.class);

        when(matchService.getMatchByCode("EL2")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);

        when(match.getPlayers()).thenReturn(List.of(killer, target));

        when(killer.getUsername()).thenReturn("killer");
        when(killer.isAlive()).thenReturn(true);
        when(killer.isInfiltrator()).thenReturn(true);
        when(killer.getPosition()).thenReturn(new Position(0.0, 0.0));

        when(target.getId()).thenReturn(20L);
        when(target.isAlive()).thenReturn(true);
        when(target.isInfiltrator()).thenReturn(false);
        when(target.getPosition()).thenReturn(new Position(5.0, 5.0)); // within elimination range (≈7.07 < 20)

        VoteRequest req = new VoteRequest();
        req.setUsername("killer");
        req.setTargetId(20L);

        ResponseEntity<?> resp = matchController.eliminate("EL2", req);

        assertEquals(200, resp.getStatusCode().value());
        // verify that target was marked dead
        verify(target, times(1)).setAlive(false);
        // verify that elimination and gamestate broadcasts happened
        verify(webSocketController, times(1)).broadcastElimination(eq("EL2"), any(EliminationEvent.class));
        verify(webSocketController, times(1)).broadcastGameState(eq("EL2"), any(GameState.class));
    }

    @Test
    void modifyFuel_tooFarFromBoat_returnsForbidden() {
        Match match = mock(Match.class);
        when(matchService.getMatchByCode("F1")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);

        Player actor = mock(Player.class);
        when(match.getPlayers()).thenReturn(List.of(actor));
        when(actor.getUsername()).thenReturn("actor");
        when(actor.isAlive()).thenReturn(true);
        // actor position far from boat: boat is at (112,0) approx; put actor far
        when(actor.getPosition()).thenReturn(new Position(10000.0, 10000.0));

        FuelActionRequest req = new FuelActionRequest();
        req.setUsername("actor");
        req.setAction(FuelActionRequest.Action.FILL);

        ResponseEntity<?> resp = matchController.modifyFuel("F1", req);

        assertEquals(403, resp.getStatusCode().value());
        verify(webSocketController, never()).broadcastGameState(anyString(), any());
    }

    @Test
    void modifyFuel_success_whenWindowOpen_updatesAndBroadcasts() {
        Match match = mock(Match.class);
        when(matchService.getMatchByCode("F2")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);

        Player actor = mock(Player.class);
        when(match.getPlayers()).thenReturn(List.of(actor));
        when(actor.getUsername()).thenReturn("actor");
        when(actor.isAlive()).thenReturn(true);
        // position near boat: boat at ~ (112,0), set actor at (112, 1)
        when(actor.getPosition()).thenReturn(new Position(112.0, 1.0));
        // fuel window open
        when(match.isFuelWindowOpenNow()).thenReturn(true);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);

        // stub match.fuel behavior
        when(match.getFuelPercentage()).thenReturn(10.0);
        // simulate adjustFuel -> returns updated value (mock)
        when(match.adjustFuel(anyDouble())).thenReturn(15.0);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);

        FuelActionRequest req = new FuelActionRequest();
        req.setUsername("actor");
        req.setAction(FuelActionRequest.Action.FILL);
        req.setAmount(5.0);

        ResponseEntity<?> resp = matchController.modifyFuel("F2", req);

        assertEquals(200, resp.getStatusCode().value());
        assertTrue(resp.getBody() instanceof FuelActionResponse);
        FuelActionResponse far = (FuelActionResponse) resp.getBody();
        assertEquals(15.0, far.getFuelPercentage());
        // broadcast happened
        verify(webSocketController, times(1)).broadcastGameState(eq("F2"), any(GameState.class));
    }

    @Test
    void getMatch_success_returnsMatch() {
        Match match = mock(Match.class);
        when(matchService.getMatchByCode("G1")).thenReturn(match);

        ResponseEntity<?> resp = matchController.getMatch("G1");

        assertEquals(200, resp.getStatusCode().value());
        assertSame(match, resp.getBody());
    }

    @Test
    void getMatch_notFound_returns404() {
        when(matchService.getMatchByCode("NOTFOUND")).thenReturn(null);

        ResponseEntity<?> resp = matchController.getMatch("NOTFOUND");

        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void startVote_matchNotFound_returnsBadRequest() {
        when(matchService.getMatchByCode("V1")).thenReturn(null);

        ResponseEntity<?> resp = matchController.startVote("V1", "player");

        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void startVote_matchNotStarted_returnsBadRequest() {
        Match match = mock(Match.class);
        when(matchService.getMatchByCode("V2")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.WAITING); // not STARTED

        ResponseEntity<?> resp = matchController.startVote("V2", "player");

        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void startVote_infiltratorTrysToVote_returnsForbidden() {
        Match match = mock(Match.class);
        Player infiltrator = mock(Player.class);

        when(matchService.getMatchByCode("V3")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);
        when(match.getPlayers()).thenReturn(List.of(infiltrator));
        when(infiltrator.getUsername()).thenReturn("infiltrator");
        when(infiltrator.isAlive()).thenReturn(true);
        when(infiltrator.isInfiltrator()).thenReturn(true);

        ResponseEntity<?> resp = matchController.startVote("V3", "infiltrator");

        assertEquals(403, resp.getStatusCode().value());
        assertTrue(((String) resp.getBody()).toLowerCase().contains("infiltrado"));
    }

    @Test
    void startVote_votingAlreadyActive_returnsBadRequest() {
        Match match = mock(Match.class);
        Player player = mock(Player.class);

        when(matchService.getMatchByCode("V4")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);
        when(match.getPlayers()).thenReturn(List.of(player));
        when(player.getUsername()).thenReturn("player");
        when(player.isAlive()).thenReturn(true);
        when(player.isInfiltrator()).thenReturn(false);
        when(match.isVotingActive()).thenReturn(true); // already voting

        ResponseEntity<?> resp = matchController.startVote("V4", "player");

        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void startVote_success_initiatesVotingAndBroadcasts() {
        Match match = mock(Match.class);
        Player voter = mock(Player.class);
        Npc npc = mock(Npc.class);

        when(matchService.getMatchByCode("V5")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);
        when(match.getPlayers()).thenReturn(List.of(voter));
        when(voter.getUsername()).thenReturn("voter");
        when(voter.isAlive()).thenReturn(true);
        when(voter.isInfiltrator()).thenReturn(false);
        when(match.isVotingActive()).thenReturn(false);
        when(match.getNpcs()).thenReturn(List.of(npc));
        when(npc.getId()).thenReturn(100L);
        when(npc.getPosition()).thenReturn(new Position(10.0, 20.0));
        when(npc.isInfiltrator()).thenReturn(false);
        when(npc.isActive()).thenReturn(true);
        when(npc.getDisplayName()).thenReturn("NPC100");
        when(match.getCode()).thenReturn("V5");

        ResponseEntity<?> resp = matchController.startVote("V5", "voter");

        assertEquals(200, resp.getStatusCode().value());
        verify(match, times(1)).startVoting();
        verify(gameEngine, times(1)).scheduleVoteTimeout(eq(match), any());
        verify(webSocketController, times(1)).broadcastGameState(eq("V5"), any(GameState.class));
        verify(webSocketController, times(1)).broadcastVoteStart(eq("V5"), any(VoteStart.class));
    }

    @Test
    void submitVote_matchNotFound_returnsBadRequest() {
        when(matchService.getMatchByCode("SV1")).thenReturn(null);

        VoteRequest req = new VoteRequest();
        req.setUsername("voter");
        req.setTargetId(1L);

        ResponseEntity<?> resp = matchController.submitVote("SV1", req);

        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void submitVote_votingNotActive_returnsBadRequest() {
        Match match = mock(Match.class);
        when(matchService.getMatchByCode("SV2")).thenReturn(match);
        when(match.isVotingActive()).thenReturn(false);

        VoteRequest req = new VoteRequest();
        req.setUsername("voter");
        req.setTargetId(1L);

        ResponseEntity<?> resp = matchController.submitVote("SV2", req);

        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void submitVote_invalidVoter_returnsForbidden() {
        Match match = mock(Match.class);
        when(matchService.getMatchByCode("SV3")).thenReturn(match);
        when(match.isVotingActive()).thenReturn(true);
        when(match.getPlayers()).thenReturn(List.of()); // no players

        VoteRequest req = new VoteRequest();
        req.setUsername("ghost");
        req.setTargetId(1L);

        ResponseEntity<?> resp = matchController.submitVote("SV3", req);

        assertEquals(403, resp.getStatusCode().value());
    }

    @Test
    void submitVote_infiltratorCannotVote_returnsForbidden() {
        Match match = mock(Match.class);
        Player infiltrator = mock(Player.class);

        when(matchService.getMatchByCode("SV4")).thenReturn(match);
        when(match.isVotingActive()).thenReturn(true);
        when(match.getPlayers()).thenReturn(List.of(infiltrator));
        when(infiltrator.getUsername()).thenReturn("infiltrator");
        when(infiltrator.isAlive()).thenReturn(true);
        when(infiltrator.isInfiltrator()).thenReturn(true);

        VoteRequest req = new VoteRequest();
        req.setUsername("infiltrator");
        req.setTargetId(1L);

        ResponseEntity<?> resp = matchController.submitVote("SV4", req);

        assertEquals(403, resp.getStatusCode().value());
    }

    @Test
    void submitVote_success_recordsVoteAndReturnsAck() {
        Match match = mock(Match.class);
        Player voter = mock(Player.class);

        when(matchService.getMatchByCode("SV5")).thenReturn(match);
        when(match.isVotingActive()).thenReturn(true);
        when(match.getPlayers()).thenReturn(List.of(voter));
        when(voter.getUsername()).thenReturn("voter");
        when(voter.isAlive()).thenReturn(true);
        when(voter.isInfiltrator()).thenReturn(false);
        when(match.allHumansVoted()).thenReturn(false); // not all voted yet

        VoteRequest req = new VoteRequest();
        req.setUsername("voter");
        req.setTargetId(100L);

        ResponseEntity<?> resp = matchController.submitVote("SV5", req);

        assertEquals(200, resp.getStatusCode().value());
        verify(match, times(1)).recordVote("voter", 100L);
        assertTrue(resp.getBody() instanceof VoteAck);
    }

    @Test
    void eliminate_deadPlayer_returnsBadRequest() {
        Match match = mock(Match.class);
        when(matchService.getMatchByCode("ELD")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);
        
        // killer must exist for the controller to find them
        Player killer = mock(Player.class);
        when(killer.getUsername()).thenReturn("killer");
        when(killer.isAlive()).thenReturn(true);
        when(killer.isInfiltrator()).thenReturn(true);
        
        // target already dead - first check validates this as 403
        Player target = mock(Player.class);
        when(match.getPlayers()).thenReturn(List.of(killer, target));
        when(target.getId()).thenReturn(1L);
        when(target.isAlive()).thenReturn(false); // dead target

        VoteRequest req = new VoteRequest();
        req.setUsername("killer");
        req.setTargetId(1L);

        ResponseEntity<?> resp = matchController.eliminate("ELD", req);

        // First validation checks: target.isAlive() is false, so returns 403
        assertEquals(403, resp.getStatusCode().value());
    }

    @Test
    void eliminate_infiltratorAsTarget_returnsForbidden() {
        Match match = mock(Match.class);
        Player killer = mock(Player.class);
        Player infiltratorTarget = mock(Player.class);

        when(matchService.getMatchByCode("ELINF")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);
        when(match.getPlayers()).thenReturn(List.of(killer, infiltratorTarget));

        when(killer.getUsername()).thenReturn("killer");
        when(killer.isAlive()).thenReturn(true);
        when(killer.isInfiltrator()).thenReturn(true); // killer IS infiltrator (so can eliminate)

        when(infiltratorTarget.getId()).thenReturn(2L);
        when(infiltratorTarget.isAlive()).thenReturn(true); // target is alive
        when(infiltratorTarget.isInfiltrator()).thenReturn(true); // but target IS also infiltrator

        VoteRequest req = new VoteRequest();
        req.setUsername("killer");
        req.setTargetId(2L);

        ResponseEntity<?> resp = matchController.eliminate("ELINF", req);

        assertEquals(403, resp.getStatusCode().value()); // cannot eliminate another infiltrator
    }

    @Test
    void eliminate_nonInfitratorCannotEliminate_returnsForbidden() {
        Match match = mock(Match.class);
        Player player = mock(Player.class);
        Player target = mock(Player.class);

        when(matchService.getMatchByCode("ELOPERM")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);
        when(match.getPlayers()).thenReturn(List.of(player, target));

        when(player.getUsername()).thenReturn("human");
        when(player.isAlive()).thenReturn(true);
        when(player.isInfiltrator()).thenReturn(false); // not infiltrator, so cannot eliminate

        VoteRequest req = new VoteRequest();
        req.setUsername("human");
        req.setTargetId(1L);

        ResponseEntity<?> resp = matchController.eliminate("ELOPERM", req);

        assertEquals(403, resp.getStatusCode().value());
    }

    @Test
    void modifyFuel_matchNotFound_returnsBadRequest() {
        when(matchService.getMatchByCode("FUELNOTFOUND")).thenReturn(null);

        FuelActionRequest req = new FuelActionRequest();
        req.setUsername("actor");
        req.setAction(FuelActionRequest.Action.FILL);

        ResponseEntity<?> resp = matchController.modifyFuel("FUELNOTFOUND", req);

        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void modifyFuel_invalidPlayer_returnsForbidden() {
        Match match = mock(Match.class);
        when(matchService.getMatchByCode("FUELINV")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);
        when(match.getPlayers()).thenReturn(List.of()); // no players

        FuelActionRequest req = new FuelActionRequest();
        req.setUsername("ghost");
        req.setAction(FuelActionRequest.Action.FILL);

        ResponseEntity<?> resp = matchController.modifyFuel("FUELINV", req);

        assertEquals(403, resp.getStatusCode().value());
    }

    @Test
    void modifyFuel_infiltratorCanSabotage_success() {
        Match match = mock(Match.class);
        Player infiltrator = mock(Player.class);

        when(matchService.getMatchByCode("FUELSABOTAGE")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);
        when(match.getPlayers()).thenReturn(List.of(infiltrator));
        when(infiltrator.getUsername()).thenReturn("inf");
        when(infiltrator.isAlive()).thenReturn(true);
        when(infiltrator.isInfiltrator()).thenReturn(true);
        when(infiltrator.getPosition()).thenReturn(new Position(112.0, 0.0)); // at boat
        when(match.isFuelWindowOpenNow()).thenReturn(true);
        when(match.adjustFuel(anyDouble())).thenReturn(50.0);

        FuelActionRequest req = new FuelActionRequest();
        req.setUsername("inf");
        req.setAction(FuelActionRequest.Action.SABOTAGE);

        ResponseEntity<?> resp = matchController.modifyFuel("FUELSABOTAGE", req);

        assertEquals(200, resp.getStatusCode().value());
        verify(match, times(1)).adjustFuel(-5.0); // default step
    }

    @Test
    void modifyFuel_humanCannotSabotage_returnsForbidden() {
        Match match = mock(Match.class);
        Player human = mock(Player.class);

        when(matchService.getMatchByCode("FUELNOSABOT")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);
        when(match.getPlayers()).thenReturn(List.of(human));
        when(human.getUsername()).thenReturn("human");
        when(human.isAlive()).thenReturn(true);
        when(human.isInfiltrator()).thenReturn(false);
        when(human.getPosition()).thenReturn(new Position(112.0, 0.0));
        when(match.isFuelWindowOpenNow()).thenReturn(true);

        FuelActionRequest req = new FuelActionRequest();
        req.setUsername("human");
        req.setAction(FuelActionRequest.Action.SABOTAGE);

        ResponseEntity<?> resp = matchController.modifyFuel("FUELNOSABOT", req);

        assertEquals(403, resp.getStatusCode().value());
    }

    @Test
    void modifyFuel_fuelWindowClosed_returns423() {
        Match match = mock(Match.class);
        Player actor = mock(Player.class);

        when(matchService.getMatchByCode("FUELCLOSED")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);
        when(match.getPlayers()).thenReturn(List.of(actor));
        when(actor.getUsername()).thenReturn("actor");
        when(actor.isAlive()).thenReturn(true);
        when(actor.getPosition()).thenReturn(new Position(112.0, 0.0));
        when(match.isFuelWindowOpenNow()).thenReturn(false);
        when(match.getFuelWindowSecondsRemaining()).thenReturn(30);

        FuelActionRequest req = new FuelActionRequest();
        req.setUsername("actor");
        req.setAction(FuelActionRequest.Action.FILL);

        ResponseEntity<?> resp = matchController.modifyFuel("FUELCLOSED", req);

        assertEquals(423, resp.getStatusCode().value());
        assertTrue(((String) resp.getBody()).contains("30"));
    }

    @Test
    void modifyFuel_infiltratorCanFillWithoutProximity() {
        Match match = mock(Match.class);
        Player infiltrator = mock(Player.class);

        when(matchService.getMatchByCode("FUELINFPROX")).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.STARTED);
        when(match.getPlayers()).thenReturn(List.of(infiltrator));
        when(infiltrator.getUsername()).thenReturn("inf");
        when(infiltrator.isAlive()).thenReturn(true);
        when(infiltrator.isInfiltrator()).thenReturn(true);
        when(infiltrator.getPosition()).thenReturn(new Position(10000.0, 10000.0)); // far from boat
        when(match.isFuelWindowOpenNow()).thenReturn(true);

        FuelActionRequest req = new FuelActionRequest();
        req.setUsername("inf");
        req.setAction(FuelActionRequest.Action.FILL); // infiltrator tries to FILL (not sabotage)

        ResponseEntity<?> resp = matchController.modifyFuel("FUELINFPROX", req);

        assertEquals(403, resp.getStatusCode().value()); // infiltrator cannot fill
    }

    @Test
    void createMatch_success_broadcastsAndReturnsMatch() {
        CreateMatchRequest req = new CreateMatchRequest();
        req.setHostName("host");

        Player host = mock(Player.class);
        CreateMatchResponse response = mock(CreateMatchResponse.class);
        Match createdMatch = mock(Match.class);

        when(authService.getPlayer("host")).thenReturn(host);
        when(matchService.createMatch(host)).thenReturn(response);
        when(response.getCode()).thenReturn("NEW001");
        when(matchService.getMatchByCode("NEW001")).thenReturn(createdMatch);

        ResponseEntity<?> resp = matchController.createMatch(req);

        assertEquals(200, resp.getStatusCode().value());
        assertSame(response, resp.getBody());
        verify(matchService, times(1)).createMatch(host);
        verify(webSocketController, times(1)).broadcastLobbyUpdate(createdMatch);
    }

    @Test
    void startMatch_insufficientPlayers_returnsBadRequest() {
        Match match = mock(Match.class);
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);

        when(matchService.getMatchByCode("INSUF")).thenReturn(match);
        when(match.getPlayers()).thenReturn(List.of(p1, p2)); // only 2 players, need 5
        when(p1.getUsername()).thenReturn("host");

        ResponseEntity<?> resp = matchController.startMatch("INSUF", "host");

        assertEquals(400, resp.getStatusCode().value());
        verify(roleService, never()).assignHumanRoles(any());
    }

    @Test
    void submitVote_allHumansVoted_conductVoting() {
        Match match = mock(Match.class);
        Player voter = mock(Player.class);
        Player npc = mock(Player.class);

        when(matchService.getMatchByCode("ALLVOTED")).thenReturn(match);
        when(match.isVotingActive()).thenReturn(true);
        when(match.getPlayers()).thenReturn(List.of(voter, npc));
        when(voter.getUsername()).thenReturn("voter");
        when(voter.isAlive()).thenReturn(true);
        when(voter.isInfiltrator()).thenReturn(false);
        when(match.allHumansVoted()).thenReturn(true); // all humans have voted

        VoteRequest req = new VoteRequest();
        req.setUsername("voter");
        req.setTargetId(100L);

        ResponseEntity<?> resp = matchController.submitVote("ALLVOTED", req);

        assertEquals(200, resp.getStatusCode().value());
        verify(match, times(1)).recordVote("voter", 100L);
    }
}

