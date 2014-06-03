package io.machinecode.then.api;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Chain<T> extends Deferred<T>, Await {

    Chain<T> link(final Chain<?> that);

    Chain<T> onLink(final OnLink then);
}
