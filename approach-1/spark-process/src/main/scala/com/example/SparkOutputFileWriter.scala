package com.example

import org.apache.commons.lang3.time.StopWatch
import org.apache.logging.log4j.LogManager
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.example.{Converter, KafkaProducerUtility}
import org.example.common.model.SparkMergeDataRequest
import org.example.common.utility.Utility

import java.util.concurrent.TimeUnit

object SparkOutputFileWriter {

  private val logger = LogManager.getLogger(SparkOutputFileWriter.getClass);

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      throw new IllegalArgumentException("App Name is missing. Please provide the App Name (e.g., SplitBatch), " +
        "Master argument is missing. Please provide the master URL (e.g., local[*]), " +
        "configFileLocation (e.g., /Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/staging_location_spark_configs/SparkOutputFileWriter-1f9307a0-b614-436c-9fe0-c4b8fad3ee38-config.json), ")
    }


    val watch = new StopWatch()
    watch.start()

    val confMap = Map(
      "spark.sql.extensions" -> "io.delta.sql.DeltaSparkSessionExtension",
      "spark.sql.catalog.spark_catalog" -> "org.apache.spark.sql.delta.catalog.DeltaCatalog"
    )

    val appName = args(0);
    val master = args(1)
    val configFileLocation = args(2)


    val sparkMergeDataRequest: SparkMergeDataRequest = Converter.getSparkMergeDataRequestConfig(configFileLocation);

    val spark = SparkSession.builder()
      .appName(appName)
      .master(master)
      .config(confMap)
      .getOrCreate()

    val file_records_delta_table = spark.read
      .format("delta")
      .load(sparkMergeDataRequest.getDeltaTableLocation)

    val businessId = sparkMergeDataRequest.getBusinessProductFileRequest.getBusinessId
    val productId = sparkMergeDataRequest.getBusinessProductFileRequest.getProductId
    val requestId = sparkMergeDataRequest.getBusinessProductFileRequest.getRequestId

    val df = file_records_delta_table
      .where(col("businessId") === businessId && col("productId") === productId && col("requestId") === requestId)

    val kafkaBootstrapServers = sparkMergeDataRequest.getKafkaProducerProperties.get("bootstrap-servers").toString;
    // modify later to use this variable  val topic = sparkMergeDataRequest.getKafkaProducerProperties.get("topic").toString;

    val kafkaProducerUtility = new KafkaProducerUtility(kafkaBootstrapServers, "file-request-status-updates") // Initialize it here

    val totalRecords = sparkMergeDataRequest.getTotalRecords

    val configurationSplitSize = sparkMergeDataRequest.getBusinessProduct.getConfigurationSplitSize

    val numberOfPartitions = (totalRecords / configurationSplitSize).ceil.toInt

    val outputLocation = sparkMergeDataRequest.getOutputDataLocation

    df.repartition(numberOfPartitions)
      .write
      .option("header", "true")
      .csv(s"$outputLocation")


    val json = Utility.getObjectMapper.createObjectNode()
    json.put("businessId", businessId)
    json.put("productId", productId)
    json.put("requestId", requestId)
    json.put("totalRecords", totalRecords)
    json.put("totalRecordsProcessed", totalRecords)
    json.put("source", "SparkOutputFileWriter")
    kafkaProducerUtility.produceRecord(requestId, json, null)

    kafkaProducerUtility.shutdown()

    spark.close()
    watch.stop()
    val totalTime = watch.getTime(TimeUnit.SECONDS)
    logger.warn(s"Total time it took ${totalTime} seconds to generate batches")

  }

}
