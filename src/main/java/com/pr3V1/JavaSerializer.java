package com.pr3V1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pr3V1.TaskMsg;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JavaSerializer implements Serializer<TaskMsg> {
    @Override public void configure(Map<String, ?> map, boolean b) {
    }
    @Override public byte[] serialize(String arg0, TaskMsg arg1) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsString(arg1).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }
    @Override public void close() {
    }
}