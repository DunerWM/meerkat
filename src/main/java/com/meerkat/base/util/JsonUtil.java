package com.meerkat.base.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.StringWriter;
import java.util.List;

public final class JsonUtil {
    @SuppressWarnings("serial")
    public static class CodecException extends RuntimeException {
        public CodecException(Throwable cause) {
            super(cause);
        }

        public CodecException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private JsonUtil() {
    }

    public static String dump(Object obj) throws CodecException {
        return dump(obj, null);
    }

    public static String dump(Object obj, JsonInclude.Include inclusion)
            throws CodecException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.setSerializationInclusion(inclusion);
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, obj);
        } catch (Exception e) {
            throw new CodecException("can't dump object :" + obj, e);
        }
        return writer.toString();
    }

    public static <T> T load(String json, Class<T> type) throws CodecException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new CodecException("parse content error, content: " + json, e);
        }
    }

    public static <T> List<T> loadList(String jsonArray, Class<T> type) throws CodecException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonArray,
                    mapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (Exception e) {
            throw new CodecException("parse content error, content: " + jsonArray, e);
        }
    }

}
