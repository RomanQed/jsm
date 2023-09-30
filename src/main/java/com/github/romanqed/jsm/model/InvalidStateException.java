package com.github.romanqed.jsm.model;

/**
 * A class representing an exception being thrown {@link MachineModelBuilder}.
 */
public class InvalidStateException extends IllegalArgumentException {
    private final Object state;

    public InvalidStateException(String message, Throwable cause, Object state) {
        super(message, cause);
        this.state = state;
    }

    public InvalidStateException(String message, Object state) {
        super(message);
        this.state = state;
    }

    public InvalidStateException(Object state) {
        super("Invalid finite machine state");
        this.state = state;
    }

    /**
     * Returns the state with which the problem occurred.
     *
     * @return the state with which the problem occurred
     */
    public Object getState() {
        return state;
    }
}
