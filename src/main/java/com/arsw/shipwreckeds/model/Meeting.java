package com.arsw.shipwreckeds.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the state of an emergency meeting inside a match.
 * <p>
 * While a meeting is active, players can send chat messages and cast votes
 * against suspicious NPCs. Once the
 * countdown finishes, the votes are tallied to determine the most selected
 * target.
 *
 */
@Setter
@Getter
public class Meeting {

    private static final Logger logger = LoggerFactory.getLogger(Meeting.class);

    private Long id;
    private Player calledBy;
    private int durationSeconds;
    private List<ChatMessage> chatMessages;
    private Map<Long, Long> votes;

    /**
     * Creates a meeting record owned by the specified player.
     *
     * @param id              sequential meeting identifier
     * @param calledBy        player who triggered the meeting
     * @param durationSeconds meeting duration in seconds
     */
    public Meeting(Long id, Player calledBy, int durationSeconds) {
        this.id = id;
        this.calledBy = calledBy;
        this.durationSeconds = durationSeconds;
        this.chatMessages = new ArrayList<>();
        this.votes = new HashMap<>();
    }

    /**
     * Starts the meeting. Future iterations may attach a real timer here.
     */
    public void start() {
        logger.info("La reunión fue convocada por {}.", calledBy.getUsername());
        logger.info("Duración: {} segundos.", durationSeconds);
    }

    /**
     * Adds a chat message to the transcript.
     *
     * @param msg message sent by a player
     */
    public void addChat(ChatMessage msg) {
        chatMessages.add(msg);
        logger.debug("[Jugador {}]: {}", msg.getSenderId(), msg.getText());
    }

    /**
     * Registers a vote cast by a player.
     *
     * @param voterId     identifier of the voter
     * @param targetNpcId NPC id selected for elimination
     */
    public void castVote(Long voterId, Long targetNpcId) {
        votes.put(voterId, targetNpcId);
        logger.debug("Jugador {} votó por el NPC {}.", voterId, targetNpcId);
    }

    /**
     * Computes the most voted NPC once the meeting finishes.
     *
     * @return NPC identifier with the highest vote count, or {@code null} in case
     *         of a tie
     */
    public Long tallyVotes() {
        if (votes.isEmpty()) {
            logger.debug("No se emitieron votos en la reunión.");
            return null;
        }

        Map<Long, Integer> voteCount = new HashMap<>();
        for (Long targetId : votes.values()) {
            voteCount.put(targetId, voteCount.getOrDefault(targetId, 0) + 1);
        }

        Long mostVotedId = null;
        int maxVotes = 0;
        boolean tie = false;

        for (Map.Entry<Long, Integer> entry : voteCount.entrySet()) {
            if (entry.getValue() > maxVotes) {
                mostVotedId = entry.getKey();
                maxVotes = entry.getValue();
                tie = false;
            } else if (entry.getValue() == maxVotes) {
                tie = true;
            }
        }

        if (tie) {
            logger.info("La votación terminó en empate.");
            return null;
        }

        logger.info("El NPC más votado fue {} con {} votos.", mostVotedId, maxVotes);
        return mostVotedId;
    }
}
