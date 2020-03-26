package org.dru.dusap.inject;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public final class Key<T> {
    public static Key<?>[] of(final Executable executable) {
        return Stream.of(executable.getParameters())
                .map((parameter) -> Key.of(parameter.getParameterizedType(), parameter))
                .toArray(Key<?>[]::new);
    }

    public static Key<?> of(final Type type, final AnnotatedElement element) {
        Objects.requireNonNull(type, "type");
        final Set<Annotation> qualifiers = new HashSet<>();
        if (element != null) {
            for (final Annotation annotation : element.getAnnotations()) {
                if (annotation.annotationType().getAnnotation(Qualifier.class) != null) {
                    qualifiers.add(annotation);
                }
            }
        }
        return new Key<>(TypeLiteral.of(type), qualifiers);
    }

    @SuppressWarnings("unchecked")
    public static <T> Key<T> of(final Class<T> type, final AnnotatedElement element) {
        return (Key<T>) of((Type) type, element);
    }

    private final TypeLiteral<T> type;
    private final Set<Annotation> qualifiers;

    private Key(final TypeLiteral<T> type, final Set<Annotation> qualifiers) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(qualifiers, "qualifiers");
        this.type = TypeLiteral.normalize(type);
        this.qualifiers = qualifiers;
    }

    public Class<? extends Module> getSourceTypeOrDefault(final Class<? extends Module> defaultType) {
        for (final Annotation qualifier : qualifiers) {
            if (qualifier.annotationType() == Source.class) {
                return ((Source) qualifier).value();
            }
        }
        return defaultType;
    }

    public Key<T> withoutSource() {
        for (final Annotation qualifier : qualifiers) {
            if (qualifier.annotationType() == Source.class) {
                final Set<Annotation> reduced = new HashSet<>(qualifiers);
                reduced.remove(qualifier);
                return new Key<>(type, reduced);
            }
        }
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Key)) return false;
        final Key<?> key = (Key<?>) o;
        return type.equals(key.type) &&
                qualifiers.equals(key.qualifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, qualifiers);
    }

    @Override
    public String toString() {
        return "Key{" +
                "type=" + type +
                ", qualifiers=" + qualifiers +
                '}';
    }
}
