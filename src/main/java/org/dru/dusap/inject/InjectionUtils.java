package org.dru.dusap.inject;

import javax.inject.Inject;
import javax.inject.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InjectionUtils {
    public static List<Annotation> getScopeAnnotations(final AnnotatedElement element) {
        Objects.requireNonNull(element, "element");
        return Stream.of(element.getAnnotations())
                .filter((annotation) -> annotation.annotationType().isAnnotationPresent(Scope.class))
                .collect(Collectors.toList());
    }

    public static Annotation getScopeAnnotation(final AnnotatedElement element) {
        final List<Annotation> annotations = getScopeAnnotations(element);
        if (annotations.isEmpty()) {
            return null;
        }
        if (annotations.size() == 1) {
            return annotations.get(0);
        }
        throw new ScopeException("multiple scope annotations: %s", annotations);
    }

    public static List<Class<? extends Module>> getDependencyTypes(final Class<? extends Module> moduleType) {
        Objects.requireNonNull(moduleType, "moduleType");
        final DependsOn dependsOn = moduleType.getAnnotation(DependsOn.class);
        return (dependsOn != null ? Arrays.asList(dependsOn.value()) : Collections.emptyList());
    }

    public static void checkModuleCircularity(final Class<? extends Module> moduleType) {
        Objects.requireNonNull(moduleType, "moduleType");
        checkModuleCircularity(moduleType, moduleType, new HashSet<>());
    }

    private static void checkModuleCircularity(final Class<? extends Module> moduleType,
                                               final Class<? extends Module> currentType,
                                               final Set<Class<? extends Module>> visitedTypes) {
        if (visitedTypes.add(currentType)) {
            for (final Class<? extends Module> dependencyType : getDependencyTypes(currentType)) {
                if (dependencyType.equals(moduleType)) {
                    throw new DependencyException("circular dependency: %s <-> %s",
                            moduleType.getName(), currentType.getName());
                }
                checkModuleCircularity(moduleType, dependencyType, visitedTypes);
            }
        }
    }

    public static int getModuleDepth(final Class<? extends Module> moduleType) {
        Objects.requireNonNull(moduleType, "moduleType");
        return getModuleDepth(moduleType, new HashSet<>(), 0);
    }

    private static int getModuleDepth(final Class<? extends Module> moduleType,
                                      final Set<Class<? extends Module>> visitedTypes, final int depth) {
        int result = depth;
        if (visitedTypes.add(moduleType)) {
            for (final Class<? extends Module> dependencyType : getDependencyTypes(moduleType)) {
                result = Math.max(result, getModuleDepth(dependencyType, visitedTypes, depth + 1));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getInjectableConstructor(final Class<T> type) {
        Objects.requireNonNull(type, "type");
        final List<Constructor<T>> constructors = Stream.of(type.getDeclaredConstructors())
                .map((constructor) -> (Constructor<T>) constructor)
                .collect(Collectors.toList());
        if (constructors.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s has no constructors...", type.getName()));
        }
        if (constructors.size() == 1) {
            return constructors.get(0);
        }
        constructors.removeIf((constructor) -> constructor.getAnnotation(Inject.class) == null);
        if (constructors.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s has several constructors of which none is"
                    + " %s annotated", type.getName(), Inject.class.getName()));
        }
        if (constructors.size() == 1) {
            return constructors.get(0);
        }
        throw new IllegalArgumentException(String.format("%s has several constructors that is %s annotated",
                type.getName(), Inject.class.getName()));
    }

    public static <T> List<Class<? super T>> getHierarchy(final Class<T> type) {
        Objects.requireNonNull(type, "type");
        final List<Class<? super T>> result = new ArrayList<>();
        Class<? super T> current = type;
        while (current != null) {
            result.add(current);
            current = current.getSuperclass();
        }
        Collections.reverse(result);
        return result;
    }

    public static List<Field> getDeclaredFields(final Class<?> type, final Predicate<? super Field> filter) {
        Objects.requireNonNull(filter, "filter");
        return getHierarchy(type).stream()
                .flatMap((current) -> Stream.of(current.getDeclaredFields()))
                .filter(filter)
                .collect(Collectors.toList());
    }

    public static List<Method> getDeclaredMethods(final Class<?> type, final Predicate<? super Method> filter) {
        Objects.requireNonNull(filter, "filter");
        return getHierarchy(type).stream()
                .flatMap((current) -> Stream.of(current.getDeclaredMethods()))
                .filter(filter)
                .collect(Collectors.toList());
    }

    public static <T> T newInstance(final Constructor<T> constructor, final Object... initargs) {
        Objects.requireNonNull(constructor, "constructor");
        constructor.setAccessible(true);
        try {
            return constructor.newInstance(initargs);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException exc) {
            throw new RuntimeException("failed to create new instance: " + constructor.toGenericString(), exc);
        }
    }

    public static void setFieldValue(final Object object, final Field field, final Object value) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(field, "field");
        field.setAccessible(true);
        try {
            field.set(object, value);
        } catch (final IllegalAccessException exc) {
            throw new RuntimeException("failed to set field: " + field.toGenericString(), exc);
        }
    }

    public static Object invokeMethod(final Object object, final Method method, final Object... args) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(method, "method");
        method.setAccessible(true);
        try {
            return method.invoke(object, args);
        } catch (final IllegalAccessException | InvocationTargetException exc) {
            throw new RuntimeException("failed to invoke method: " + method.toGenericString(), exc);
        }
    }

    private InjectionUtils() throws InstantiationException {
        throw new InstantiationException();
    }
}
