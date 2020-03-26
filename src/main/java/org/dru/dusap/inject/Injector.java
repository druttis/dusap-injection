package org.dru.dusap.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public interface Injector {
    Class<? extends Module> getModuleType();

    List<Binding<?>> getLocalBindings();

    List<Binding<?>> getBindings();

    <T> T getInstance(Key<T> key);

    <T> T getInstance(Class<T> type);

    <T> T newInstance(Constructor<T> constructor, boolean injectMembers);

    <T> T newInstance(Class<T> type, boolean injectMembers);

    void injectField(Object instance, Field field);

    void injectFields(Object instance);

    Object injectMethod(Object instance, Method method);

    void injectMethods(Object instance);

    void injectMembers(Object instance);
}
