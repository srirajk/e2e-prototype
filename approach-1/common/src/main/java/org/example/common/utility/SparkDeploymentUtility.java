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
    private final String configFileAppBaseLocation;
    private final String logFileAppBaseLocation;
    private final String sparkJobType;

    public <mapper> SparkDeploymentUtility(final String sparkSubmitPath, final String jarPath,
                                           final String mainClass,
                                           final String master, final ObjectMapper mapper,
                                           final String configFileBaseLocation, final String logFileBaseLocation,
                                           final String sparkJobType) {
        this.sparkSubmitPath = sparkSubmitPath;
        this.jarPath = jarPath;
        this.mainClass = mainClass;
        this.master = master;
        this.mapper = mapper;
        this.sparkJobType = sparkJobType;
        this.configFileAppBaseLocation = configFileBaseLocation+File.separator + sparkJobType;
        Utility.createDirectoryIfNotExists(configFileAppBaseLocation);
        this.logFileAppBaseLocation = logFileBaseLocation + File.separator + sparkJobType;
        Utility.createDirectoryIfNotExists(logFileAppBaseLocation);
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
            String configFileLocation = this.configFileAppBaseLocation + File.separator + appName + "-config.json";
            File file = new File(configFileLocation);
            mapper.writeValue(file, configFile);
            //String logFile = logFileAppBaseLocation + File.separator + appName + ".log";
            List<String> command = new ArrayList<>(List.of(
                    sparkSubmitPath,
                    "--master", master,
                    "--driver-memory", String.valueOf(driverMemory) + "g",
                    "--executor-memory", String.valueOf(executorMemory) + "g",
                    "--class", mainClass,
                    jarPath, appName, master, configFileLocation));
            log.info("Command: {}", command);
            command.addAll(args);
            executeProcess(appName, command); // create a method which will execute the command and pipe the logs to a file in the logFileAppBaseLocation and add appName to the log file.

            log.info("Full Command: {}", command);
            // i want to execute this command on the process builder and just would love to know if its success or not ... also
            // i would like to get a log file of the spark job
      /*      /Users/srirajkadimisetty/tools/spark-3.5.1-bin-hadoop3-scala2.13/bin/spark-submit
                --master "local[*]"
                --driver-memory 1g
                --executor-memory 2g
                --class com.example.ReadTextFileAndGenerateRecord
                    /Users/srirajkadimisetty/projects/demo/e2e-prototype/spark-split/target/spark-split-1.0-SNAPSHOT-jar-with-dependencies.jar
        "local[*]" "/Users/srirajkadimisetty/sample-data/txt/"
        "customer_xTUz_2000.txt"
        "test-data"*/
        } catch (Exception e) {
            log.error("Error while executing spark job for appType :: {} for appName :: {} and failed with exception :: {}", sparkJobType, appName, e);
        }
    }

    private void executeProcess(final String appName, final List<String> command) {
        try {
            // Create ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            // Redirect output to a log file
            File logFile = new File(logFileAppBaseLocation + File.separator + appName + ".log.txt");
            log.info("Log file: {}", logFile);
            processBuilder.redirectOutput(logFile);

            // Start the process
            Process process = processBuilder.start();

            // Wait for the process to finish and check the exit value
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Spark job completed successfully.");
            } else {
                log.error("Spark job failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
