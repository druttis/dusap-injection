package org.dru.dusap.inject;

import org.dru.dusap.inject.internal.InjectionImpl;
import org.dru.dusap.inject.internal.ScopingFactoryRegistry;
import org.dru.dusap.inject.internal.SingletonScopingFactory;

import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Objects;

@SuppressWarnings("UnusedReturnValue")
public final class InjectionBuilder {
    public static Injector newInjector(final Class<? extends Module> moduleType) {
        return new InjectionBuilder().build().getInjector(moduleType);
    }

    private final ScopingFactoryRegistry scopingFactoryRegistry;

    public InjectionBuilder() {
        scopingFactoryRegistry = new ScopingFactoryRegistry();
        withScopingFactory(Singleton.class, new SingletonScopingFactory());
    }

    public <T extends Annotation> InjectionBuilder withScopingFactory(final Class<T> annotationType,
                                                                      final ScopingFactory<T> scopingFactory) {
        scopingFactoryRegistry.registerScopingFactory(annotationType, scopingFactory);
        return this;
    }

    public Injection build() {
        return new InjectionImpl(scopingFactoryRegistry);
    }
}
