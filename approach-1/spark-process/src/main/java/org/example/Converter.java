package org.example;

import org.example.common.model.SparkFileSplitRequest;
import org.example.common.utility.Utility;

import java.io.File;
import java.io.IOException;

public class Converter {

    public SparkFileSplitRequest getConfig(final String configFileLocation) {
        try {
           return  Utility.getObjectMapper().readValue(new File(configFileLocation), SparkFileSplitRequest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
