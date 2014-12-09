package io.machinecode.then.core;

import java.util.Formatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>Message bundle for Then library.</p>
 *
 * @author <a href="mailto:brent.n.douglas@gmail.com">Brent Douglas</a>
 * @since 1.0
 */
public final class Messages {

    private Messages(){}

    private static final ResourceBundle MESSAGES;

    static {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("ThenMessages", Locale.getDefault(), Messages.class.getClassLoader());
        } catch (final Exception e) {
            bundle = ResourceBundle.getBundle("ThenMessages", Locale.ENGLISH, Messages.class.getClassLoader());
        }
        MESSAGES = bundle;
    }

    /**
     * @param key The message id.
     * @return A localised message prepended with the message id.
     */
    public static String get(final String key) {
        return key.split("\\.")[0] + ": " + raw(key);
    }

    /**
     * @param key The message id.
     * @param args Values to be formatted into the message.
     * @return A formatted localised message prepended with the message id and format it.
     */
    public static String format(final String key, final Object... args) {
        return new Formatter().format(get(key), args).toString();
    }

    /**
     * @param key The message id.
     * @return A localised message as found in the bundle.
     */
    public static String raw(final String key) {
        return MESSAGES.getString(key);
    }
}
