package io.machinecode.then.api;

/**
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class CancelledException extends CompletionException {

    public CancelledException(final String message) {
        super(message);
    }
}
