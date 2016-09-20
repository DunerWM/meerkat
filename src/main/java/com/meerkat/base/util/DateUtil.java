package com.meerkat.base.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wm on 16/9/19.
 */
public final class DateUtil {

    /**
     * 构造方法私有化
     */
    private DateUtil() {
    }

    public static String dataToString(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

}
