package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Promise<T> {

    int PENDING = 0;
    int RESOLVED = 1;
    int REJECTED = 2;

    void resolve(final T that);

    void reject(final Throwable that);

    boolean isResolved();

    boolean isRejected();

    int getState();

    Promise<T> whenResolved(final WhenResolved<T> then);

    Promise<T> whenRejected(final WhenRejected<Throwable> then);

    Promise<T> onResolve(final On<Promise<?>> on);

    Promise<T> onReject(final On<Promise<?>> on);

    Promise<T> always(final On<Promise<?>> on);
}
