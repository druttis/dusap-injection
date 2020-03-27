package org.dru.dusap.inject;

public class ScopeException extends InjectionException {
    public ScopeException(final String message, final Object... args) {
        super(message, args);
    }

    public ScopeException(final String message, final Throwable cause, final Object... args) {
        super(message, cause, args);
    }
}
