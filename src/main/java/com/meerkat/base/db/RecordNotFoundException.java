package com.meerkat.base.db;

import org.springframework.core.NestedRuntimeException;

@SuppressWarnings("serial")
public class RecordNotFoundException extends NestedRuntimeException {

    public RecordNotFoundException(String msg) {
        super(msg);
    }

    public RecordNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
