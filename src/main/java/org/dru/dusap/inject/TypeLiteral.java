package org.dru.dusap.inject;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Objects;

public class TypeLiteral<T> {
    private static final Class<?> WILDCARD_MARKER = WildcardMarker.class;

    public static <T> TypeLiteral<T> of(final Type type) {
        return new TypeLiteral<>(type);
    }

    static <T> TypeLiteral<T> normalize(final TypeLiteral<T> typeLiteral) {
        Objects.requireNonNull(typeLiteral, "typeLiteral");
        if (typeLiteral.getClass() == TypeLiteral.class) {
            return typeLiteral;
        }
        return new TypeLiteral<>(typeLiteral.getRawType(), typeLiteral.argumentTypes);
    }

    private static Type[] getArgumentTypes(final Type type) {
        if (type instanceof Class) {
            return new Type[0];
        } else if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments();
        } else if (type instanceof GenericArrayType) {
            final GenericArrayType genericArrayType = (GenericArrayType) type;
            final Type componentType = genericArrayType.getGenericComponentType();
            if (!(componentType instanceof ParameterizedType)) {
                throw new IllegalArgumentException("expected ParameterizedType, got " + componentType.toString());
            }
            return ((ParameterizedType) componentType).getActualTypeArguments();
        } else if (type instanceof WildcardType) {
            final WildcardType wildcardType = (WildcardType) type;
            final Type[] lowerBounds = wildcardType.getLowerBounds();
            final Type[] upperBounds = wildcardType.getUpperBounds();
            final Type lower = lowerBounds.length > 0 ? wildcardType.getLowerBounds()[0] : Object.class;
            final Type upper = upperBounds.length > 0 ? wildcardType.getUpperBounds()[0] : Object.class;
            return new Type[]{lower, upper};
        } else if (type instanceof TypeVariable) {
            throw new RuntimeException(String.format("variable type %s can't be fully resolved", type));
        } else {
            return new Type[]{type};
        }
    }

    private static Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof GenericArrayType) {
            final GenericArrayType genericArrayType = (GenericArrayType) type;
            final Type componentType = genericArrayType.getGenericComponentType();
            if (!(componentType instanceof ParameterizedType)) {
                throw new IllegalArgumentException("expected ParameterizedType, got " + componentType.toString());
            }
            final Class<?> rawType = (Class<?>) ((ParameterizedType) componentType).getRawType();
            return Array.newInstance(rawType, 0).getClass();
        } else if (type instanceof WildcardType) {
            return WILDCARD_MARKER;
        } else {
            return Object.class;
        }
    }

    private static Type getGenericSuperclassType(final Class<?> subclass) {
        final Type superclass = subclass.getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new RuntimeException("missing type parameter, use like this: new TypeLiteral<MyType>(){};");
        }
        final ParameterizedType parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }

    private final Class<? super T> type;
    private final String typeName;
    private final String[] argumentTypes;

    @SuppressWarnings({"unchecked", "unused"})
    protected TypeLiteral() {
        final Type genericType = getGenericSuperclassType(getClass());
        this.type = (Class<T>) getRawType(genericType);
        typeName = type.getName();
        final Type[] argumentTypes = getArgumentTypes(genericType);
        this.argumentTypes = new String[argumentTypes.length];
        initArgumentTypes(argumentTypes);
    }

    @SuppressWarnings("unchecked")
    private TypeLiteral(final Type type) {
        Objects.requireNonNull(type, "type");
        this.type = (Class<T>) getRawType(type);
        typeName = this.type.getName();
        final Type[] argumentTypes = getArgumentTypes(type);
        this.argumentTypes = new String[argumentTypes.length];
        initArgumentTypes(argumentTypes);
    }

    private TypeLiteral(final Class<? super T> type, final String... argumentTypes) {
        this.type = type;
        typeName = type.getName();
        this.argumentTypes = argumentTypes;
    }

    private void initArgumentTypes(final Type... argumentsType) {
        for (int index = 0; index < argumentsType.length; index++) {
            this.argumentTypes[index] = new TypeLiteral<>(argumentsType[index]).toString();
        }
    }

    public final Class<? super T> getRawType() {
        return type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeLiteral)) return false;
        final TypeLiteral<?> that = (TypeLiteral<?>) o;
        return typeName.equals(that.typeName) &&
                Arrays.equals(argumentTypes, that.argumentTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(typeName);
        result = 31 * result + Arrays.hashCode(argumentTypes);
        return result;
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder(typeName);
        if (argumentTypes.length > 0) {
            sb.append(Arrays.toString(argumentTypes));
        }
        return sb.toString();
    }

    private interface WildcardMarker {
    }
}