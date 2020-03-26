package org.dru.dusap.inject;

import java.util.List;

public interface Injection {
    List<Binding<?>> getBindings();

    Injector getInjector(Class<? extends Module> moduleType);
}
