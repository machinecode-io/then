package io.machinecode.then.api;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class ResolvedException extends CompletionException {

    public ResolvedException(final String message) {
        super(message);
    }
}
