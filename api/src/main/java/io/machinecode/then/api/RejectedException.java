package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class RejectedException extends CompletedException {

    public RejectedException(final String message) {
        super(message);
    }
}
