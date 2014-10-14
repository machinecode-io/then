package io.machinecode.then.core;

import io.machinecode.then.api.OnComplete;

/**
* @author Brent Douglas (brent.n.douglas@gmail.com)
 * @since 1.0
*/
class Count implements OnComplete {
    int count = 0;

    @Override
    public void complete(final int state) {
        count++;
    }
}
