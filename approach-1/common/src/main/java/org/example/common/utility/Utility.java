package org.example.common.utility;
import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Utility {

    public static void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean result = directory.mkdirs();
            if (result) {
                log.debug("Directory was created successfully {}", directoryPath);
            } else {
                log.error("Failed to create directory {}", directoryPath);
            }
        }
    }

    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

}
