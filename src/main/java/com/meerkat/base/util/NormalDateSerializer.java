package com.meerkat.base.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wm on 16/9/19.
 */
public class NormalDateSerializer extends JsonSerializer<Date> {
    @Override
    public void serialize(Date value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = formatter.format(value);
        generator.writeString(formattedDate);
    }
}
