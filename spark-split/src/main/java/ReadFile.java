import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.SparkContext;

import java.util.*;

public class ReadFile {
    private static final Logger logger = LogManager.getLogger(ReadFile.class);

    public static void main(String[] args) {
        final String appName = "ReadMockData";
        final String folder = "/Users/srirajkadimisetty/sample-data/txt/";
        final String fileName = "customer_mpaU_10.txt";
        final int fieldLength = 13;
        final String topicName = "demo-topic";
        final String requestId = UUID.randomUUID().toString();
        final String file = folder + fileName;
        logger.info("***** Starting the process *****");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        SparkSession spark = SparkSession.builder()
                .appName(appName)
                .master("local[*]")
                .getOrCreate();

        SparkContext sc = spark.sparkContext();
        JavaSparkContext jsc = new JavaSparkContext(sc);
        logger.info("***** Starting the Spark Context *****");
        JavaRDD<String> fileRdd = jsc.textFile(file);

        // Fetch the first two lines to check header and initial record
        List<String> firstTwoLines = fileRdd.take(2);
        String headerLine = firstTwoLines.get(0);
        String secondLine = firstTwoLines.get(1);

        logger.info("Header Line: " + headerLine);
        logger.info("Second Line: " + secondLine);


        final int defaultParallelism = jsc.defaultParallelism();

        int totalRecordsEmbeddedWithinFile = fileRdd.mapPartitionsWithIndex((index, iter) -> {
            int size = 0;
            while(iter.hasNext()) {
                iter.next();
                size++;
            }

            if (index == defaultParallelism - 1) {
                // Last partition, exclude footer
                return Collections.singletonList(size - 1).iterator();
            } else {
                return Collections.singletonList(size).iterator();
            }
        }, true).reduce(Integer::sum) - 2; // Exclude header and footer

        logger.info("Total Records within the InputFile: " + totalRecordsEmbeddedWithinFile);

        int totalRecordsFooterValue = fileRdd.mapPartitions(iter -> {
            List<String> list = new ArrayList<>();
            iter.forEachRemaining(list::add);
            return Collections.singletonList(Integer.parseInt(list.get(list.size() - 1))).iterator();
        }).collect().get(0);

        logger.info("Total Records Footer Value " + totalRecordsFooterValue);


        if (totalRecordsEmbeddedWithinFile != totalRecordsFooterValue) {
            throw new RuntimeException("Record count mismatch");
        } else {
            System.out.println("Total Records Match from the file to the expected Record Count");
        }

        stopWatch.stop();
        logger.info("Time Elapsed: " + stopWatch.getTime());
    }

}
