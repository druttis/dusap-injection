package org.dru.dusap.inject.internal;

import org.dru.dusap.inject.ScopeException;
import org.dru.dusap.inject.Scoping;
import org.dru.dusap.inject.ScopingFactory;

import javax.inject.Scope;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ScopingFactoryRegistry {
    private final Map<Class<? extends Annotation>, ScopingFactory<?>> scopingFactoryByAnnotationType;

    public ScopingFactoryRegistry() {
        scopingFactoryByAnnotationType = new ConcurrentHashMap<>();
    }

    public <T extends Annotation> void registerScopingFactory(final Class<T> annotationType,
                                                              final ScopingFactory<T> scopingFactory) {
        checkScopeAnnotated(annotationType);
        Objects.requireNonNull(scopingFactory, "scopingFactory");
        if (scopingFactoryByAnnotationType.computeIfAbsent(annotationType, ($) -> scopingFactory) != scopingFactory) {
            throw new ScopeException("factory already registered: %s", annotationType.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> ScopingFactory<T> getScopingFactoryByAnnotationType(final Class<T> annotationType) {
        checkScopeAnnotated(annotationType);
        final ScopingFactory<T> scopingFactory = (ScopingFactory<T>) scopingFactoryByAnnotationType.get(annotationType);
        if (scopingFactory == null) {
            throw new ScopeException("no such factory: %s", annotationType.getName());
        }
        return scopingFactory;
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> Scoping getScoping(final T annotation) {
        Objects.requireNonNull(annotation, "annotation");
        final Class<T> annotationType = (Class<T>) annotation.annotationType();
        checkScopeAnnotated(annotationType);
        final ScopingFactory<T> scopingFactory = getScopingFactoryByAnnotationType(annotationType);
        try {
            return scopingFactory.getScoping(annotation);
        } catch (final RuntimeException exc) {
            throw new ScopeException("unhandled exception caught caused by scopingFactory %s", exc,
                    scopingFactory.getClass().getName());
        }
    }

    private void checkScopeAnnotated(final Class<? extends Annotation> annotationType) {
        Objects.requireNonNull(annotationType, "annotationType");
        if (!annotationType.isAnnotationPresent(Scope.class)) {
            throw new ScopeException("@%s is not a @%s annotation", annotationType.getName(), Scope.class.getName());
        }
    }
}
