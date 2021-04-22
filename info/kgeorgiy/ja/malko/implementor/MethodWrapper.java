package info.kgeorgiy.ja.malko.implementor;

import java.lang.reflect.Method;

/**
 * Wrapper for method with correct hashcode and equals
 */
public class MethodWrapper {
    private Method method;

    /**
     * Wrap method.
     *
     * @param method to wrap
     */
    public MethodWrapper(Method method) {
        this.method = method;
    }

    /**
     * Unwrap method.
     *
     * @return unwrapped {@link Method}
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Check if number and types of corresponding classes are match.
     *
     * @param a first {@link Class[]} to compare
     * @param b second {@link Class[]} to compare
     * @return matching result
     */
    public boolean isEqualsTypeNames(Class<?>[] a, Class<?>[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].getTypeName().equals(b[i].getTypeName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * if {@code obj} instance of {@link MethodWrapper} check if equals
     * signatures of wrapped methods, otherwise {@code false}.
     *
     * @param obj {@link Object} to check on equality
     * @return equality result
     */
    public boolean equals(Object obj) {
        if (obj instanceof MethodWrapper) {
            Method otherMethod = ((MethodWrapper) obj).method;
            return method.getName().equals(otherMethod.getName()) &&
                    isEqualsTypeNames(
                            method.getParameterTypes(),
                            otherMethod.getParameterTypes());
        }
        return false;
    }


    /**
     * Get hashcode of method by coding it {@code name} and {@code parameter types}.
     *
     * @return {@link int} hash.
     */
    public int hashCode() {
        int hash = method.getName().hashCode();

        Class<?>[] args = method.getParameterTypes();
        for (Class<?> arg : args) {
            hash = hash * 31 + arg.getTypeName().hashCode();
        }
        return hash;
    }
}
