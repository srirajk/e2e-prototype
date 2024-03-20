import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.example.Converter
import org.example.common.model.SparkFileSplitRequest
import org.apache.logging.log4j.LogManager

object SplitBatch {

  private val logger = LogManager.getLogger(SplitBatch.getClass);

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      throw new IllegalArgumentException("App Name is missing. Please provide the App Name (e.g., SplitBatch), " +
        "Master argument is missing. Please provide the master URL (e.g., local[*]), " +
        "configFileLocation (e.g., /Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/staging_location_spark_configs/FileSplitProcessor-1f9307a0-b614-436c-9fe0-c4b8fad3ee38-config.json), ")
    }

    val watch = new StopWatch()
    watch.start()

    val appName = args(0);
    val master = args(1)
    val configFileLocation = args(2)

    val converter = new Converter();
    val fileSplitRequest: SparkFileSplitRequest = converter.getConfig(configFileLocation);

    val spark = SparkSession.builder()
      .appName(appName)
      .master(master)
      .getOrCreate()
    val sc: SparkContext = spark.sparkContext

    val fileRdd: RDD[String] = sc.textFile(fileSplitRequest.getBusinessProductFileRequest.getFilePath)
    val firstTwoLines = fileRdd.take(2)
    val headerLine = firstTwoLines(0)
    val secondLine = firstTwoLines(1)
    logger.info(s"Header Line: $headerLine")
    logger.info(s"Second Line: $secondLine")
    val parallelism = sc.defaultParallelism

    // Efficiently calculate total record count without fetching all data
    val totalRecordsExpected: Int = fileRdd.mapPartitionsWithIndex { (idx, iter) =>
      if (idx == parallelism - 1) { // Assuming the last partition
        Iterator.single(iter.size - 1) // Exclude the last line (footer)
      } else {
        Iterator.single(iter.size)
      }
    }.sum.toInt - 2 // Exclude header & footer

    // Fetch the last line for total records value (footer)
    val totalRecordsInFooterValue: Int = fileRdd.mapPartitions(iter =>
      Iterator.single(iter.toSeq.last)).collect().last.toInt

    logger.info(s"totalRecordsExpected: $totalRecordsExpected")
    logger.info(s"totalRecordsInFooterValue: $totalRecordsInFooterValue")

    if (totalRecordsExpected != totalRecordsInFooterValue) {
      //throw new Exception("Records count mismatch")
      logger.error("Total Records Match from the file to the expected Record Count")
    } else {
      logger.info("Total Records Match from the file to the expected Record Count")
    }

    // POJO .. data object can be hashmap.. but validation needs to be provided.
    val outputData: RDD[(Long, JsonNode)] = fileRdd
      .zipWithIndex()
      .filter { case (_, index) => index != 0 && index < totalRecordsExpected + 1 } // Exclude header and footer
      .map { case (record, index) => (record.split("\\|"), index) }
      .filter { case (record, _) => record.length == fieldLength } // move to field level
      .map { case (record, index) =>
        // conversion of hashMap
        val javaList = record.toList.asJava
        val json = UtilityConverter.convertData(javaList)
        // validation
        (index, json)
      }


    //filter and then ssend to kafka with the right messages

    watch.stop()
    spark.stop()



  }

}
