package com.meerkat.base.util;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.apache.commons.lang.StringUtils;

/**
 * Created by wm on 16/9/27.
 */
public class PasswordEncoder {

    public static String encodePassword(String password, String salt) {
        if (StringUtils.isBlank(password)) {
            return null;
        }
        if (Boolean.valueOf(ConfigPropertiesUtil.getValue("login.password.encrypt"))) {
            return Hashing.sha256().hashString(mergePasswordAndSalt(password, salt, true), Charsets.UTF_8).toString();
        } else {
            return password;
        }
    }

    public static void main(String[] args) {
        System.out.println(new PasswordEncoder().encodePassword("huoqiu123", "f1a781eaa880d1dea796878dd4904df1d0fce5dbb1d3e3adc9544236f016692e"));
    }

    protected static String mergePasswordAndSalt(String password, Object salt, boolean strict) {
        if (password == null) {
            password = "";
        }

        if (strict && (salt != null)) {
            if ((salt.toString().lastIndexOf("{") != -1) || (salt.toString().lastIndexOf("}") != -1)) {
                throw new IllegalArgumentException("Cannot use { or } in salt.toString()");
            }
        }

        if ((salt == null) || "".equals(salt)) {
            return password;
        } else {
            return password + "{" + salt.toString() + "}";
        }
    }

}
