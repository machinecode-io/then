package io.machinecode.then.api;

/**
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class FailureException extends Exception {

    final Object failure;

    public FailureException(final String message, final Object failure) {
        super(message);
        this.failure = failure;
    }
}
