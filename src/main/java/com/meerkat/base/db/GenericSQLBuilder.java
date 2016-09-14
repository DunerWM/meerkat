package com.meerkat.base.db;

import com.google.common.base.Joiner;
import com.meerkat.base.lang.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericSQLBuilder implements ISQLBuilder {
    protected String table;
    protected String tag;

    protected List<String> columns = new ArrayList<String>();
    protected List<String> joins = new ArrayList<String>();
    protected List<String> conditions = new ArrayList<String>();
    protected List<String> groupBys = new ArrayList<String>();
    protected List<String> havings = new ArrayList<String>();
    protected List<String> orderBys = new ArrayList<String>();

    protected boolean lockForUpdate;

    protected Integer offset;
    protected Integer rowCount;

    protected List<Object> parameters = new ArrayList<Object>();

    @Override
    public ISQLBuilder clone() {
        try {
            GenericSQLBuilder clone = (GenericSQLBuilder) super.clone();

            clone.columns = new ArrayList<String>(columns);
            clone.joins = new ArrayList<String>(joins);
            clone.conditions = new ArrayList<String>(conditions);
            clone.groupBys = new ArrayList<String>(groupBys);
            clone.havings = new ArrayList<String>(havings);
            clone.orderBys = new ArrayList<String>(orderBys);
            clone.parameters = new ArrayList<Object>(parameters);
            clone.lockForUpdate = lockForUpdate;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public Object[] getParameters() {
        return parameters.toArray();
    }

    @Override
    public String toCountSQL() {
        if (groupBys.isEmpty()) {
            StringBuilder sql = new StringBuilder("select ");
            fillTag(sql);
            sql.append(" count(*) from ");
            sql.append(table).append(toJoinSQL());
            if (!conditions.isEmpty()) {
                sql.append(" where ");
                for (int i = 0; i < conditions.size(); i++) {
                    if (i > 0) {
                        sql.append(" and ");
                    }
                    sql.append(conditions.get(i));
                }
            }

            return sql.toString();
        } else {
            //避免子查询里包含"sql_cache"关键字

            String countSql = toSQL0(true, false, false).toLowerCase();
            int charLen = countSql.length();
            countSql = countSql.replace("sql_cache", "");

            StringBuffer sqlBuffer = new StringBuffer(countSql.length() + 40);
            if (charLen == countSql.length()) {
                sqlBuffer.append("select count(*) from (");
            } else {
                sqlBuffer.append("select sql_cache count(*) from(");
            }
            sqlBuffer.append(countSql);
            sqlBuffer.append(") a");
            return sqlBuffer.toString();
        }
    }

    @Override
    public String toSQL() {
        return toSQL0(true, true, true);
    }

    protected String toSQL0(boolean withGroup, boolean withOrderBy,
                            boolean withLimit) {
        StringBuilder sql = new StringBuilder("select ");
        fillTag(sql);
        sql.append(toSelectColumns()).append(" from ").append(table)
                .append(toJoinSQL());
        if (!conditions.isEmpty()) {
            sql.append(" where ");
            for (int i = 0; i < conditions.size(); i++) {
                if (i > 0) {
                    sql.append(" and ");
                }
                sql.append(conditions.get(i));
            }
        }

        if (withGroup) {
            sql.append(toGroupBySQL()).append(toHavingSQL());
        }
        if (withOrderBy) {
            sql.append(toOrderBySQL());
        }
        if (withLimit) {
            sql.append(toLimitSQL());
        }

        if (lockForUpdate) {
            sql.append(" for update");
        }

        return sql.toString();
    }

    private void fillTag(StringBuilder sql) {
        if (!Strings.isBlank(tag)) {
            sql.append("/* tag: ").append(tag).append(" */ ");
        }
    }

    protected String toSelectColumns() {
        if (columns.isEmpty()) {
            return "*";
        }
        return Joiner.on(", ").join(columns);
    }

    protected String toJoinSQL() {
        if (joins.isEmpty()) {
            return "";
        }
        return "\n" + Joiner.on("\n").join(joins);
    }

    protected String toGroupBySQL() {
        if (groupBys.isEmpty()) {
            return "";
        }

        return " group by " + Joiner.on(", ").join(groupBys);
    }

    protected String toHavingSQL() {
        if (havings.isEmpty()) {
            return "";
        }
        return " having " + Joiner.on(" and ").join(havings);
    }

    protected String toOrderBySQL() {
        if (orderBys.isEmpty()) {
            return "";
        }

        return " order by " + Joiner.on(", ").join(orderBys);
    }

    protected String toLimitSQL() {
        if (offset == null) {
            return "";
        }
        return " limit " + offset + ", " + rowCount;
    }

    @Override
    public String escapeColumn(String column) {
        return column;
    }

    @Override
    public void tag(String tag) {
        this.tag = tag;
    }

    @Override
    public void addParam(Object... params) {
        for (Object param : params) {
            parameters.add(param);
        }
    }

    @Override
    public void where(String condition, Object... params) {
        conditions.add(condition);
        addParam(params);

    }

    @Override
    public void select(String... columns) {
        this.columns.addAll(Arrays.asList(columns));
    }

    @Override
    public void clearSelect() {
        this.columns.clear();
    }

    @Override
    public void join(String joinSql) {
        joins.add(joinSql);
    }

    @Override
    public void groupBy(String groupBy) {
        groupBys.add(groupBy);
    }

    @Override
    public void having(String having) {
        havings.add(having);
    }

    @Override
    public void orderBy(String orderBy) {
        orderBys.add(orderBy);
    }

    @Override
    public void limit(Integer offset, Integer rowCount) {
        this.offset = offset;
        this.rowCount = rowCount;
    }

    @Override
    public boolean hasLimit() {
        return offset != null;
    }

    public void lock() {
        lockForUpdate = true;
    }

    @Override
    public String getLastInsertIdSQL() {
        return null;
    }

}