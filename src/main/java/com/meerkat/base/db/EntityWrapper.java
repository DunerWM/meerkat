package com.meerkat.base.db;

public class EntityWrapper {
    private final Object entity;
    private final EntityClassWrapper entityClassWrapper;

    public EntityWrapper(Object entity) {
        this.entity = entity;
        entityClassWrapper = EntityClassWrapper.wrap(entity);
    }

    public String getIdProperty() {
        if (!entityClassWrapper.isIdFieldPresent()) {
            return null;
        }

        return entityClassWrapper.getIdColumnField().getName();
    }

    public Object getId() {
        return getPropertyValue(getIdProperty());
    }

    public Object getPropertyValue(String fieldName) {
        EntityClassWrapper.ColumnField field = entityClassWrapper.getColumnField(fieldName);
        return field.get(entity);
    }

    public String getTableName() {
        return entityClassWrapper.getTableName();
    }

    public String getIdColumnName() {
        return getColumnName(getIdProperty());
    }

    public String getColumnName(String fieldName) {
        EntityClassWrapper.ColumnField field = entityClassWrapper.getColumnField(fieldName);
        return field.getColumnName();
    }
}
