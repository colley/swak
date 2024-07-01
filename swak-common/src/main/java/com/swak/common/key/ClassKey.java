package com.swak.common.key;

import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

public class ClassKey implements Comparable<ClassKey>, Serializable {

    private final String className;

    private final Class<?> targetClass;

    public ClassKey(Class<?> targetClass) {
        this.targetClass = targetClass;
        this.className = targetClass.getName();
    }

    public static ClassKey classKey(Class<?> targetClass) {
        return new ClassKey(targetClass);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ClassKey)) {
            return false;
        }
        ClassKey otherKey = (ClassKey) other;
        return ObjectUtils.nullSafeEquals(this.targetClass, otherKey.targetClass);
    }

    @Override
    public int hashCode() {
        return this.targetClass != null ? this.targetClass.hashCode() * 29 : 0;
    }

    @Override
    public int compareTo(ClassKey other) {
        // Just need to sort by name, ok to collide (unless used in TreeMap/Set!)
        return className.compareTo(other.className);
    }

    @Override
    public String toString() {
        return className;
    }
}
