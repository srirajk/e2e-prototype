package org.example.common.utility;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Log4j2
@Builder
public class SparkDeploymentUtility {
    private final String sparkSubmitPath;
    private final String jarPath;
    private final String mainClass;
    private final String master;
    private final ObjectMapper mapper;
    private final String configFileLocation;

    public <mapper> SparkDeploymentUtility(final String sparkSubmitPath, final String jarPath,
                                           final String mainClass,
                                           final String master, final ObjectMapper mapper,
                                           final String configFileLocation) {
        this.sparkSubmitPath = sparkSubmitPath;
        this.jarPath = jarPath;
        this.mainClass = mainClass;
        this.master = master;
        this.mapper = mapper;
        this.configFileLocation = configFileLocation;
    }

    public void deploySparkJob(final String appName,
                               final int executors,
                               final int executorMemory,
                               final int driverMemory,
                               final ObjectNode configFile,
                               final List<String> args) {

        log.info("Deploying Spark job with master: {}, appName: {}, jarPath: {}, mainClass: {}, executors: {}, executorMemory: {}, driverMemory: {}, configFile: {}, args: {}",
                master, appName, jarPath, mainClass, executors, executorMemory, driverMemory, configFile, args);
        try {
            String configFileLocation = this.configFileLocation + "/" + appName + "-config.json";
            File file = new File(configFileLocation);
            mapper.writeValue(file, configFile);
            List<String> command = new ArrayList<>(List.of(
                    sparkSubmitPath,
                    "--master", master,
                    "--driver-memory", String.valueOf(driverMemory) + "g",
                    "--executor-memory", String.valueOf(executorMemory) + "g",
                    "--class", mainClass,
                    jarPath, appName, master, configFileLocation));
            log.info("Command: {}", command);
            command.addAll(args);
            log.info("Full Command: {}", command);

      /*      /Users/srirajkadimisetty/tools/spark-3.5.1-bin-hadoop3-scala2.13/bin/spark-submit
                --master "local[*]"
                --driver-memory 1g
                --executor-memory 2g
                --class com.example.ReadTextFileAndGenerateRecord
                    /Users/srirajkadimisetty/projects/demo/e2e-prototype/spark-split/target/spark-split-1.0-SNAPSHOT-jar-with-dependencies.jar
        "local[*]" "/Users/srirajkadimisetty/sample-data/txt/"
        "customer_xTUz_2000.txt"
        "test-data"*/
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
