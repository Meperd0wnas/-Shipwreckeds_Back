package com.arsw.shipwreckeds.controller;

import com.arsw.shipwreckeds.model.Match;
import com.arsw.shipwreckeds.util.Constants;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Envía el estado del lobby a todos los suscriptores en /topic/lobby/{code}
     */
    public void broadcastLobbyUpdate(Match match) {
        if (match == null || match.getCode() == null)
            return;
        String dest = "/topic/lobby/" + match.getCode();
        messagingTemplate.convertAndSend(dest, match);
    }

    /**
     * Publica el GameState completo a /topic/game/{code}
     */
    public void broadcastGameState(String code, Object gameState) {
        if (code == null)
            return;
        String dest = Constants.WS_TOPIC_GAME + code;
        messagingTemplate.convertAndSend(dest, gameState);
    }

    /**
     * Broadcast that a voting session has started. Payload can be a VoteStart DTO.
     */
    public void broadcastVoteStart(String code, Object voteStart) {
        if (code == null)
            return;
        String dest = Constants.WS_TOPIC_GAME + code + Constants.WS_TOPIC_GAME_VOTE_START;
        messagingTemplate.convertAndSend(dest, voteStart);
    }

    /**
     * Broadcast final vote results.
     */
    public void broadcastVoteResult(String code, Object result) {
        if (code == null)
            return;
        String dest = Constants.WS_TOPIC_GAME + code + Constants.WS_TOPIC_GAME_VOTE_RESULT;
        messagingTemplate.convertAndSend(dest, result);
    }

    /**
     * Broadcast a player elimination event to /topic/game/{code}/elimination
     */
    public void broadcastElimination(String code, Object eliminationEvent) {
        if (code == null)
            return;
        String dest = Constants.WS_TOPIC_GAME + code + Constants.WS_TOPIC_GAME_ELIMINATION;
        messagingTemplate.convertAndSend(dest, eliminationEvent);
    }
}
