package com.meerkat.base.db;

import com.google.common.collect.AbstractIterator;
import com.meerkat.base.db.EntityClassWrapper.ColumnField;
import com.meerkat.base.lang.Lists;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Query implements Cloneable {
    public static final int DEFAULT_PER_PAGE = 10;

    private static enum ConditionType {
        SEGMENT(null), EQ("="), NOT_EQ("!="),

        /* in, like */
        IN(null), NOT_IN(null), LIKE("like"), NOT_LIKE("not like"),

        /* between */
        BETWEEN(null), NOT_BETWEEN(null),

        /* <, <=, >, >= */
        LESS("<"), LE("<="), GREAT(">"), GE(">="),

        /* null/not null */
        NULL(null), NOT_NULL(null);

        private ConditionType(String op) {
            this.op = op;
        }

        public final String op;
    }

    private static class Condition {
        final ConditionType type;
        final String column;
        final Object[] params;

        public Condition(ConditionType type, String column, Object value) {
            this.type = type;
            this.column = column;
            this.params = toParams(value);
        }

        private Object[] toParams(Object value) {
            if (value == null) {
                return new Object[0];
            }

            if (value.getClass().isArray()) {
                Object[] values = (Object[]) value;
                Object[] params = new Object[values.length];
                for (int i = 0; i < values.length; i++) {
                    params[i] = rewrite(values[i]);
                }
                return params;
            } else {
                return new Object[]{rewrite(value)};
            }
        }

        private Object rewrite(Object value) {
            if (value instanceof Enum) {
                value = value.toString();
            } else if (value instanceof DateTime) {
                value = ((DateTime) value).toDate();
            }

            return value;
        }

        public String toSQL(ISQLBuilder g) {
            if (type.op != null) {
                return g.escapeColumn(column) + " " + type.op + " ?";
            }

            switch (type) {
                case SEGMENT:
                    return "(" + column + ")";
                case IN:
                case NOT_IN:
                    StringBuilder c = new StringBuilder();
                    c.append(g.escapeColumn(column));
                    if (type == ConditionType.NOT_IN) {
                        c.append(" not");
                    }
                    c.append(" in (");
                    for (int i = 0; i < params.length; i++) {
                        if (i > 0) {
                            c.append(", ");
                        }
                        c.append("?");
                    }
                    c.append(")");
                    return c.toString();
                case BETWEEN:
                case NOT_BETWEEN:
                    StringBuilder between = new StringBuilder();
                    between.append(g.escapeColumn(column));
                    if (type == ConditionType.NOT_BETWEEN) {
                        between.append(" not");
                    }
                    between.append(" between ? and ?");
                    return between.toString();
                case NULL:
                    return g.escapeColumn(column) + " is null";
                case NOT_NULL:
                    return g.escapeColumn(column) + " is not null";
                default:
                    throw new IllegalStateException();
            }
        }

    }

    private final DB db;
    private final JdbcTemplate jdbcTemplate;

    private final ISQLBuilder sqlBuilder;

    private String table;
    private boolean eager = false;

    Query(DB db, JdbcTemplate jdbcTemplate, ISQLBuilder sqlBuilder) {
        this.db = db;
        this.jdbcTemplate = jdbcTemplate;
        this.sqlBuilder = sqlBuilder;
    }

    public Query eager(boolean eagerLoad) {
        this.eager = eagerLoad;
        return this;
    }

    public Query clone() {
        return new Query(db, jdbcTemplate, (ISQLBuilder) sqlBuilder.clone());
    }

    public Query from(String table) {
        this.table = table;
        sqlBuilder.setTable(table);
        return this;
    }

    public String getTable() {
        return table;
    }

    public Query groupBy(String groupBy) {
        sqlBuilder.groupBy(groupBy);
        return this;
    }

    public Query having(String having) {
        sqlBuilder.having(having);
        return this;
    }

    public Query limit(Integer rowCount) {
        return limit(0, rowCount);
    }

    /**
     * @param offset   从0开始
     * @param rowCount
     * @return
     */
    public Query limit(Integer offset, Integer rowCount) {
        sqlBuilder.limit(offset, rowCount);
        return this;
    }

    public Query orderBy(String orderBy) {
        if (orderBy == null) {
            return this;
        }
        if (orderBy.endsWith("!")) {
            orderBy = orderBy.substring(0, orderBy.length() - 1) + " desc";
        }
        sqlBuilder.orderBy(orderBy);
        return this;
    }

    public Query select(String... columns) {
        sqlBuilder.select(columns);
        return this;
    }

    public Query join(String join) {
        sqlBuilder.join(join);
        return this;
    }

    public Query tag(String tag) {
        sqlBuilder.tag(tag);
        return this;
    }

    private Query where(Condition c) {
        sqlBuilder.where(c.toSQL(sqlBuilder), c.params);
        return this;
    }

    public Query segment(String sql, Object... params) {
        return where(new Condition(ConditionType.SEGMENT, sql, params));
    }

    public Query eq(String name, Object value) {
        return where(new Condition(ConditionType.EQ, name, value));
    }

    public Query where(String name, Object value) {
        return eq(name, value);
    }

    public Query where(String name, Object value, boolean ignoreNullValue) {
        if (ignoreNullValue && value == null) {
            return this;
        }
        return where(name, value);
    }

    public Query where(String name, Object value, Object... nameValues) {
        return eq(name, value, nameValues);
    }

    public Query eq(String name, Object value, Object... nameValues) {
        if (nameValues.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "nameValues.length % 2 must be 0");
        }
        where(new Condition(ConditionType.EQ, name, value));
        for (int i = 0; i < nameValues.length; i = i + 2) {
            where(new Condition(ConditionType.EQ, (String) nameValues[i],
                    nameValues[i + 1]));
        }
        return this;
    }

    public Query where(Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            where(new Condition(ConditionType.EQ, entry.getKey(),
                    entry.getValue()));
        }
        return this;
    }

    public Query not(String name, Object value) {
        return where(new Condition(ConditionType.NOT_EQ, name, value));
    }

    public Query in(String name, List<?> values) {
        return in(name, values.toArray());
    }

    public Query in(String name, Object... values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("values can't be empty");
        }
        return where(new Condition(ConditionType.IN, name, values));
    }

    public Query notIn(String name, Object... values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("values can't be empty");
        }
        return where(new Condition(ConditionType.NOT_IN, name, values));
    }

    public Query like(String name, Object value) {
        return where(new Condition(ConditionType.LIKE, name, value));
    }

    public Query notLike(String name, Object value) {
        return where(new Condition(ConditionType.NOT_LIKE, name, value));
    }

    public Query between(String name, Object from, Object to) {
        return where(new Condition(ConditionType.BETWEEN, name, new Object[]{
                from, to}));
    }

    public Query notBetween(String name, Object from, Object to) {
        return where(new Condition(ConditionType.NOT_BETWEEN, name,
                new Object[]{from, to}));
    }

    public Query less(String name, Object value, boolean ignoreNullValue) {
        if (ignoreNullValue && value == null) {
            return this;
        }
        return less(name, value);
    }

    public Query less(String name, Object value) {
        return where(new Condition(ConditionType.LESS, name, value));
    }

    public Query lessOrEquals(String name, Object value, boolean ignoreNullValue) {
        if (ignoreNullValue && value == null) {
            return this;
        }
        return lessOrEquals(name, value);
    }

    public Query lessOrEquals(String name, Object value) {
        return where(new Condition(ConditionType.LE, name, value));
    }

    public Query great(String name, Object value, boolean ignoreNullValue) {
        if (ignoreNullValue && value == null) {
            return this;
        }
        return great(name, value);
    }

    public Query great(String name, Object value) {
        return where(new Condition(ConditionType.GREAT, name, value));
    }

    public Query greatOrEquals(String name, Object value, boolean ignoreNullValue) {
        if (ignoreNullValue && value == null) {
            return this;
        }
        return greatOrEquals(name, value);
    }

    public Query greatOrEquals(String name, Object value) {
        return where(new Condition(ConditionType.GE, name, value));
    }

    public Query isNull(String name) {
        return where(new Condition(ConditionType.NULL, name, null));
    }

    public Query isNotNull(String name) {
        return where(new Condition(ConditionType.NOT_NULL, name, null));
    }

    public Pagination<Map<String, Object>> paginate(int page, int pageSize) {
        limit((page - 1) * pageSize, pageSize);

        Integer count = jdbcTemplate.queryForObject(sqlBuilder.toCountSQL(),
                Integer.class, sqlBuilder.getParameters());

        List<Map<String, Object>> data = Lists.newArrayList();
        if (count > (page - 1) * pageSize) {
            data = jdbcTemplate.queryForList(sqlBuilder.toSQL(),
                    sqlBuilder.getParameters());
        }

        Pagination<Map<String, Object>> pagination = new Pagination<Map<String, Object>>(
                page, pageSize, count);
        pagination.setData(data);

        return pagination;
    }

    /**
     * 分页查询，每页大小为10.
     *
     * @param klass
     * @param page  当前页数，从1开始
     * @return Pagination
     */
    public <T> Pagination<T> paginate(Class<T> klass, int page) {
        return paginate(klass, page, DEFAULT_PER_PAGE);
    }

    public <T> Pagination<T> paginate(Class<T> klass, int page, int pageSize) {
        return paginate(klass, null, page, pageSize);
    }

    /**
     * 分页查询.
     *
     * @param klass
     * @param page     当前页数，从1开始
     * @param pageSize 每页大小
     * @return Pagination
     */
    public <T> Pagination<T> paginate(Class<T> klass, RowMapper<T> rowMapper, int page, int pageSize) {
        limit((page - 1) * pageSize, pageSize);

        Integer count = jdbcTemplate.queryForObject(sqlBuilder.toCountSQL(),
                Integer.class, sqlBuilder.getParameters());

        List<T> data = Lists.newArrayList();
        if (count > (page - 1) * pageSize) {
            data = doQuery(klass, rowMapper);
        }

        Pagination<T> pagination = new Pagination<T>(page, pageSize, count);
        pagination.setData(data);

        return pagination;
    }

    public <T> Pagination<T> nextOnlyPaginate(Class<T> klass, int page,
                                              int pageSize) {
        return nextOnlyPaginate(klass, null, page, pageSize);
    }

    public <T> Pagination<T> nextOnlyPaginate(Class<T> klass, RowMapper<T> rowMapper, int page,
                                              int pageSize) {
        limit((page - 1) * pageSize, pageSize + 1);
        List<T> data = doQuery(klass, rowMapper);
        Pagination<T> pagination = null;
        if (data.size() > pageSize) {
            pagination = new Pagination<T>(page, true);
            data.remove(pageSize);
        } else {
            pagination = new Pagination<T>(page, false);
        }
        pagination.setData(data);
        return pagination;
    }

    /**
     * 遍历满足条件的所有记录。
     *
     * @param klass   记录将转换成的类型
     * @param handler
     * @return int 总记录数.
     */
    public <T> int each(Class<T> klass, final RecordHandler<T> handler) {
        final BeanPropertyRowMapper<T> mapper = db.buildRowMapper(klass);

        class EachHandler implements RowCallbackHandler {
            private int rowNumber = 0;

            public int getRowNumber() {
                return rowNumber;
            }

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                T record = mapper.mapRow(rs, rowNumber++);
                handler.process(record);
            }

        }

        EachHandler eachHandler = new EachHandler();
        jdbcTemplate.query(sqlBuilder.toSQL(), sqlBuilder.getParameters(),
                eachHandler);

        return eachHandler.getRowNumber();
    }

    public <T> int forEach(Class<T> klass, final RecordHandler<T> handler) {
        return forEach(klass, handler, 1000);
    }

    /**
     * 遍历满足条件的所有记录, 通过多次查询实现, 每次最多查询1000条记录，对数据库和内存压力较小。
     *
     * @param klass     记录将转换成的类型
     * @param handler
     * @param batchSize
     * @return 总记录数.
     */
    public <T> int forEach(Class<T> klass, final RecordHandler<T> handler,
                           int batchSize) {
        int rows = 0;
        Iterator<T> ite = iterator(klass, batchSize);
        while (ite.hasNext()) {
            T record = ite.next();
            handler.process(record);
            rows++;
        }
        return rows;
    }

    public <T> Iterator<T> iterator(final Class<T> klass) {
        return iterator(klass, 1000, null);
    }

    public <T> Iterator<T> iterator(final Class<T> klass, final int batchSize) {
        return iterator(klass, batchSize, null);
    }

    public <T> Iterator<T> iterator(final Class<T> klass, final int batchSize,
                                    String idAlias) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must > 0");
        }

        EntityClassWrapper wrapper = EntityClassWrapper.wrap(klass);
        final ColumnField idField = wrapper.getIdColumnField();
        if (idField == null) {
            throw new IllegalArgumentException("id field is required");
        }

        final String idColumnName = idAlias == null ? idField.getColumnName()
                : idAlias;

        final Query clone = clone();
        clone.orderBy(idColumnName + " asc").limit(0, batchSize);

        return new AbstractIterator<T>() {
            private List<T> batchData = new ArrayList<T>();
            private Iterator<T> batchIterator;
            private boolean allBatchEnd;

            private Query query = clone;
            private Object maxId;

            @Override
            protected T computeNext() {
                if (batchIterator == null || !batchIterator.hasNext()) {
                    if (allBatchEnd && !batchIterator.hasNext()) {
                        return endOfData();
                    }

                    if (maxId != null) {
                        query = clone.clone();
                        query.great(idColumnName, maxId);
                    }

                    batchData.clear();
                    int rows = query.each(klass, new RecordHandler<T>() {

                        @Override
                        public void process(T record) {
                            batchData.add(record);
                            maxId = idField.get(record);
                        }

                    });

                    if (rows < batchSize) {
                        allBatchEnd = true;
                    }

                    batchIterator = batchData.iterator();
                }

                if (!batchIterator.hasNext()) {
                    return endOfData();
                }

                return batchIterator.next();
            }
        };
    }

    public boolean isExists() {
        limit(1);
        return all().size() > 0;
    }

    public Map first() {
        return first(HashMap.class, false);
    }

    /**
     * 得到符合条件的第一条记录.
     *
     * @param klass
     * @return T 没找到任何记录时返回null.
     */
    public <T> T first(Class<T> klass) {
        return first(klass, false);
    }

    public <T> T first(Class<T> klass, boolean throwIfNotFound) {
        return first(klass, null, throwIfNotFound);
    }

    /**
     * 得到符合条件的第一条记录.
     *
     * @param klass
     * @param throwIfNotFound 是否在没有记录时抛出RecordNotFoundException, 如果为false, 在没有找到任何记录时返回null
     * @return T
     */
    public <T> T first(Class<T> klass, RowMapper<T> rowMapper, boolean throwIfNotFound) {
        if (!sqlBuilder.hasLimit()) {
            sqlBuilder.limit(0, 1);
        }
        List<T> data = doQuery(klass, rowMapper);

        if (data.size() == 0) {
            if (throwIfNotFound) {
                throw new RecordNotFoundException("no record found for "
                        + klass.getSimpleName());
            }
            return null;
        }
        return data.get(0);
    }

    public <T> List<T> all(Class<T> klass) {
        return doQuery(klass, null);
    }

    public <T> List<T> all(Class<T> klass, RowMapper<T> rowMapper) {
        return doQuery(klass, rowMapper);
    }

    public List<Map<String, Object>> all() {
        return jdbcTemplate.queryForList(sqlBuilder.toSQL(),
                sqlBuilder.getParameters());
    }

    public int count() {
        sqlBuilder.clearSelect();
        select("count(*)");
        Integer count = first(Integer.class);
        return count == null ? 0 : count;
    }

    public Query lock() {
        sqlBuilder.lock();
        return this;
    }

    private <T> List<T> doQuery(Class<T> klass, RowMapper<T> rowMapper) {
        List<T> data = null;
        if (Util.isPrimitive(klass)) {
            data = jdbcTemplate.queryForList(sqlBuilder.toSQL(),
                    sqlBuilder.getParameters(), klass);
        } else {
            if (rowMapper == null) {
                rowMapper = db.buildRowMapper(klass);
            }

            data = jdbcTemplate.query(sqlBuilder.toSQL(),
                    sqlBuilder.getParameters(), new UniqueFieldRowMapperResultSetExtractor<T>(rowMapper));
        }
        if (eager) {
            db.load(data);
        }
        return data;
    }

    public String toSQL() {
        return sqlBuilder.toSQL();
    }
}
