package io.machinecode.then.api;

/**
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public class ResolvedException extends CompletionException {

    public ResolvedException(final String message) {
        super(message);
    }
}
