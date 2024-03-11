package com.example.rph.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Conversion;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Utility {

    public static ObjectMapper mapper = new ObjectMapper();

    public static ObjectNode convertToObjectNode(final String data) {
        try {
            return (ObjectNode) mapper.readTree(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static long bytesToLong(byte[] bytes) {
        if (bytes.length != 8) {
            throw new IllegalArgumentException("Invalid byte array length for conversion to long");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getLong();
    }

    public static long bytesToInt(byte[] bytes) {
        byte[] copyBytes = Arrays.copyOf(bytes, bytes.length);
        ArrayUtils.reverse(copyBytes);
        return Conversion.byteArrayToInt(copyBytes, 0, 0, 0, copyBytes.length);
    }

}
