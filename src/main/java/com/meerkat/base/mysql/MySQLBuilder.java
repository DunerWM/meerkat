package com.meerkat.base.mysql;

import com.meerkat.base.db.GenericSQLBuilder;
import com.meerkat.base.db.ISQLBuilder;

public class MySQLBuilder extends GenericSQLBuilder implements ISQLBuilder {

    @Override
    public String getLastInsertIdSQL() {
        return "select last_insert_id()";
    }

}
