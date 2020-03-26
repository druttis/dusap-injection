package org.dru.dusap.inject.internal;

import org.dru.dusap.inject.Scoping;

import javax.inject.Provider;
import java.util.concurrent.atomic.AtomicReference;

public enum Scopings implements Scoping {
    NO_SCOPE {
        @Override
        public <T> Provider<T> scope(final Provider<T> provider) {
            return provider;
        }
    },
    SINGLETON {
        @SuppressWarnings("unchecked")
        @Override
        public <T> Provider<T> scope(final Provider<T> provider) {
            final AtomicReference<Object> ref = new AtomicReference<>();
            return () -> {
                Object instance = ref.get();
                if (instance == null) {
                    instance = provider.get();
                    if (instance == null) {
                        instance = NULL;
                    }
                    if (!ref.compareAndSet(null, instance)) {
                        instance = ref.get();
                    }
                }
                if (instance == null) {
                    instance = NULL;
                }
                return (T) instance;
            };
        }
    };

    private static final Object NULL = new Object();
}
