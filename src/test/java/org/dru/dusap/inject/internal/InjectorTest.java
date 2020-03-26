package org.dru.dusap.inject.internal;

import org.dru.dusap.inject.*;
import org.junit.Test;

import javax.inject.Inject;

public class InjectorTest {
    @Test(expected = Exception.class)
    public void test() {
        InjectionBuilder.newInjector(ModuleC.class).getInstance(Dumper.class);
    }

    public void test2() {
        InjectionBuilder.newInjector(ModuleD.class).getInstance(Dumper2.class);
    }


    static class ModuleA implements Module {
        @Provides
        @Expose
        String getString() {
            return "A";
        }
    }

    static class ModuleB implements Module {
        @Provides
        @Expose
        String getString() {
            return "B";
        }
    }

    @DependsOn({ModuleA.class, ModuleB.class})
    static class ModuleC implements Module {
        @Provides
        Dumper getDumper() {
            return new Dumper();
        }
    }

    static class Dumper {
        @Inject
        void acceptString(String string) {
            System.out.println(string);
        }
    }

    @DependsOn({ModuleA.class, ModuleB.class})
    static class ModuleD implements Module {
        @Provides
        Dumper2 getDumper() {
            return new Dumper2();
        }
    }

    static class Dumper2 {
        @Inject
        void acceptString(@Source(ModuleA.class) String string) {
            System.out.println(string);
        }
    }
}
