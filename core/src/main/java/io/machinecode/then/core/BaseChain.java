package io.machinecode.then.core;

import io.machinecode.then.api.Chain;
import io.machinecode.then.api.OnLink;
import io.machinecode.then.api.Sync;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Brent Douglas <brent.n.douglas@gmail.com>
 */
public abstract class BaseChain<T> extends DeferredImpl<T> implements Chain<T> {

    private static final Logger log = Logger.getLogger(BaseChain.class);

    protected final List<OnLink> onLinks = new LinkedList<OnLink>();

    protected void cancelling(final OnLink on) {
        onLink(on);
        //try { //TODO This needs to wait on the entire chain to be cancelled
        //    await(lock, condition);
        //} catch (final Exception e) {
        //    exception = new RuntimeException(Messages.format("THEN-000006.deferred.cancel.exception"), e);
        //}
    }

    @Override
    public abstract void await(final Sync sync) throws InterruptedException;

    @Override
    public abstract void await(final long timeout, final TimeUnit unit, final Sync sync) throws InterruptedException, TimeoutException;

    @Override
    protected Logger log() {
        return log;
    }
}
