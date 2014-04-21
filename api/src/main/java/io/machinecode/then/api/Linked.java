package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Linked<T> extends Deferred<T>, Synchronized {

    void link(final Linked<?> that);

    void onLink(final On<Deferred<?>> listener);
}
