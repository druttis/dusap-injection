package org.dru.dusap.inject;

public class DependencyException extends InjectionException {
    public DependencyException(final String message, final Object... args) {
        super(message, args);
    }

    public DependencyException(final String message, final Throwable cause, final Object... args) {
        super(message, cause, args);
    }
}
