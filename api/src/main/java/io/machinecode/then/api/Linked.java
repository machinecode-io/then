package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Linked<T> extends Deferred<T>, Synchronized {

    Linked<T> link(final Linked<?> that);

    Linked<T> onLink(final On<Deferred<?>> on);
}
