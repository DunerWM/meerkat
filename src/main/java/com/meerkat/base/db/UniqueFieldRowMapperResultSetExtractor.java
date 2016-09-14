package com.meerkat.base.db;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gccmx on 15/12/8.
 */
public class UniqueFieldRowMapperResultSetExtractor<T> extends RowMapperResultSetExtractor<T> {

    public UniqueFieldRowMapperResultSetExtractor(RowMapper rowMapper) {
        super(rowMapper);
    }

    public UniqueFieldRowMapperResultSetExtractor(RowMapper rowMapper, int rowsExpected) {
        super(rowMapper, rowsExpected);
    }


    @Override
    public List<T> extractData(ResultSet rs) throws SQLException {
        checkUniqueField(rs);
        return super.extractData(rs);
    }

    protected void checkUniqueField(ResultSet rs) throws SQLException {
        ResultSetMetaData resultSetMetaData = rs.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        Set<String> columnNameSet = new HashSet();
        for (int i = 1; i <= columnCount; i++) {
            String name = StringUtils.isBlank(resultSetMetaData.getColumnLabel(i)) ? resultSetMetaData.getColumnName(i) : resultSetMetaData.getColumnLabel(i);
            columnNameSet.add(name);
        }
        if (columnNameSet.size() != columnCount) {
            throw new SQLException("the column name of result set is not unique");
        }
    }
}
