package com.meerkat.base.db;

public interface ISQLBuilder extends Cloneable {
    ISQLBuilder clone();

    void setTable(String table);

    Object[] getParameters();

    String toCountSQL();

    String toSQL();

    String escapeColumn(String column);

    void tag(String tag);

    void addParam(Object... tag);

    void where(String condition, Object... params);

    void select(String... columns);

    void clearSelect();

    void join(String joinSql);

    void groupBy(String groupBy);

    void having(String having);

    void orderBy(String orderBy);

    void limit(Integer offset, Integer rowCount);

    boolean hasLimit();

    void lock();

    String getLastInsertIdSQL();
}