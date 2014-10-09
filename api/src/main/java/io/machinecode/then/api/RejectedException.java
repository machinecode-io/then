package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 * @since 1.0
 */
public class RejectedException extends CompletionException {

    public RejectedException(final String message) {
        super(message);
    }
}
