package org.dru.dusap.inject.internal;

import org.dru.dusap.inject.Binding;
import org.dru.dusap.inject.Key;
import org.dru.dusap.inject.Scoping;

import javax.inject.Provider;

final class BindingImpl<T> implements Binding<T> {
    private final Key<T> key;
    private final Provider<? extends T> provider;
    private final Scoping scoping;
    private final Provider<? extends T> scopedProvider;
    private final boolean exposed;

    BindingImpl(final Key<T> key, final boolean exposed, final Provider<? extends T> provider, final Scoping scoping) {
        this.key = key;
        this.exposed = exposed;
        this.provider = provider;
        this.scoping = scoping;
        scopedProvider = scoping.scope(provider);
    }

    @Override
    public Key<T> getKey() {
        return key;
    }

    @Override
    public boolean isExposed() {
        return exposed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Provider<? extends T>> getProviderClass() {
        return (Class<? extends Provider<? extends T>>) provider.getClass();
    }

    @Override
    public Class<? extends Scoping> getScopeClass() {
        return scoping.getClass();
    }

    public T getInstance() {
        return scopedProvider.get();
    }

    @Override
    public String toString() {
        return "Binding{" +
                "key=" + key +
                ", exposed=" + exposed +
                ", provider=" + provider +
                ", scope=" + scoping +
                '}';
    }
}
