package io.machinecode.then.api;

/**
 * <p>Listener for a {@link Deferred} entering a state where {@link Deferred#isDone()}
 * returns {@code true}.</p>
 *
 * @see Deferred
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public interface OnComplete {

    /**
     * @param state The final state of the {@link Deferred}. It is not guaranteed that this parameter will be
     *              one of the constants defined in {@link Promise}, inheritors MAY provide alternate
     *              terminal states.
     *
     * @see Deferred#RESOLVED
     * @see Deferred#REJECTED
     * @see Deferred#PENDING
     */
    void complete(final int state);
}
