package com.meerkat.base.db;

import java.math.BigDecimal;
import java.math.BigInteger;

final class Util {

    public static boolean isPrimitive(Class<?> klass) {
        if (klass.isPrimitive()) {
            return true;
        }
        return klass == Integer.class || klass == Long.class
                || klass == String.class || klass == Boolean.class
                || klass == Short.class || klass == Float.class
                || klass == Double.class || klass == BigInteger.class
                || klass == BigDecimal.class;
    }
}
