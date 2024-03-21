package com.example


import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.time.StopWatch
import org.apache.logging.log4j.LogManager
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.example.common.model.{FileRequestLineEvent, SparkFileSplitRequest}
import org.example.common.utility.Utility
import org.example.common.validate.Validator
import org.example.{Converter, KafkaProducerUtility}

import java.util.concurrent.TimeUnit

object ReadAndSplitFileRequest {

  private val logger = LogManager.getLogger(ReadAndSplitFileRequest.getClass);


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
    val fileSplitRequest: SparkFileSplitRequest = converter.getSparkFileSplitRequestConfig(configFileLocation);
    val exclusionMarker = fileSplitRequest.getExclusionMarker

    val fieldLength = fileSplitRequest.getBusinessProduct.getFieldLength
    val businessId = fileSplitRequest.getBusinessProductFileRequest.getBusinessId
    val productId = fileSplitRequest.getBusinessProductFileRequest.getProductId
    val requestId = fileSplitRequest.getBusinessProductFileRequest.getRequestId

    val spark = SparkSession.builder()
      .appName(appName)
      .master(master)
      .getOrCreate()
    val sc: SparkContext = spark.sparkContext

    val fileRdd: RDD[String] = sc.textFile(fileSplitRequest.getBusinessProductFileRequest.getFilePath)
    val firstTwoLines = fileRdd.take(2)
    val headerLine = firstTwoLines(0)
    val secondLine = firstTwoLines(1)
    logger.warn(s"Header Line: $headerLine")
    logger.warn(s"Second Line: $secondLine")
    val parallelism = sc.defaultParallelism

    // Efficiently calculate total record count without fetching all data
    // TODO look at various other approaches
    val totalRecordsExpected: Int = fileRdd.mapPartitionsWithIndex { (idx, iter) =>
      if (idx == parallelism - 1) { // Assuming the last partition
        Iterator.single(iter.size - 1) // Exclude the last line (footer)
      } else {
        Iterator.single(iter.size)
      }
    }.sum.toInt - exclusionMarker // Exclude header & footer


    // Fetch the last line for total records value (footer)
    val totalRecordsInFooterValue: Int = fileRdd.mapPartitions(iter =>
      Iterator.single(iter.toSeq.last)).collect().last.toInt


    /*    val totalRecordsInFooterValue: Option[Int] = fileRdd
          .mapPartitionsWithIndex((idx, iter) => {
            if (idx == fileRdd.getNumPartitions - 1) { // Check if this is the last partition
              // Process only the last line of the last partition
              val lastLine = if (iter.nonEmpty) Some(iter.reduce((_, b) => b)) else None
              lastLine
                .map(_.split("\\|"))
                .filter(parts => parts.length == 1 && parts.head.matches("\\d+")) // Ensure it's a single number
                .map(parts => Iterator.single(parts.head.toInt)) // Convert to Int and wrap in Iterator
            } else {
              Iterator.empty // Not the last partition, return an empty iterator
            }
          })
          .collect()
          .flatten // Flatten to remove empty options and unwrap non-empty options
          .lastOption // Get the last element, if present
    */
    logger.warn(s"totalRecordsExpected: $totalRecordsExpected")
    logger.warn(s"totalRecordsInFooterValue: $totalRecordsInFooterValue")

    if (totalRecordsExpected != totalRecordsInFooterValue) {
      //throw new Exception("Records count mismatch")
      logger.error("1st Validation :: Total Records DID not Match from the file to the expected Record Count can be ignored")
    } else {
      logger.warn("Total Records Match from the file to the expected Record Count")
    }

    // POJO .. data object can be hashmap.. but validation needs to be provided.
    val fileRequestLineEvents: RDD[FileRequestLineEvent] = fileRdd
      .zipWithIndex()
      .filter { case (_, index) => index != 0 && index < totalRecordsExpected + 1 } // Exclude header and footer
      .map { case (record, index) => (record.split("\\|"), index) }
      .filter { case (record, _) => record.length == fieldLength } // move to field level
      .map { case (recordArray, index) =>
        Validator.validateFileRequestRecord(fileSplitRequest, index, recordArray, totalRecordsExpected)
      }

    val recordCounter = spark.sparkContext.longAccumulator("Record Counter")

    val kafkaBootstrapServers = fileSplitRequest.getKafkaProducerProperties.get("bootstrap-servers").toString;
    val topic = fileSplitRequest.getKafkaProducerProperties.get("topic").toString;

    fileRequestLineEvents.foreachPartition { partitionIterator =>
      val kafkaProducerUtility = new KafkaProducerUtility(kafkaBootstrapServers, topic) // Initialize it here
      partitionIterator.foreach { fileRequestLineEvent: FileRequestLineEvent => {
        val headerJavaMap: _root_.java.util.Map[_root_.java.lang.String, _root_.scala.Array[Byte]] = KafkaHeaderBuilderUtility.extractAndBuildHeaders(totalRecordsExpected, fileRequestLineEvent)
        val json = Utility.getObjectMapper.convertValue(fileRequestLineEvent, classOf[ObjectNode])
        val key = fileRequestLineEvent.getRequestId + "-" + fileRequestLineEvent.getRecordNumber.toString
        kafkaProducerUtility.produceRecord(key, json, headerJavaMap)
        recordCounter.add(1) // Increment the accumulator for each record
      }
      }
      kafkaProducerUtility.shutdown()
    }

    val accumulatorRecordCounterValue = recordCounter.value

    if (totalRecordsInFooterValue == accumulatorRecordCounterValue) {
      logger.warn(s"2nd Validation :: MATCHED Total Records ${totalRecordsInFooterValue} Match from the file to the expected Record Count Accumulator ${accumulatorRecordCounterValue} ")
    } else {
      logger.error(s"2nd Validation :: NOT MATCHED Total Records ${totalRecordsInFooterValue} DID not Match from the file to the expected Record Count Accumulator ${accumulatorRecordCounterValue} cannot be ignored")
    }


    logger.debug(s"***** Printing the records after the conversion")
    logger.debug(s"")
    fileRequestLineEvents.take(5).foreach(record => logger.debug(record.toString))
    logger.debug(s"*****")
    logger.debug(s"*****")


    val kafkaProducerUtility = new KafkaProducerUtility(kafkaBootstrapServers, "file-request-status-updates") // Initialize it here
    val json = Utility.getObjectMapper.createObjectNode()
    json.put("businessId", businessId)
    json.put("productId", productId)
    json.put("requestId", requestId)
    json.put("totalRecords", totalRecordsInFooterValue)
    json.put("totalRecordsProcessed", 0)
    json.put("source", "ReadAndSplitFileRequest")
    kafkaProducerUtility.produceRecord(requestId, json, null)
    kafkaProducerUtility.shutdown()

    spark.close()
    watch.stop()
    val totalTime = watch.getTime(TimeUnit.SECONDS)
    logger.warn(s"Total time it took ${totalTime} seconds for records processed: $accumulatorRecordCounterValue")
  }
}
