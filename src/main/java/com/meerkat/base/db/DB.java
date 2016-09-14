package com.meerkat.base.db;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.meerkat.base.db.sqlite.SQLiteSQLBuilder;
import com.meerkat.base.exception.NoDataUpdatedException;
import com.meerkat.base.lang.Lists;
import com.meerkat.base.lang.Maps;
import com.meerkat.base.mysql.MySQLBuilder;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.*;

public final class DB {
    public static enum Type {
        MySQL(MySQLBuilder.class),

        SQLITE(SQLiteSQLBuilder.class);

        private Type(Class<? extends ISQLBuilder> klass) {
            this.klass = klass;
        }

        private final Class<? extends ISQLBuilder> klass;

        public ISQLBuilder newSQLBuilder() {
            try {
                return klass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Type type;
    private final JdbcTemplate jdbcTemplate;

    public DB(DataSource dataSource) {
        this(dataSource, Type.MySQL);
    }

    public DB(DataSource dataSource, Type dbType) {
        this.type = dbType;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Query from(Class<?> klass) {
        EntityClassWrapper wrapper = EntityClassWrapper.wrap(klass);
        return from(wrapper.getTableName());
    }

    public Query from(String table) {
        return from(table, null);
    }

    public Query from(String table, Object... params) {
        ISQLBuilder isqlBuilder = type.newSQLBuilder();
        if (params != null && params.length > 0) {
            isqlBuilder.addParam(params);
        }
        return new Query(this, jdbcTemplate, isqlBuilder).from(table);
    }

    <T> BeanPropertyRowMapper<T> buildRowMapper(Class<T> klass) {
        return new DBBeanPropertyRowMapper<>(klass);
    }

    public <T> List<T> all(Class<T> klass) {
        return from(klass).all(klass);
    }

    /**
     * 查询符合单个条件的所以记录.
     *
     * @param klass  查询结果类
     * @param column 列名
     * @param value  列值
     * @return List<T>
     */
    public <T> List<T> all(Class<T> klass, String column, Object value) {
        return from(klass).where(column, value).all(klass);
    }

    public <T> List<T> all(Class<T> klass, String column, List<?> values) {
        return all(klass, column, values.toArray());
    }

    public <T> List<T> all(Class<T> klass, String column, Object... values) {
        if (values.length == 0) {
            return new ArrayList<>();
        }
        return from(klass).in(column, values).all(klass);
    }

    /**
     * 查找对应主键的记录, 主键对应的列名为id.
     *
     * @param klass
     * @param id
     * @return T 记录instance
     */
    public <T, P> T find(Class<T> klass, P id) {
        return find(klass, id, false);
    }

    /**
     * 查找对应主键的记录, 主键对应的列名为id.
     *
     * @param klass
     * @param id
     * @param throwException 如果对应记录不存在时，是否抛出RecordNotFoundException.
     * @return 记录instance
     */
    public <T, P> T find(Class<T> klass, P id, boolean throwException) {
        EntityClassWrapper wrapper = EntityClassWrapper.wrap(klass);
        return find(klass, wrapper.getIdColumnField().getColumnName(), id, throwException);
    }

    public <T, P> T find(Class<T> klass, String primaryKey, P id, boolean throwException) {
        return find(klass, null, primaryKey, id, throwException);
    }

    public <T, P> T find(Class<T> klass, String table, String primaryKey, P id, boolean throwException) {
        if (table == null) {
            EntityClassWrapper wrapper = EntityClassWrapper.wrap(klass);
            table = wrapper.getTableName();
        }
        T t = from(table).where(primaryKey, id).first(klass);
        if (t == null && throwException) {
            throw new RecordNotFoundException("no record found for "
                    + klass.getSimpleName() + "(" + id + ")");
        }
        return t;
    }

    private Object[] rewriteParams(Object[] params) {
        for (int i = 0; i < params.length; i++) {
            Object value = params[i];
            if (value instanceof Enum) {
                params[i] = value.toString();
            }
        }
        return params;
    }

    @SuppressWarnings("unchecked")
    private <T> SQLExecutor<T> getSQLExecutor(T entity) {
        return new SQLExecutor<>(jdbcTemplate, (Class<T>) entity.getClass());
    }

    public <T> int insert(T entity) {
        return getSQLExecutor(entity).insert(entity, false);
    }

    public <T> int replace(T entity) {
        return getSQLExecutor(entity).insert(entity, true);
    }

    public <T> int batchInsert(List<T> entities) {
        if (entities.isEmpty()) {
            throw new IllegalArgumentException("entities can't be empty");
        }

        T entity = entities.get(0);
        return getSQLExecutor(entity).batchInsert(entities, false);
    }

    public <T> int batchReplace(List<T> entities) {
        if (entities.isEmpty()) {
            throw new IllegalArgumentException("entities can't be empty");
        }

        T entity = entities.get(0);
        return getSQLExecutor(entity).batchInsert(entities, true);
    }

    public int insert(String table, Object... kv) {
        if (kv.length == 0 || kv.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        Map<String, Object> data = Maps.newHashMap();
        for (int i = 0; i < kv.length; ) {
            String key = (String) kv[i];
            Object value = kv[i + 1];
            data.put(key, value);

            i += 2;
        }

        return insert(table, data);
    }

    public int insert(String table, Map<String, Object> data) {
        if (data.size() == 0) {
            throw new IllegalArgumentException();
        }
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(table).append("(");
        List<String> keys = Lists.newArrayList();
        List<Object> params = Lists.newArrayList();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            keys.add(key);
            params.add(entry.getValue());
        }
        sql.append(Joiner.on(',').join(keys));
        sql.append(") values(");
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(")");

        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

    /**
     * 按主键更新entity的所有属性.
     *
     * @param entity
     * @param <T>
     * @return 影响的记录数
     */
    public <T> int update(T entity) {
        return getSQLExecutor(entity).update(entity);
    }

    /**
     * 按主键更新entity的指定属性.
     *
     * @param entity
     * @param properties
     * @param <T>
     * @return 影响的记录数
     */
    public <T> int update(T entity, String... properties) {
        if (properties == null || properties.length == 0) {
            throw new IllegalArgumentException("properties can't be empty");
        }

        return getSQLExecutor(entity).update(entity, properties);
    }


    /**
     * 按主键更新entity的所有属性.
     *
     * @param entity
     * @param <T>
     * @return 影响的记录数
     */
    public <T> int updateWithCheckVersion(T entity) throws NoDataUpdatedException {
        return getSQLExecutor(entity).updateWithCheckVersion(entity);
    }

    /**
     * 按主键更新entity的指定属性.
     *
     * @param entity
     * @param properties
     * @param <T>
     * @return 影响的记录数
     */
    public <T> int updateWithCheckVersion(T entity, String... properties) throws NoDataUpdatedException {
        if (properties == null || properties.length == 0) {
            throw new IllegalArgumentException("properties can't be empty");
        }

        return getSQLExecutor(entity).updateWithCheckVersion(entity, properties);
    }

    /**
     * 更新数据库记录为指定值，只能更新一列。
     *
     * @param table           表名
     * @param column          要更新的列名
     * @param value           更新值
     * @param condition       条件的sql片段，如id=?
     * @param conditionParams 条件里占位符对应的值
     * @return 更新数据行数
     */
    public int update(String table, String column, Object value,
                      String condition, Object... conditionParams) {
        String[] columns = {column};
        Object[] values = {value};
        return update(table, columns, values, condition, conditionParams);
    }

    public int update(String table, String[] column, Object[] value,
                      String condition, Object... conditionParams) {
        if (Strings.isNullOrEmpty(condition)) {
            throw new IllegalArgumentException("condition can't be blank");
        }
        if (column == null || value == null || value.length == 0 || value.length == 0) {
            throw new IllegalArgumentException("value and column can't be null");
        }
        if (value.length != column.length) {
            throw new IllegalArgumentException("column.size can't match value.size");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(table);
        sql.append(" set ");
        boolean isFirst = true;
        for (int i = 0; i < column.length; i++) {
            if (isFirst) {
                isFirst = false;
            } else {
                sql.append(",");
            }
            sql.append(column[i]).append(" = ?");
        }
        sql.append(" where ").append(condition);
        List<Object> params = Lists.newArrayList();
        params.addAll(Arrays.asList(value));
        params.addAll(Arrays.asList(conditionParams));

        return jdbcTemplate.update(sql.toString(),
                rewriteParams(params.toArray()));
    }

    public <T> int update(Class<T> klass, String column, Object value,
                          String condition, Object... conditionParams) {
        String tableName = EntityClassWrapper.wrap(klass).getTableName();
        return update(tableName, column, value, condition, conditionParams);
    }

    /**
     * 更新updated_at为当前时间.
     *
     * @param entity
     * @param <T>
     * @return
     */
    public <T> int touch(T entity) {
        EntityClassWrapper wrapper = EntityClassWrapper.wrap(entity);
        EntityClassWrapper.ColumnField columnField = wrapper.getColumnField("updatedAt");
        if (columnField == null) {
            throw new IllegalArgumentException("entity缺少updatedAt属性");
        }
        Class<?> type = columnField.getType();
        Object value = null;
        if (type == DateTime.class) {
            value = DateTime.now();
        } else if (type == Date.class) {
            value = new Date();
        } else {
            throw new IllegalArgumentException("不支持的updatedAt类型，目前只支持org.joda.time.DateTime, java.util.Date");
        }
        columnField.set(entity, value);

        return update(entity, "updatedAt");
    }

    public <T> T lock(T entity) {
        Class<T> klass = (Class<T>) entity.getClass();
        EntityClassWrapper wrapper = EntityClassWrapper.wrap(klass);
        EntityClassWrapper.ColumnField idColumnField = wrapper.getIdColumnField();
        Object id = idColumnField.get(entity);

        return lock(klass, id);
    }

    public <T> T lock(Class<T> klass, Object id) {
        EntityClassWrapper wrapper = EntityClassWrapper.wrap(klass);
        EntityClassWrapper.ColumnField idColumnField = wrapper.getIdColumnField();
        return from(klass)
                .where(idColumnField.getColumnName(), id)
                .lock().first(klass);
    }

    public <T> int delete(T entity) {
        return getSQLExecutor(entity).delete(entity);
    }

    /**
     * 按主键删除表中得记录. 主键名为id.
     *
     * @param table 表名
     * @param id    主键值
     * @return 删除的行数
     */
    public <T> int delete(String table, T id) {
        return delete(table, "id", id);
    }

    public <T> int delete(Class<T> klass, T id) {
        EntityClassWrapper wrapper = EntityClassWrapper.wrap(klass);
        return delete(wrapper.getTableName(), "id", id);
    }

    /**
     * 按列值删除记录.
     *
     * @param table  表名
     * @param column 列名
     * @param value  值
     * @return 删除的行数
     */
    public <T> int delete(String table, String column, T value) {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ").append(table);
        sql.append(" where ").append(column).append(" = ?");

        return jdbcTemplate.update(sql.toString(), value);
    }

    /**
     * 执行ddl sql, 如create table, alter table, drop table.
     *
     * @param sql
     */
    public void execute(String sql) {
        jdbcTemplate.execute(sql);
    }

    /**
     * 执行insert, update, delete sql.
     *
     * @param sql
     * @param objects
     * @return 影响的记录数
     */
    public int execute(String sql, Object... objects) {
        return jdbcTemplate.update(sql, objects);
    }

    /**
     * 递归加载所有引用的entity properties.
     *
     * @param e
     * @param <E> entity type
     * @return 原对象
     */
    public <E> E load(E e) {
        return load(e, true);
    }

    @SuppressWarnings("unchecked")
    public <E> E load(E e, boolean recursive) {
        if (e == null) {
            return null;
        }

        if (e instanceof List) {
            return (E) load((List<?>) e, recursive);
        }

        EntityClassWrapper classWrapper = EntityClassWrapper.wrap(e);
        EntityWrapper entityWrapper = new EntityWrapper(e);

        loadReference(e, classWrapper, entityWrapper);
        loadReferences(e, classWrapper, entityWrapper);

        if (recursive) {
            for (EntityClassWrapper.EntityField f : classWrapper.getEntityFields()) {
                Object child = f.get(e);
                load(child, true);
            }
        }

        return e;
    }

    private <E> void loadReferences(E e, EntityClassWrapper classWrapper,
                                    EntityWrapper entityWrapper) {
        for (EntityClassWrapper.ReferencesField f : classWrapper.getReferencesFields()) {
            Class<?> referenceEntityClass = f.getReferenceEntityClasss();
            String orderBy = f.getOrderBy();

            EntityClassWrapper referenceClassWrapper = EntityClassWrapper
                    .wrap(referenceEntityClass);
            EntityClassWrapper.ColumnField columnField = referenceClassWrapper.getColumnField(f
                    .getProperty());

            Query query = from(referenceEntityClass).where(
                    columnField.getColumnName(), entityWrapper.getId());
            if (!Strings.isNullOrEmpty(orderBy)) {
                query.orderBy(orderBy);
            }
            List<?> references = query.all(referenceEntityClass);

            f.set(e, references);
        }
    }

    private <E> void loadReference(E e, EntityClassWrapper classWrapper,
                                   EntityWrapper entityWrapper) {
        for (EntityClassWrapper.ReferenceField f : classWrapper.getReferenceFields()) {
            String referenceProperty = f.getReferenceProperty();
            Class<?> referenceEntityClass = f.getReferenceEntityClasss();

            if (f.isInverse()) {
                EntityClassWrapper referenceClassWrapper = EntityClassWrapper
                        .wrap(referenceEntityClass);
                Object id = entityWrapper.getId();
                EntityClassWrapper.ColumnField referenceEntityField = referenceClassWrapper
                        .getColumnField(referenceProperty);
                Object reference = from(referenceEntityClass).where(
                        referenceEntityField.getColumnName(), id).first(
                        referenceEntityClass);
                f.set(e, reference);
            } else {
                Object referenceId = entityWrapper
                        .getPropertyValue(referenceProperty);
                if (referenceId == null) {
                    continue;
                }
                Object reference = find(referenceEntityClass, referenceId);
                f.set(e, reference);
            }
        }
    }

    public <E> List<E> load(List<E> input) {
        return load(input, true);
    }

    public <E> List<E> load(List<E> input, boolean recursive) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Class<?> klass = input.get(0).getClass();
        EntityClassWrapper classWrapper = EntityClassWrapper.wrap(klass);

        loadReference(input, classWrapper);
        loadReferences(input, classWrapper);

        if (recursive) {
            for (EntityClassWrapper.EntityField f : classWrapper.getEntityFields()) {
                List<?> entities = Lists.map(input, f.getName());
                entities = Lists.flatten(entities);
                entities = Lists.compact(entities);
                load(entities, true);
            }
        }

        return input;
    }

    private <E> void loadReferences(List<E> input,
                                    EntityClassWrapper classWrapper) {
        for (EntityClassWrapper.ReferencesField f : classWrapper.getReferencesFields()) {
            EntityClassWrapper.ColumnField idField = classWrapper.getIdColumnField();
            List<?> ids = Lists.map(input, idField.getName());
            Class<?> referenceEntityClass = f.getReferenceEntityClasss();
            String orderBy = f.getOrderBy();

            EntityClassWrapper referenceClassWrapper = EntityClassWrapper
                    .wrap(referenceEntityClass);
            EntityClassWrapper.ColumnField columnField = referenceClassWrapper.getColumnField(f
                    .getProperty());

            Query query = from(referenceEntityClass).in(
                    columnField.getColumnName(), ids.toArray());
            if (!Strings.isNullOrEmpty(orderBy)) {
                query.orderBy(orderBy);
            }
            List<?> references = query.all(referenceEntityClass);

            for (E e : input) {
                Object id = idField.get(e);
                List<?> values = Lists.filter(references, f.getProperty(), id);
                f.set(e, values);
            }
        }
    }

    private <E> void loadReference(List<E> input,
                                   EntityClassWrapper classWrapper) {
        for (EntityClassWrapper.ReferenceField f : classWrapper.getReferenceFields()) {
            String referenceProperty = f.getReferenceProperty();
            Class<?> referenceEntityClass = f.getReferenceEntityClasss();
            EntityClassWrapper referenceClassWrapper = EntityClassWrapper
                    .wrap(referenceEntityClass);

            if (f.isInverse()) {
                EntityClassWrapper.ColumnField idField = classWrapper.getIdColumnField();
                List<?> idValues = Lists.map(input, idField.getName());

                EntityClassWrapper.ColumnField referenceField = referenceClassWrapper
                        .getColumnField(referenceProperty);
                List<?> referenceValues = from(referenceEntityClass).in(
                        referenceField.getColumnName(), idValues.toArray())
                        .all(referenceEntityClass);
                Map<?, ?> valueMap = Lists.toMap(referenceValues,
                        referenceField.getName());

                for (E e : input) {
                    Object referenceValue = idField.get(e);
                    Object value = valueMap.get(referenceValue);
                    if (value != null) {
                        f.set(e, value);
                    }
                }
            } else {
                List<?> referenceIdValues = Lists.map(input, referenceProperty);
                EntityClassWrapper.ColumnField referenceIdField = referenceClassWrapper
                        .getIdColumnField();
                String idColumnName = referenceIdField.getColumnName();
                List<?> referenceValues = from(referenceEntityClass).in(
                        idColumnName, referenceIdValues.toArray()).all(
                        referenceEntityClass);
                Map<?, ?> valueMap = Lists.toMap(referenceValues,
                        referenceIdField.getName());
                for (E e : input) {
                    Object referenceValue = classWrapper.getColumnField(
                            referenceProperty).get(e);
                    Object value = valueMap.get(referenceValue);
                    if (value != null) {
                        f.set(e, value);
                    }
                }
            }
        }
    }

    public <E> Pagination<E> load(Pagination<E> pagination) {
        load(pagination.getData());
        return pagination;
    }


    public <T> List<T> selectValues(Class<T> klass, String sql, Object... params) {
        if (Util.isPrimitive(klass)) {
            return jdbcTemplate.queryForList(sql, params, klass);
        }

        return jdbcTemplate.query(sql, params, buildRowMapper(klass));
    }

    public <T> T selectValue(Class<T> klass, String sql, Object... params) {
        List<T> values = selectValues(klass, sql, params);
        if (values.size() == 0) {
            return null;
        }
        return values.get(0);
    }
}
