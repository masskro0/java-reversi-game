package model;

/**
 * The IllegalMoveException is a RunTimeException which is thrown when a player
 * makes a turn even though it is not his turn. It is invoked in the methods
 * move and machineMove in the class ReversiBoard, which implements the
 * interface Board.
 */
public class IllegalMoveException extends RuntimeException {

    /**
     * Throws a new Runtime Exception with a given error message.
     *
     * @param errorMessage A message of why something went wrong.
     */
    public IllegalMoveException(final String errorMessage) {
        super(errorMessage);
    }
}
