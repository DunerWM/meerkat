package com.meerkat.base.db.support;


import org.joda.time.DateTime;
import org.springframework.core.convert.converter.Converter;

import java.util.Date;

public class DateToDateTimeConverter implements Converter<Date, DateTime> {
    @Override
    public DateTime convert(Date source) {
        if (source == null) {
            return null;
        }
        return new DateTime(source);
    }
}
