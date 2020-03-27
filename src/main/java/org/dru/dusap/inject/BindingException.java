package org.dru.dusap.inject;

public class BindingException extends InjectionException {
    public BindingException(final String message, final Object... args) {
        super(message, args);
    }

    public BindingException(final String message, final Throwable cause, final Object... args) {
        super(message, cause, args);
    }
}
