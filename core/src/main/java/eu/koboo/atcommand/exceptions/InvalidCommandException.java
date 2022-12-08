package eu.koboo.atcommand.exceptions;

/**
 * This exception is only thrown, if the parsing/registration of
 * a command is not valid.
 */
public class InvalidCommandException extends Exception {

    /**
     * The default constructor of the exception
     * @param message The message which is shown in the StackTrace
     */
    public InvalidCommandException(String message) {
        super(message);
    }
}