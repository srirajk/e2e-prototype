package org.example;

import org.example.common.model.FileRequestLineEvent;
import org.example.common.model.SparkFileSplitRequest;
import org.example.common.utility.Utility;

import java.io.File;
import java.io.IOException;

public class Converter {

    public SparkFileSplitRequest getSparkFileSplitRequestConfig(final String configFileLocation) {
        try {
           return  Utility.getObjectMapper().readValue(new File(configFileLocation), SparkFileSplitRequest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FileRequestLineEvent getFileRequestLineEvent(final byte[] fileRequestLineEventInBytes) {
        try {
            return  Utility.getObjectMapper().readValue(fileRequestLineEventInBytes, FileRequestLineEvent.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String removeExtraSpacesAndSpecialCharacters(final String input) {
        return input.replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ");
    }


    public KafkaStreamingConfig getKafkaStreamingConfigConfig(final String configFileLocation) {
        try {
            return  Utility.getObjectMapper().readValue(new File(configFileLocation), KafkaStreamingConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
