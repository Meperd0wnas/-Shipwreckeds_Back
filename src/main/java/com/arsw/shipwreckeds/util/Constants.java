package com.arsw.shipwreckeds.util;

/**
 * Central constants used across the Shipwreckeds application.
 * Centralizes string literals to improve maintainability and reduce duplication.
 */
public class Constants {

    // Error messages
    public static final String MATCH_NOT_FOUND = "Partida no encontrada.";
    public static final String MATCH_NOT_IN_PROGRESS = "La partida no está en curso.";
    public static final String INSUFFICIENT_PLAYERS = "No hay suficientes jugadores para iniciar la partida. Se requieren 5 jugadores humanos.";

    // Match status
    public static final String MATCH_STATUS_STARTED = "STARTED";

    // Avatar types
    public static final String AVATAR_TYPE_HUMAN = "human";
    public static final String AVATAR_TYPE_NPC = "npc";

    // WebSocket topics
    public static final String WS_TOPIC_GAME = "/topic/game/";
    public static final String WS_TOPIC_GAME_VOTE_START = "/vote/start";
    public static final String WS_TOPIC_GAME_VOTE_RESULT = "/vote/result";
    public static final String WS_TOPIC_GAME_ELIMINATION = "/elimination";

    private Constants() {
        // Utility class, no instantiation
    }
}
