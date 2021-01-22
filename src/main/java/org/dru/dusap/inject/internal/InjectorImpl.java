package org.dru.dusap.inject.internal;

import org.dru.dusap.inject.*;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InjectorImpl implements Injector {
    private static final Predicate<AnnotatedElement> INJECT_ANNOTATED
            = (elem) -> (elem.getAnnotation(Inject.class) != null);
    private static final Predicate<Method> PROVIDES_ANNOTATED
            = (method) -> (method.getAnnotation(Provides.class) != null);

    private final InjectionImpl injectionImpl;
    private final InjectorImpl parentInjectorImpl;
    private final ScopingFactoryRegistry scopingFactoryRegistry;
    private final Class<? extends Module> moduleType;
    private final Set<Class<? extends Module>> dependencyTypes;
    private final Set<Class<? extends Module>> childTypes;
    private final Map<Key<?>, BindingImpl<?>> bindingImplByKey;
    private Module moduleInstance;

    InjectorImpl(final InjectionImpl injectionImpl, final InjectorImpl parentInjectorImpl,
                 final ScopingFactoryRegistry scopingFactoryRegistry, final Class<? extends Module> moduleType,
                 final Collection<Class<? extends Module>> dependencyTypes) {
        this.injectionImpl = injectionImpl;
        this.parentInjectorImpl = parentInjectorImpl;
        this.scopingFactoryRegistry = scopingFactoryRegistry;
        this.moduleType = moduleType;
        this.dependencyTypes = new HashSet<>(dependencyTypes);
        childTypes = ConcurrentHashMap.newKeySet();
        bindingImplByKey = new ConcurrentHashMap<>();
        final Key<Injector> key = Key.of(Injector.class, null);
        final Provider<Injector> provider = () -> this;
        bindingImplByKey.put(key, new BindingImpl<>(key, false, provider, Scopings.NO_SCOPING));
        dependencyTypes.forEach(dependencyType -> injectionImpl.getInjector(dependencyType).childTypes.add(moduleType));
    }

    @Override
    public Class<? extends Module> getModuleType() {
        return moduleType;
    }

    @Override
    public List<Binding<?>> getLocalBindings() {
        return new ArrayList<>(bindingImplByKey.values());
    }

    @Override
    public List<Binding<?>> getBindings() {
        final List<Binding<?>> result = getLocalBindings();
        InjectionUtils.getDependencyTypes(getModuleType()).forEach(dependencyType -> {
            final Injector dependencyInjector = injectionImpl.getInjector(dependencyType);
            result.addAll(dependencyInjector.getBindings().stream()
                    .filter(Binding::isExposed)
                    .collect(Collectors.toList()));
        });
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(final Key<T> key) {
        Objects.requireNonNull(key, "key");
        final Class<? extends Module> sourceType = key.getSourceTypeOrDefault(moduleType);
        final InjectorImpl injector = injectionImpl.getInjector(sourceType);
        if (injector != this && !dependencyTypes.contains(injector.getModuleType())) {
            throw new DependencyException("illegal dependency: %s", injector.getModuleType().getName());
        }
        final BindingImpl<?> binding = injector.getBindingOrNull(key.withoutSource());
        if (binding == null) {
            throw new BindingException("no such binding: %s", key);
        }
        return (T) binding.getInstance();
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return getInstance(Key.of(type, null));
    }

    @Override
    public <T> T newInstance(final Constructor<T> constructor, final boolean injectMembers) {
        final Key<?>[] keys = Key.of(constructor);
        final Object[] initargs = getInstances(keys);
        final T instance = InjectionUtils.newInstance(constructor, initargs);
        if (injectMembers) {
            injectMembers(instance);
        }
        return instance;
    }

    @Override
    public <T> T newInstance(final Class<T> type, final boolean injectMembers) {
        return newInstance(InjectionUtils.getInjectableConstructor(type), injectMembers);
    }

    @Override
    public void injectField(final Object instance, final Field field) {
        final Key<?> key = Key.of(field.getType(), field);
        final Object value = getInstance(key);
        InjectionUtils.setFieldValue(instance, field, value);
    }

    @Override
    public void injectFields(final Object instance) {
        final Class<?> type = instance.getClass();
        InjectionUtils.getDeclaredFields(type, INJECT_ANNOTATED).forEach((field) -> injectField(instance, field));
    }

    @Override
    public Object injectMethod(final Object instance, final Method method) {
        final Key<?>[] keys = Key.of(method);
        final Object[] args = getInstances(keys);
        return InjectionUtils.invokeMethod(instance, method, args);
    }

    @Override
    public void injectMethods(final Object instance) {
        final Class<?> type = instance.getClass();
        InjectionUtils.getDeclaredMethods(type, INJECT_ANNOTATED).forEach((method) -> injectMethod(instance, method));
    }

    @Override
    public void injectMembers(final Object instance) {
        injectFields(instance);
        injectMethods(instance);
    }

    void setModuleInstance(final Module moduleInstance) {
        this.moduleInstance = moduleInstance;
    }

    @SuppressWarnings("unchecked")
    <T> BindingImpl<T> getLocalBinding(final Key<T> key) {
        return (BindingImpl<T>) bindingImplByKey.get(key);
    }

    <T> BindingImpl<T> getBindingOrNull(final Key<T> key) {
        return getBindingOrNull(key, new HashSet<>());
    }

    <T> BindingImpl<T> getBindingOrNull(final Key<T> key, final Set<Class<? extends Module>> visitedModuleTypes) {
        if (visitedModuleTypes.add(moduleType)) {
            BindingImpl<T> binding = getLocalBinding(key);
            if (binding != null) {
                return binding;
            }
            if (parentInjectorImpl != null) {
                binding = parentInjectorImpl.getBindingOrNull(key);
                if (binding != null && binding.isExposed()) {
                    return binding;
                }
            }
            if (injectionImpl != null) {
                final List<BindingImpl<T>> bindings = new ArrayList<>();
                for (final Class<? extends Module> dependencyType : InjectionUtils.getDependencyTypes(moduleType)) {
                    binding = injectionImpl.getInjector(dependencyType).getLocalBinding(key);
                    if (binding != null && binding.isExposed()) {
                        bindings.add(binding);
                    }
                }
                if (bindings.size() == 1) {
                    return bindings.get(0);
                } else if (bindings.size() > 1) {
                    throw new BindingException("multiple bindings: %s", bindings);
                } else {
                    for (final Class<? extends Module> childType : childTypes) {
                        binding = injectionImpl.getInjector(childType).getBindingOrNull(key, visitedModuleTypes);
                        if (binding != null) {
                            bindings.add(binding);
                        }
                    }
                    if (bindings.size() == 1) {
                        return bindings.get(0);
                    } else if (bindings.size() > 1) {
                        throw new BindingException("multiple bindings: %s", bindings);
                    }
                }
            }
        }
        return null;
    }

    Object[] getInstances(final Key<?>[] keys) {
        return Stream.of(keys)
                .map(this::getInstance)
                .toArray(Object[]::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void bindProviderMethods() {
        InjectionUtils.getDeclaredMethods(moduleType, PROVIDES_ANNOTATED).forEach((method) -> {
            final Key<?> key = Key.of(method.getGenericReturnType(), method);
            final Binding<?> existing = bindingImplByKey.get(key);
            if (existing != null) {
                throw new BindingException("already bound: %s", existing);
            }
            final Provider<?> provider = new ProviderMethod<>(this, () -> moduleInstance, method);
            final Annotation scope = InjectionUtils.getScopeAnnotation(method);
            final Scoping scoping;
            if (scope != null) {
                scoping = scopingFactoryRegistry.getScoping(scope);
            } else {
                scoping = Scopings.NO_SCOPING;
            }
            final boolean exposed = (method.getAnnotation(Expose.class) != null);
            final BindingImpl<?> binding = new BindingImpl(key, exposed, provider, scoping);
            bindingImplByKey.put(key, binding);
        });
    }
}
