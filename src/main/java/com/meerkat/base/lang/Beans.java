package com.meerkat.base.lang;

import com.google.common.base.CaseFormat;
import com.meerkat.base.util.JsonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public final class Beans {

    private static CaseFormat[] toFormat = {
            CaseFormat.LOWER_CAMEL, CaseFormat.UPPER_CAMEL, CaseFormat.LOWER_HYPHEN, CaseFormat.LOWER_UNDERSCORE, CaseFormat.UPPER_UNDERSCORE};

    private Beans() {
    }

    public static void set(Object bean, String property, Object value) {
        BeanWrapper wrapper = PropertyAccessorFactory
                .forBeanPropertyAccess(bean);

        wrapper.setPropertyValue(property, value);
    }

    @SuppressWarnings("unchecked")
    public static <R> R get(Object bean, String property) {
        BeanWrapper wrapper = PropertyAccessorFactory
                .forBeanPropertyAccess(bean);
        return (R) wrapper.getPropertyValue(property);
    }

    public static <T, R> R copy(T from, Class<R> klass,
                                String... excludedProperties) {
        R to = BeanUtils.instantiate(klass);
        return copy(from, to, excludedProperties);
    }

    /**
     * 浅copy, 不逐级复制.
     *
     * @param from
     * @param to
     * @param excludedProperties
     * @return
     */
    public static <T, R> R copy(T from, R to, String... excludedProperties) {
        BeanWrapper wrapper = PropertyAccessorFactory
                .forBeanPropertyAccess(from);
        BeanWrapper toWrapper = PropertyAccessorFactory
                .forBeanPropertyAccess(to);
        Arrays.sort(excludedProperties);

        for (PropertyDescriptor descriptor : wrapper.getPropertyDescriptors()) {
            if (descriptor.getWriteMethod() == null
                    || descriptor.getReadMethod() == null) {
                continue;
            }

            String name = descriptor.getName();
            boolean found = Arrays.binarySearch(excludedProperties, name) >= 0;
            if (found) {
                continue;
            }

            toWrapper.setPropertyValue(name, wrapper.getPropertyValue(name));
        }
        return to;
    }

    public static <T, R> T extend(T target, R source, String... includedProperties) {
        return extend(target, source, false, includedProperties);
    }

    /**
     * @param target
     * @param source
     * @param ignoreNullProperty 是否忽略source里值为空的属性
     * @param includedProperties 未指定时extend所有属性
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> T extend(T target, R source, boolean ignoreNullProperty,
                                  String... includedProperties) {
        BeanWrapper targetWrapper = PropertyAccessorFactory
                .forBeanPropertyAccess(target);
        BeanWrapper sourceWrapper = PropertyAccessorFactory
                .forBeanPropertyAccess(source);

        Arrays.sort(includedProperties);

        for (PropertyDescriptor descriptor : sourceWrapper
                .getPropertyDescriptors()) {
            if (descriptor.getWriteMethod() == null
                    || descriptor.getReadMethod() == null) {
                continue;
            }

            String name = descriptor.getName();
            boolean found = includedProperties.length == 0
                    || Arrays.binarySearch(includedProperties, name) >= 0;
            if (!found) {
                continue;
            }

            if (ignoreNullProperty && sourceWrapper.getPropertyValue(name) == null) {
                continue;
            }

            if (!targetWrapper.isWritableProperty(name)) {
                continue;
            }

            targetWrapper.setPropertyValue(name,
                    sourceWrapper.getPropertyValue(name));
        }
        return target;
    }

    /**
     * 将json传按照字段匹配的
     *
     * @param json
     * @param klass
     * @param <T>
     * @return
     */
    public static <T> T jsonToBean(String json, Class<T> klass) {
        return mapToBean((Map<Object, Object>) JsonUtil.loadList(json, HashMap.class), klass);
    }

    public static <T> List<T> mapToBeanList(List<Map> value, Class<?> type) {
        List list = new ArrayList(value.size());
        for (Map map : value) {
            list.add(mapToBean(map, type));
        }
        return list;
    }

    public static <T> T mapToBean(Map<Object, Object> data, Class<T> klass) {
        try {
            T bean = klass.newInstance();
            for (Map.Entry<Object, Object> entry : data.entrySet()) {
                String fieldName = entry.getKey().toString();
                PropertyDescriptor propertyDescriptor = getPropertyDescriptor(klass, fieldName);
                if (propertyDescriptor != null) {
                    Object value = entry.getValue();
                    Class<?> fieldType = propertyDescriptor.getPropertyType();
                    if (!fieldType.isAssignableFrom(Map.class) && value instanceof List && ((List) value).size() > 0 && ((List) value).get(0) instanceof Map) {
                        if (fieldType.isAssignableFrom(List.class)) {
                            try {
                                fieldType = (Class) ((ParameterizedType) klass.getDeclaredField(propertyDescriptor.getName()).getGenericType()).getActualTypeArguments()[0];
                                value = mapToBeanList(((List) value), fieldType);
                            } catch (Exception e) {
                                //ignore
                            }
                        }
                    }
                    propertyDescriptor.getWriteMethod().invoke(bean, value);
                }
            }
            return bean;
        } catch (Exception e) {
            throw new RuntimeException("将map转为" + klass.getName() + "时出现错误", e);
        }
    }

    private static <T> PropertyDescriptor getPropertyDescriptor(Class<T> klass, String fieldName) {
        PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(klass, fieldName);
        if (propertyDescriptor == null) {
            CaseFormat from = CaseFormat.LOWER_CAMEL;
            if (fieldName.contains("-")) {
                from = CaseFormat.LOWER_HYPHEN;
            } else if (fieldName.contains("_")) {
                if (fieldName.toUpperCase().equals(fieldName)) {
                    from = CaseFormat.UPPER_UNDERSCORE;
                } else {
                    from = CaseFormat.LOWER_UNDERSCORE;
                }
            } else if (fieldName.substring(0, 1).toUpperCase().equals(fieldName.substring(0, 1))) {
                from = CaseFormat.UPPER_CAMEL;
            }
            for (CaseFormat to : toFormat) {
                propertyDescriptor = BeanUtils.getPropertyDescriptor(klass, from.to(to, fieldName));
                if (propertyDescriptor != null) {
                    break;
                }
            }
        }
        return propertyDescriptor;
    }


    public static void main(String[] args) {
        String[] fields = {"lower-hyphen",
                "lower_underscore",
                "lowerCamel",
                "UpperCamel",
                "UPPER_UNDERSCORE"};
        for (String field : fields) {
            CaseFormat from = CaseFormat.LOWER_CAMEL;
            if (field.contains("-")) {
                from = CaseFormat.LOWER_HYPHEN;
            } else if (field.contains("_")) {
                if (field.toUpperCase().equals(field)) {
                    from = CaseFormat.UPPER_UNDERSCORE;
                } else {
                    from = CaseFormat.LOWER_UNDERSCORE;
                }
            } else if (field.substring(0, 1).toUpperCase().equals(field.substring(0, 1))) {
                from = CaseFormat.UPPER_CAMEL;
            }

            System.out.println(from.to(CaseFormat.LOWER_UNDERSCORE, field));
            System.out.println(from.to(CaseFormat.LOWER_CAMEL, field));
            System.out.println(from.to(CaseFormat.UPPER_CAMEL, field));
            System.out.println(from.to(CaseFormat.UPPER_UNDERSCORE, field));
            System.out.println(from.to(CaseFormat.LOWER_HYPHEN, field));

            System.out.println("----------------");
        }
    }
}
