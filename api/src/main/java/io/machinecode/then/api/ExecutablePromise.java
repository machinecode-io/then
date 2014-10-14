package io.machinecode.then.api;

import java.util.concurrent.Callable;

/**
 * <p>A {@link Promise} that exposes a computational task suitable for execution by an {@link java.util.concurrent.ExecutorService}.</p>
 *
 * @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
 */
public interface ExecutablePromise<T,F,P> extends Promise<T,F,P> {

    /**
     * @return The computation as a {@link Runnable}.
     */
    Runnable asRunnable();

    /**
     * @return The computation as a {@link Callable}.
     */
    Callable<T> asCallable();
}
