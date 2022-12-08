package eu.koboo.atcommand.exceptions;

/**
 * This exception is thrown, if a condition is not matched.
 */
public class ConditionException extends IllegalStateException {

    /**
     * The default constructor of the exception
     * @param message The message, which is passed to the error method.
     */
    public ConditionException(String message) {
        super(message);
    }
}