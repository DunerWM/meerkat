package com.meerkat.base.db;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.meerkat.base.db.annotation.EntityReference;
import com.meerkat.base.db.annotation.EntityReferences;
import com.meerkat.base.lang.Reflects;
import com.meerkat.base.lang.Reflects.ClassAnnotation;
import org.joda.time.DateTime;
import org.springframework.dao.DataAccessException;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EntityClassWrapper {

    private static ConcurrentHashMap<Class<?>, EntityClassWrapper> cache;

    static {
        cache = new ConcurrentHashMap<Class<?>, EntityClassWrapper>();
    }

    @SuppressWarnings("serial")
    static class BadFieldException extends DataAccessException {

        public BadFieldException(String msg, Throwable cause) {
            super(msg, cause);
        }

    }

    public static abstract class BaseField {
        protected final Field field;

        public BaseField(Field field) {
            field.setAccessible(true);
            this.field = field;
        }

        public Field getField() {
            return field;
        }

        public String getName() {
            return field.getName();
        }

        public Class<?> getType() {
            return field.getType();
        }

        public Object get(Object entity) {
            try {
                return field.get(entity);
            } catch (Exception e) {
                throw new BadFieldException(e.getMessage(), e);
            }
        }

        public Object getJdbcValue(Object entity) {
            Object value = get(entity);
            if (value instanceof Enum) {
                value = value.toString();
            } else if (value instanceof DateTime) {
                value = ((DateTime) value).toDate();
            }
            return value;
        }

        public void set(Object entity, Object value) {
            try {
                field.set(entity, value);
            } catch (Exception e) {
                throw new BadFieldException(e.getMessage(), e);
            }
        }
    }

    public static class ColumnField extends BaseField {
        private Id id;
        private Column column;
        private String columnName;

        private boolean isUpdatedAt;
        private boolean isCreatedAt;
        private boolean isVersion;


        public ColumnField(Field field) {
            super(field);

            id = field.getAnnotation(Id.class);
            column = field.getAnnotation(Column.class);

            if (column != null && !Strings.isNullOrEmpty(column.name())) {
                columnName = column.name();
            } else {
                columnName = CaseFormat.LOWER_CAMEL.to(
                        CaseFormat.LOWER_UNDERSCORE, field.getName());
            }

            isUpdatedAt = "updated_at".equalsIgnoreCase(columnName);
            isCreatedAt = "created_at".equalsIgnoreCase(columnName);
            isVersion = "version".equalsIgnoreCase(columnName);

        }

        public boolean isId() {
            return id != null;
        }

        public String getColumnName() {
            return columnName;
        }

        public boolean isUpdatedAt() {
            return isUpdatedAt;
        }

        public boolean isCreatedAt() {
            return isCreatedAt;
        }

        public boolean isTimestamp() {
            return isUpdatedAt || isCreatedAt;
        }

        public boolean isVersion() {
            return isVersion;
        }
    }

    public static class ReferenceField extends BaseField {
        private final EntityReference reference;

        public ReferenceField(Field field) {
            super(field);
            reference = field.getAnnotation(EntityReference.class);
        }

        public EntityReference getReference() {
            return reference;
        }

        public String getReferenceProperty() {
            String referenceProperty = reference.referenceProperty();
            if (Strings.isNullOrEmpty(referenceProperty)) {
                referenceProperty = field.getName() + "Id";
            }
            return referenceProperty;
        }

        public boolean isInverse() {
            return reference.inverse();
        }

        public Class<?> getReferenceEntityClasss() {
            return field.getType();
        }
    }

    public static class ReferencesField extends BaseField {
        private final EntityReferences references;

        public ReferencesField(Field field) {
            super(field);
            references = field.getAnnotation(EntityReferences.class);
        }

        public EntityReferences getReferences() {
            return references;
        }

        public Class<?> getReferenceEntityClasss() {
            return references.referenceClass();
        }

        public String getProperty() {
            return references.property();
        }

        public String getOrderBy() {
            return references.orderBy();
        }
    }

    public static class EntityField extends BaseField {

        public EntityField(Field field) {
            super(field);
        }

    }

    private final Class<?> klass;
    private final List<Field> fields;
    private final Table table;
    private final String tableName;
    private final LinkedHashMap<String, ColumnField> columnFields;
    private final ColumnField idColumnField;
    private final ColumnField versionColumnField;
    private final LinkedHashMap<String, ReferenceField> referenceFields;
    private final LinkedHashMap<String, ReferencesField> referencesFields;
    private final List<EntityField> entityFields;//EntityReference or EntityReferences

    public EntityClassWrapper(Class<?> klass) {
        if (!Reflects.isAnnotationPresent(klass, Table.class)) {
            throw new IllegalArgumentException("no Table annotation");
        }

        this.klass = klass;
        ClassAnnotation<Table> ca = Reflects.getAnnotation(klass, Table.class);
        this.table = ca.getAnnotation();

        String name = table.name();
        if (Strings.isNullOrEmpty(name)) {
            name = ca.getKlass().getSimpleName();
            tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,
                    name);
        } else {
            tableName = name;
        }

        fields = Reflects.getFields(klass, true);

        columnFields = new LinkedHashMap<String, ColumnField>();
        scanColumnFields();

        idColumnField = scanIdField();
        versionColumnField = scanVersionField();
        referenceFields = new LinkedHashMap<String, ReferenceField>();
        scanReferenceFields();

        referencesFields = new LinkedHashMap<String, ReferencesField>();
        scanReferencesFields();

        entityFields = Lists.newArrayList();
        scanEntityFields();
    }

    private void scanEntityFields() {
        for (Field field : fields) {
            Class<?> type = field.getType();
            if (Reflects.isAnnotationPresent(type, Table.class) ||
                    field.getAnnotation(EntityReference.class) != null ||
                    field.getAnnotation(EntityReferences.class) != null) {
                entityFields.add(new EntityField(field));
            }
        }
    }

    private void scanColumnFields() {
        for (Field field : fields) {
            Id id = field.getAnnotation(Id.class);
            Column column = field.getAnnotation(Column.class);
            if (id == null && column == null) {
                continue;
            }

            ColumnField entityField = new ColumnField(field);
            columnFields.put(entityField.getName(), entityField);
        }
    }

    private ColumnField scanTimestampField(String name) {
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }

            if (name.equals(field.getName())) {//column name?
                return new ColumnField(field);
            }
        }
        return null;
    }

    private ColumnField scanIdField() {
        for (Field field : fields) {
            Id id = field.getAnnotation(Id.class);
            if (id != null) {
                return new ColumnField(field);
            }
        }
        return null;
    }

    private ColumnField scanVersionField() {
        for (Field field : fields) {
            NotVersion notVersion = field.getAnnotation(NotVersion.class);
            if (notVersion == null && field.getName().equalsIgnoreCase("version")) {
                return new ColumnField(field);
            }
        }
        return null;
    }


    private void scanReferenceFields() {
        for (Field field : fields) {
            EntityReference reference = field
                    .getAnnotation(EntityReference.class);
            if (reference == null) {
                continue;
            }

            ReferenceField referenceField = new ReferenceField(field);
            referenceFields.put(referenceField.getName(), referenceField);
        }
    }

    private void scanReferencesFields() {
        for (Field field : fields) {
            EntityReferences references = field
                    .getAnnotation(EntityReferences.class);
            if (references == null) {
                continue;
            }

            ReferencesField referencesField = new ReferencesField(field);
            referencesFields.put(referencesField.getName(), referencesField);
        }
    }

    public Class<?> getWrappedClass() {
        return klass;
    }

    public Collection<ColumnField> getColumnFields() {
        return columnFields.values();
    }

    public boolean isIdPresent(Object entity) {
        if (!isIdFieldPresent()) {
            return false;
        }
        Object value = idColumnField.get(entity);
        return value != null;
    }

    public boolean isIdFieldPresent() {
        return idColumnField != null;
    }

    public ColumnField getIdColumnField() {
        return idColumnField;
    }

    public ColumnField getColumnField(String fieldName) {
        return columnFields.get(fieldName);
    }

    public String getTableName() {
        return tableName;
    }

    public Collection<ReferenceField> getReferenceFields() {
        return referenceFields.values();
    }

    public Collection<ReferencesField> getReferencesFields() {
        return referencesFields.values();
    }

    public List<EntityField> getEntityFields() {
        return entityFields;
    }

    public ColumnField getVersionColumnField() {
        return versionColumnField;
    }

    public static EntityClassWrapper wrap(Object entity) {
        return wrap(entity.getClass());
    }

    public static EntityClassWrapper wrap(Class<?> klass) {
        EntityClassWrapper wrapper = null;
        wrapper = cache.get(klass);
        if (wrapper != null) {
            return wrapper;
        }

        wrapper = new EntityClassWrapper(klass);
        EntityClassWrapper oldWrapper = cache.putIfAbsent(klass, wrapper);
        if (oldWrapper != null) {
            return oldWrapper;
        }
        return wrapper;
    }

}
