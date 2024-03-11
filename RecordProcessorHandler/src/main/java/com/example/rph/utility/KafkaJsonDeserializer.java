package com.example.rph.utility;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

import static com.example.rph.utility.Utility.mapper;

public class KafkaJsonDeserializer implements Deserializer {

    @Override
    public JsonNode deserialize(String s, byte[] bytes) {
        try {
            JsonNode jsonNode = mapper.readTree(bytes);
            System.out.println(jsonNode);
            return jsonNode;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
