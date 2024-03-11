package com.example.utility;

import org.apache.kafka.common.serialization.Serializer;

import static com.example.utility.UtilityConverter.mapper;

public class KafkaJsonSerializer implements Serializer {


    @Override
    public byte[] serialize(String s, Object o) {
        byte[] retVal = null;
        try {
            retVal = mapper.writeValueAsBytes(o);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return retVal;
    }
}
