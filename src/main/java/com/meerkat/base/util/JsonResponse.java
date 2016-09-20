package com.meerkat.base.util;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wm on 16/9/20.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class JsonResponse {

    private boolean success;
    private String message;
    private List<String> messages = new ArrayList<String>();
    private Map<String, Object> data = new HashMap<String, Object>();

    public JsonResponse() {
    }

    public JsonResponse(boolean success) {
        this(success, null);
    }

    public JsonResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void appendMessage(String msg) {
        messages.add(msg);
    }

    public List<String> getMessages() {
        return messages;
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "JsonResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", messages=" + messages +
                ", data=" + data +
                '}';
    }
}

