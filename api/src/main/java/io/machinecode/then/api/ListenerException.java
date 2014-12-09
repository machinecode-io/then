package io.machinecode.then.api;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class ListenerException extends RuntimeException {

    public ListenerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ListenerException(final Throwable cause) {
        super(cause);
    }
}
