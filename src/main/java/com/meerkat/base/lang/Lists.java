package com.meerkat.base.lang;

import org.springframework.beans.BeanUtils;

import java.util.*;

public final class Lists {

    private Lists() {
    }

    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList<T>();
    }

    public static <T> List<T> fill(Class<T> klass, int count) {
        ArrayList<T> result = newArrayList();
        for (int i = 0; i < count; i++) {
            result.add(BeanUtils.instantiate(klass));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> flatten(List<?> list) {
        List<T> result = newArrayList();
        for (Object o : list) {
            if (o instanceof List) {
                List<T> subResult = flatten((List<?>) o);
                result.addAll(subResult);
            } else {
                result.add((T) o);
            }
        }
        return result;
    }

    public static <T> List<T> compact(List<T> list) {
        List<T> result = newArrayList();
        for (T t : list) {
            if (t != null) {
                result.add(t);
            }
        }
        return result;
    }

    public static <T, R> List<R> map(List<T> list, String property) {
        List<R> result = newArrayList();
        for (T t : list) {
            R r = Beans.get(t, property);
            result.add(r);
        }
        return result;
    }

    public static <K, V> Map<K, V> toMap(List<V> list, String keyProperty) {
        Map<K, V> map = new HashMap<K, V>();
        for (V v : list) {
            K k = Beans.get(v, keyProperty);
            map.put(k, v);
        }
        return map;
    }

    public static <T> List<T> filter(List<T> list, String property, Object value) {
        List<T> result = newArrayList();
        for (T t : list) {
            Object v = Beans.get(t, property);
            if ((v == null && value == null) || (v != null && v.equals(value))) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * 移除与value相等的值, 原数组不发生变化.
     *
     * @param list
     * @param value
     * @return
     */
    public static <T> List<T> without(List<T> list, T value) {
        List<T> result = newArrayList();
        for (T t : list) {
            if ((value == null && t == null)
                    || (value != null && value.equals(t))) {
                continue;
            }
            result.add(t);
        }
        return result;
    }

    public static <T> List<T> uniq(List<T> input) {
        LinkedHashMap<T, T> map = new LinkedHashMap<>();
        for (T t : input) {
            map.put(t, t);
        }
        return new ArrayList<T>(map.values());
    }

    public static <K, T> List<T> sortBy(List<T> input, String keyProperty,
                                        List<K> keys) {
        if (input.isEmpty()) {
            return newArrayList();
        }

        Map<K, T> map = toMap(input, keyProperty);
        List<T> result = newArrayList();
        for (K k : keys) {
            T t = map.get(k);
            if (t != null) {
                result.add(t);
            }
        }
        return result;
    }

    public static <K, V> Map<K, List<V>> group(List<V> input, String keyProperty) {
        Map<K, List<V>> result = new HashMap<>();

        for (V v : input) {
            K k = Beans.get(v, keyProperty);
            List<V> list = result.get(k);
            if (list == null) {
                list = new ArrayList<>();
                result.put(k, list);
            }
            list.add(v);
        }

        return result;
    }

    /**
     * 将数组按n个一份拆分.
     *
     * @param input
     * @param n
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> group(List<T> input, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must > 0");
        }

        int size = input.size();
        int m = (size + n - 1) / n;
        List<List<T>> result = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            List<T> items = new ArrayList(n);
            int end = i < m - 1 ? n : size - i * n;
            for (int j = 0; j < end; j++) {
                items.add(input.get(i * n + j));
            }
            result.add(items);
        }
        return result;
    }
}
