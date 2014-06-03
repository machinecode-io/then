package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class CancelledException extends CompletedException {

    public CancelledException(final String message) {
        super(message);
    }
}
