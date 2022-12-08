package eu.koboo.atcommand.exceptions;

/**
 * This exception is thrown if an exception occurs
 * by parsing the arguments to objects.
 */
public class ParameterException extends Exception {

    /**
     * The default constructor of the exception
     *
     * @param message The message, which is passed to the error method.
     */
    public ParameterException(String message) {
        super(message);
    }
}