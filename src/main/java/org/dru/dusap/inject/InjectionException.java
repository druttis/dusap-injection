package org.dru.dusap.inject;

public class InjectionException extends RuntimeException {
    public InjectionException(final String message, final Object... args) {
        super(String.format(message, args));
    }

    public InjectionException(final String message, final Throwable cause, final Object... args) {
        super(String.format(message, args), cause);
    }
}
