package com.meerkat.base.db;

import com.meerkat.base.db.support.DateToDateTimeConverter;
import com.meerkat.base.db.support.LongToDateTimeConverter;
import org.joda.time.DateTime;
import org.springframework.beans.BeanWrapper;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DBBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {

    private static final ConversionService conversionService;

    static {
        DefaultConversionService defaultConversionService = new DefaultConversionService();
        defaultConversionService.addConverter(Date.class, DateTime.class, new DateToDateTimeConverter());
        defaultConversionService.addConverter(Long.class, DateTime.class, new LongToDateTimeConverter());
        conversionService = defaultConversionService;
    }

    public DBBeanPropertyRowMapper(Class<T> mappedClass) {
        super(mappedClass);
        setPrimitivesDefaultedForNullValue(true);
    }

    protected void initBeanWrapper(BeanWrapper bw) {
        bw.setConversionService(conversionService);
    }

    protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
        if (pd.getPropertyType() == DateTime.class) {
            return JdbcUtils.getResultSetValue(rs, index, Date.class);
        }
        if (pd.getPropertyType() == BigDecimal.class) {
            String value = rs.getString(index);
            if (value == null) {
                return null;
            }
            return new BigDecimal(value);
        }

        return super.getColumnValue(rs, index, pd);
    }

}
