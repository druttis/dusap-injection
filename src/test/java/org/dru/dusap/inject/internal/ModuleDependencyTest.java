package org.dru.dusap.inject.internal;

import org.dru.dusap.inject.*;
import org.junit.Test;

import javax.inject.Inject;

public class ModuleDependencyTest {
    @Test(expected = DependencyException.class)
    public void testCircularFails() {
        InjectionBuilder.newInjector(ModuleA.class);
    }

    @Test(expected = DependencyException.class)
    public void testSelfCircularFails() {
        InjectionBuilder.newInjector(ModuleC.class);
    }

    @Test
    public void testNonCircularSuccess() {
        InjectionBuilder.newInjector(ModuleE.class);
    }

    @Test(expected = DependencyException.class)
    public void testIllegalSourceFails() {
        InjectionBuilder.newInjector(ModuleF.class);
    }

    @DependsOn(ModuleB.class)
    static class ModuleA implements Module {
    }

    @DependsOn(ModuleA.class)
    static class ModuleB implements Module {
    }

    @DependsOn(ModuleC.class)
    static class ModuleC implements Module {
    }

    static class ModuleD implements Module {
        @Provides
        @Expose
        public String provideString() {
            return "string";
        }
    }

    @DependsOn(ModuleD.class)
    static class ModuleE implements Module {
    }

    @DependsOn(ModuleE.class)
    static class ModuleF implements Module {
        @Inject
        void runIt(@Source(ModuleD.class) String string) {

        }
    }
}
