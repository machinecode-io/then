package io.machinecode.then.api;

/**
 * Listener for a {@link Promise} entering a state where {@link io.machinecode.then.api.Promise#isDone()}
 * returns true.
 *
 * @see Promise
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public interface OnComplete {

    /**
     * @param state The final state of the {@link Promise}. It is not guaranteed that this parameter will be
     *              one of the constants defined in {@link Promise} , inheritors MAY provide alternate
     *              terminal states.
     *
     * @see Promise#RESOLVED
     * @see Promise#REJECTED
     * @see Promise#PENDING
     */
    void complete(final int state);
}
