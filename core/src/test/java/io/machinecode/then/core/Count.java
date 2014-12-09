package io.machinecode.then.core;

import io.machinecode.then.api.OnComplete;

/**
* @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
*/
class Count implements OnComplete {
    int count = 0;

    @Override
    public void complete(final int state) {
        count++;
    }
}
