package com.meerkat.base.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Reflects {
    public static class ClassAnnotation<A extends Annotation> {
        private final Class<?> klass;
        private final A annotation;

        public ClassAnnotation(Class<?> klass, A annotation) {
            this.klass = klass;
            this.annotation = annotation;
        }

        public Class<?> getKlass() {
            return klass;
        }

        public A getAnnotation() {
            return annotation;
        }
    }

    private Reflects() {
    }

    public static List<Field> getFields(Class<?> klass, boolean recursive) {
        Map<String, Field> fields = new LinkedHashMap<String, Field>();

        ArrayList<Class<?>> classList = Lists.newArrayList();
        classList.add(klass);
        if (recursive) {
            Class<?> current = klass.getSuperclass();
            while (current != null) {
                classList.add(current);
                current = current.getSuperclass();
            }
        }

        for (int i = classList.size() - 1; i >= 0; i--) {
            Class<?> current = classList.get(i);
            for (Field field : current.getDeclaredFields()) {
                fields.put(field.getName(), field);
            }
        }

        return new ArrayList<>(fields.values());
    }

    public static boolean isAnnotationPresent(Class<?> klass,
                                              Class<? extends Annotation> annotationClass) {
        Class<?> current = klass;
        while (current != null) {
            if (current.isAnnotationPresent(annotationClass)) {
                return true;
            }
            current = current.getSuperclass();
        }

        return false;
    }

    public static <A extends Annotation> ClassAnnotation<A> getAnnotation(
            Class<?> klass, Class<A> annotationClass) {
        Class<?> current = klass;
        while (current != null) {
            if (current.isAnnotationPresent(annotationClass)) {
                A annotation = current.getAnnotation(annotationClass);
                return new ClassAnnotation<A>(current, annotation);
            }
            current = current.getSuperclass();
        }

        return null;
    }
}
