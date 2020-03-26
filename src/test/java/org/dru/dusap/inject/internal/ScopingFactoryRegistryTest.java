package org.dru.dusap.inject.internal;

import org.dru.dusap.inject.Source;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ScopingFactoryRegistryTest {
    private ScopingFactoryRegistry scopingFactoryRegistry;

    @Before
    public void setUp() {
        scopingFactoryRegistry = new ScopingFactoryRegistry();
        scopingFactoryRegistry.registerScopingFactory(Singleton.class, new SingletonScopingFactory());
    }

    @Test
    public void register() {

        // check that double registration fails.
        try {
            scopingFactoryRegistry.registerScopingFactory(Singleton.class, new SingletonScopingFactory());
            Assert.fail();
        } catch (final Exception exc) {
            // success
        }

        // check that non @Scope annotated annotations fails.
        try {
            scopingFactoryRegistry.registerScopingFactory(Source.class, annotation -> null);
            Assert.fail();
        } catch (final Exception exc) {
            // success
        }
    }

    @Test
    public void getScope() {
        // check that registered scoping factory are returned
        scopingFactoryRegistry.getScopingFactoryByAnnotationType(Singleton.class);

        // check that non @Scope annotated annotations fails.
        try {
            scopingFactoryRegistry.getScopingFactoryByAnnotationType(Source.class);
            Assert.fail();
        }
        catch (final Exception exc) {
            // success
        }

        // check that non registered scoping factory fails
        try {
            scopingFactoryRegistry.getScopingFactoryByAnnotationType(DummyScope.class);
            Assert.fail();
        }
        catch (final Exception exc) {
            // success
        }
    }

    @Scope
    @Retention(RetentionPolicy.RUNTIME)
    @interface DummyScope {

    }
}