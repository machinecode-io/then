package io.machinecode.then.core;

import org.junit.Assert;

/**
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public class UnitTest extends Assert {

    public static void assertArrayIs(final boolean expect, final boolean[] array) {
        for (final boolean that : array) {
            assertEquals(expect, that);
        }
    }
}
