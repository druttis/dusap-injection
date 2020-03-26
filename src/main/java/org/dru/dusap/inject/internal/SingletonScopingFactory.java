package org.dru.dusap.inject.internal;

import org.dru.dusap.inject.Scoping;
import org.dru.dusap.inject.ScopingFactory;

import javax.inject.Singleton;

public final class SingletonScopingFactory implements ScopingFactory<Singleton> {
    @Override
    public Scoping getScoping(final Singleton annotation) {
        return Scopings.SINGLETON;
    }
}
