package model;

/**
 * This enum represents the three player types, which defines who can make the
 * next move on the Reversi board.
 */
public enum Player {

    /**
     * Represents the human player and his ability to make a move.
     */
    HUMAN,

    /**
     * Represents the machine and his ability to make a move.
     */
    COMPUTER,

    /**
     * This one is only used, when neither the human player nor the computer
     * can make a move. This player results in a gameover.
     */
    NOBODY
}
