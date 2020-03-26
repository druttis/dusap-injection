package org.dru.dusap.inject.internal;

import org.dru.dusap.inject.DependsOn;
import org.dru.dusap.inject.InjectionBuilder;
import org.dru.dusap.inject.Module;
import org.junit.Test;

public class ModuleDependencyTest {
    @Test(expected = Exception.class)
    public void testCircularFails() {
        InjectionBuilder.newInjector(ModuleA.class);
    }

    @Test(expected = Exception.class)
    public void testSelfCircularFails() {
        InjectionBuilder.newInjector(ModuleC.class);
    }

    @Test
    public void testNonCircularSuccess() {
        InjectionBuilder.newInjector(ModuleE.class);
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
    }

    @DependsOn(ModuleD.class)
    static class ModuleE implements Module {
    }
}
