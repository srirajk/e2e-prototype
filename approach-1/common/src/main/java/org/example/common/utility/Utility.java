package org.example.common.utility;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Utility {

    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

}
