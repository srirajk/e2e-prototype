package com.example

import com.example.utility.{KafkaProducerUtility, UtilityConverter}
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang3.time.StopWatch
import org.apache.logging.log4j.LogManager
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import java.util
import java.util.concurrent.TimeUnit
import java.util.{Map, UUID}
import scala.jdk.CollectionConverters._

object ReadTextFileAndGenerateRecord {

  private val logger = LogManager.getLogger(ReadTextFileAndGenerateRecord.getClass);

  def main(args: Array[String]): Unit = {
    // Replace this later with the args

    if (args.length < 4) {
      throw new IllegalArgumentException("Master argument is missing. Please provide the master URL (e.g., local[*]), " +
        "folder (e.g., /Users/srirajkadimisetty/sample-data/txt/), " +
        "fileName (e.g., customer_mpaU_10.txt), " +
        "topicName (e.g., test-topic).")
    }

    val master = args(0)
    val folder = args(1)
    val fileName = args(2)
    val topicName = args(3)

    val appName = "ReadTextFileAndGenerateRecords";
    // val folder = "/Users/srirajkadimisetty/sample-data/txt/";
    // val fileName = "customer_mpaU_10.txt";
    val fieldLength = 13;
   // val topicName = "demo-topic";
    val requestId = UUID.randomUUID().toString;

    val file = folder + fileName

    val watch = new StopWatch()

    watch.start()


    val spark = SparkSession.builder()
      .appName(appName)
      .master(master)
      .getOrCreate()

    val sc: SparkContext = spark.sparkContext
    val fileRdd: RDD[String] = sc.textFile(file)

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
    val totalRecordsFooterValue: Int = fileRdd.mapPartitions(iter => Iterator.single(iter.toSeq.last)).collect().last.toInt

    logger.info(s"totalRecordsExpected: $totalRecordsExpected")
    logger.info(s"totalRecordsFooterValue: $totalRecordsFooterValue")

    if (totalRecordsExpected != totalRecordsFooterValue) {
      //throw new Exception("Records count mismatch")
      logger.error("Total Records Match from the file to the expected Record Count")
    } else {
      logger.info("Total Records Match from the file to the expected Record Count")
    }

    val outputData: RDD[(Long, JsonNode)] = fileRdd
      .zipWithIndex()
      .filter { case (_, index) => index != 0 && index < totalRecordsExpected + 1 } // Exclude header and footer
      .map { case (record, index) => (record.split("\\|"), index) }
      .filter { case (record, _) => record.length == fieldLength }
      .map { case (record, index) =>
        val javaList = record.toList.asJava
        val json = UtilityConverter.convertData(javaList)
        (index, json)
      }

    val recordCounter = spark.sparkContext.longAccumulator("Record Counter")

    outputData.foreachPartition { partitionIterator =>
      val kafkaProducerUtility = new KafkaProducerUtility(topicName) // Initialize it here
      partitionIterator.foreach { case (index, json) =>
        val headerJavaMap: Map[String, Array[Byte]] = new util.HashMap[String, Array[Byte]]()
        headerJavaMap.put("requestId", requestId.getBytes)
        headerJavaMap.put("recordNumber", java.nio.ByteBuffer.allocate(java.lang.Long.BYTES).putLong(index).array())
        headerJavaMap.put("fileName", fileName.getBytes)
        headerJavaMap.put("fieldLength", java.nio.ByteBuffer.allocate(java.lang.Long.BYTES).putLong(fieldLength).array())
        headerJavaMap.put("recordCount", java.nio.ByteBuffer.allocate(java.lang.Integer.BYTES).putInt(totalRecordsExpected).array())
        kafkaProducerUtility.produceRecord(index.toString, json, headerJavaMap)
        recordCounter.add(1) // Increment the accumulator for each record
      }
      kafkaProducerUtility.shutdown()
    }

    logger.info(s"***** Printing the records after the conversion")
    logger.info(s"")
    outputData.take(5).foreach(println)
    logger.info(s"*****")
    logger.info(s"*****")
    spark.close()
    watch.stop()
    val totalTime = watch.getTime(TimeUnit.SECONDS)
    logger.info(s"Total time it took ${totalTime} seconds for records processed: ${recordCounter.value}")
  }
}
