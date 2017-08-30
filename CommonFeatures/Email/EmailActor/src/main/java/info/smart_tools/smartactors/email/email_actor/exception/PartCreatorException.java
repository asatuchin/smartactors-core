package info.smart_tools.smartactors.email.email_actor.exception;

import info.smart_tools.smartactors.email.email_actor.email.MessagePartCreator;

/**
 * Exception for {@link MessagePartCreator}
 */
public class PartCreatorException extends Exception {
    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public PartCreatorException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */

    public PartCreatorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public PartCreatorException(final Throwable cause) {
        super(cause);
    }
}
