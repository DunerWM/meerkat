package com.meerkat.base.db;

public interface RecordHandler<T> {

    void process(T record);

}
