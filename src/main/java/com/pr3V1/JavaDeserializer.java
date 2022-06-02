package com.pr3V1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class JavaDeserializer implements Deserializer<TaskMsg> {
    @Override public void close() {
    }
    @Override public void configure(Map<String, ?> arg0, boolean arg1) {
    }
    @Override
    public TaskMsg deserialize(String arg0, byte[] arg1) {
        ObjectMapper mapper = new ObjectMapper();
        TaskMsg user = null;
        try {
            user = mapper.readValue(arg1, TaskMsg.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
}