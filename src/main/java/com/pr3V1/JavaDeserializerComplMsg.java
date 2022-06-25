package com.pr3V1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class JavaDeserializerComplMsg implements Deserializer<CompletitionOfTaskMsg> {
    @Override public void close() {
    }
    @Override public void configure(Map<String, ?> arg0, boolean arg1) {
    }
    @Override
    public CompletitionOfTaskMsg deserialize(String arg0, byte[] arg1) {
        ObjectMapper mapper = new ObjectMapper();
        CompletitionOfTaskMsg user = null;
        try {
            user = mapper.readValue(arg1, CompletitionOfTaskMsg.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
}