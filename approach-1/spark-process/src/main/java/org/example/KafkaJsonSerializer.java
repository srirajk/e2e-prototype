package org.example;

import org.apache.kafka.common.serialization.Serializer;
import org.example.common.utility.Utility;
public class KafkaJsonSerializer implements Serializer {
    @Override
    public byte[] serialize(String s, Object o) {
        byte[] retVal = null;
        try {
            retVal = Utility.getObjectMapper().writeValueAsBytes(o);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return retVal;
    }
}
