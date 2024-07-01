package com.swak.autoconfigure.condition;

import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class ClassBeanFiltering {

    public static List<String> filter(Collection<String> classNames, ClassBeanFiltering.ClassNameFilter classNameFilter,
                                      ClassLoader classLoader) {
        if (CollectionUtils.isEmpty(classNames)) {
            return Collections.emptyList();
        } else {
            List<String> matches = new ArrayList(classNames.size());
            for (String candidate : classNames) {
                if (classNameFilter.matches(candidate, classLoader)) {
                    matches.add(candidate);
                }
            }
            return matches;
        }
    }

    public static List<String> filter(ApplicationContext applicationContext, Collection<String> classNames, ClassBeanFiltering.ClassNameFilter classNameFilter) {
      return filter(classNames,classNameFilter,deduceClassLoader(applicationContext));
    }

    public static ClassLoader deduceClassLoader(ApplicationContext applicationContext) {
        if(applicationContext==null) {
            return ClassUtils.getDefaultClassLoader();
        }
        ClassLoader classLoader = applicationContext.getClassLoader();
        return classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader();
    }

    public enum ClassNameFilter {
        PRESENT {
            public boolean matches(String className, ClassLoader classLoader) {
                return isPresent(className, classLoader);
            }
        },
        MISSING {
            public boolean matches(String className, ClassLoader classLoader) {
                return !isPresent(className, classLoader);
            }
        };

        ClassNameFilter() {}

        abstract boolean matches(String className, ClassLoader classLoader);

        static boolean isPresent(String className, ClassLoader classLoader) {
            if (classLoader == null) {
                classLoader = ClassUtils.getDefaultClassLoader();
            }
            try {
                resolve(className, classLoader);
                return true;
            } catch (Throwable var3) {
                return false;
            }
        }

        protected static void resolve(String className, ClassLoader classLoader) throws ClassNotFoundException {
            if (classLoader != null) {
                Class.forName(className, false, classLoader);
            } else {
                Class.forName(className);
            }
        }
    }
}
