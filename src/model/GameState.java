package model;

/**
 * This enum represents the game state of the board and controls whether
 * players can make any moves or not.
 */
public enum GameState {

    /**
     * The game is running and at least one player can make a move.
     */
    RUNNING,

    /**
     * The game is over and nobody can make a move anymore.
     */
    OVER
}
