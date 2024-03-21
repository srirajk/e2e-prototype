package com.example

import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.logging.log4j.LogManager
import org.apache.spark.sql.functions.{col, countDistinct, when}
import org.apache.spark.sql.{DataFrame, Encoders, Row, SparkSession}
import org.example.{Converter, KafkaProducerUtility}
import org.example.common.model.FileRequestLineEvent
import org.example.common.utility.Utility
import org.example.filter.MatchFilter

object SparkStreamingAggregator {

  private val logger = LogManager.getLogger(SparkStreamingAggregator.getClass);

  def main(args: Array[String]): Unit = {

    if (args.length < 1) {
      throw new IllegalArgumentException(
        "ApplicationConfigFileLocation (e.g., /Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/src/main/resources/kafka-streaming-aggregator-config.json), ")
    }

    /*    val appName = args(0);
        val master = args(1)*/
    val applicationConfigFileLocation = args(0)

    val converter = new Converter();
    val config = converter.getKafkaStreamingConfigConfig(applicationConfigFileLocation);

    val confMap = Map(
      "spark.sql.extensions" -> "io.delta.sql.DeltaSparkSessionExtension",
      "spark.sql.catalog.spark_catalog" -> "org.apache.spark.sql.delta.catalog.DeltaCatalog"
    )

    val appName = config.getAppDetails.getAppName;
    val master = config.getAppDetails.getMaster;
    val checkpointLocation = config.getAppDetails.getCheckpointLocation + Converter.removeExtraSpacesAndSpecialCharacters(appName)
    val deltalakeTable = config.getDeltalake.getStorageLocation + config.getDeltalake.getTableName


    val spark = SparkSession.builder()
      .appName(appName)
      .master(master)
      .config(confMap)
      .getOrCreate()


    val kafkaBrokers = config.getKafka.getBrokers
    val kafkaDf = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", config.getKafka.getInputTopic)
      .option("startingOffsets", "earliest")
      .option("group.id", config.getKafka.getGroupId)
      .option("kafkaConsumer.pollTimeoutMs", "2000")
      .load()


    /*
        I need to convert the value into a FilerequestFileEvent class whcih is in this package org.example.common.model.FileRequestLineEvent
        and then i need to call the postfilterlogic which is org.example.filter.MatchFilter and that will return FileRequestLineEvent and then i need to go and explode on every single record and if it has list of maytch models,
        i need to explode it and then i need to flatten the data and then i need to create an individual record for each match model and then i need to write it to a file.

          As in the process for every single batch, i need to look at the requestIds, and businessIds for every single event and then execute a call after writing to the file to make a call to another method.
    */

    val valueColumn = col("value").cast("BINARY")

    val convertedDf = kafkaDf.select(valueColumn).map(
      row => {
        val value = row.getAs[Array[Byte]]("value")
        val fileRequestLineEvent: FileRequestLineEvent = Converter.getFileRequestLineEvent(value)
        val event = MatchFilter.executeFilterChain(fileRequestLineEvent)
        event
      })(Encoders.bean(classOf[FileRequestLineEvent]))

    val explodedDf = convertedDf.selectExpr("*", "explode_outer(hits) as hit")

    val parsedDf = explodedDf.select(
      col("businessId"),
      col("errorMessage"),
      col("fieldLength"),
      col("fileName"),
      col("filePath"),
      col("rawText"), // Select the concatenated key
      col("productId"),
      col("recordNumber"),
      col("requestId"),
      col("valid"),
      col("totalRecords"),
      col("postFilterApplied"),
      when(col("hit.matchId").isNotNull, col("hit.matchId")).alias("matchId"), // Check if matchId is not null
      when(col("hit.description").isNotNull, col("hit.description")).alias("description"), // Check if description is not null
      when(col("hit.matchableItemId").isNotNull, col("hit.matchableItemId")).alias("matchableItemId"), // Check if matchableItemId is not null
      when(col("hit.decision").isNotNull, col("hit.decision")).alias("decision") // Check if decision is not null
    )

    parsedDf.writeStream
      .outputMode("append")
      .foreachBatch(
        {
          (batchDf: DataFrame, batchId: Long) => {
            logger.warn(s"Started writing the batch of records for batchId :: $batchId")
            batchDf.write
              .format("delta")
              .mode("append")
              .option("path", deltalakeTable)
              .partitionBy("businessId", "productId", "requestId")
              .save()

            batchDf.dropDuplicates("requestId").foreachPartition(
              (partition: Iterator[Row]) => {
                val kafkaProducerUtility = new KafkaProducerUtility(config.getKafka.getBrokers, config.getKafka.getNotificationTopic) // Initialize it here
                partition.foreach(
                  record => {
                    val businessId = record.getAs[String]("businessId")
                    val productId = record.getAs[String]("productId")
                    val requestId = record.getAs[String]("requestId")
                    // val recordNumber = record.getAs[Long]("recordNumber")
                    val totalRecords = record.getAs[Long]("totalRecords")
                    //logger.info(s"Pre Processing BusinessId: $businessId, ProductId: $productId, RequestId: $requestId, RecordNumber: $recordNumber")
                    val json = Utility.getObjectMapper.createObjectNode()
                    json.put("businessId", businessId)
                    json.put("productId", productId)
                    json.put("requestId", requestId)
                    json.put("totalRecords", totalRecords)
                    kafkaProducerUtility.produceRecord(requestId, json, null)
                  }
                )
                kafkaProducerUtility.shutdown();
              })

            logger.warn(s"Finished writing the batch of records for batchId :: $batchId")
          }
        }
      )
      .option("checkpointLocation", checkpointLocation)
      .start()
      .awaitTermination()

    // First explode on fileRequest


    //val concatenatedFileRequestDf = convertedDf.withColumn("fileRequest", expr("concat_ws(',', map_keys(fileRequest), map_values(fileRequest))"))

    /*val concatenatedFileRequestDf = convertedDf.withColumn("fileRequest",
      expr("concat_ws(',', map_keys(fileRequest), map_values(fileRequest).cast('string'))"))*/

    /*    val concatenatedFileRequestDf = convertedDf.withColumn("fileRequest",
          concat_ws(", ",
            expr("transform(map_entries(fileRequest), e -> concat(e.key, ':', cast(e.value as string)))")
          ))*/
    // Then explode on hits


    /*    {"businessId":"business123",
          "errorMessage":"",
          "fieldLength":13,
          "fileName":"/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/demo-test-folders/business123_product123_LURn_20-03-2024-17-00-17.txt_10.txt",
          "filePath":"/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/demo-test-folders/business123_product123_LURn_20-03-2024-17-00-17.txt_10.txt",
          "fileRequest":{"11":{},"12":{},"0":{},"1":{},"2":{},"3":{},"4":{},"5":{},"6":{},"7":{},"8":{},"9":{},"10":{}},
          "productId":"product123",
          "recordNumber":2,
          "requestId":"0424d107-0d88-466e-aa02-478012366e0a",
          "valid":true,"postFilterApplied":false,"matchId":"0.5342249664242037",
          "description":"Description of the match0.9002997405092461",
          "matchableItemId":"0.163725117751798",
          "decision":"risk"
        }*/


    /*  df.writeStream
        .outputMode("append")
        .foreachBatch(
          {
            (batchDf: DataFrame, batchId: Long) => {

              // can i get the full dataframe for the distinct requestIds within this batch ?

              batchDf.write
                .format("delta")
                .mode("append")
                .option("path", "/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/spark-output-data/output_data")
                .partitionBy("businessId", "productId", "requestId")
                .save()

              batchDf.dropDuplicates("requestId").foreachPartition(
                (partition: Iterator[Row]) => {
                  val kafkaProducerUtility = new KafkaProducerUtility(config.getKafka.getBrokers, config.getKafka.getNotificationTopic) // Initialize it here
                  partition.foreach(
                    record => {
                      val businessId = record.getAs[String]("businessId")
                      val productId = record.getAs[String]("productId")
                      val requestId = record.getAs[String]("requestId")
                      val recordNumber = record.getAs[Long]("recordNumber")
                      logger.info(s"Pre Processing BusinessId: $businessId, ProductId: $productId, RequestId: $requestId, RecordNumber: $recordNumber")

                    }
                  )
                })

            /*  val spark = batchDf.sparkSession

              val output_data_table = spark.read
                .format("delta")
                .load("/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/spark-output-data/output_data")

              batchDf.dropDuplicates("requestId").foreachPartition(
                (partition: Iterator[Row]) => {
                  partition.foreach(
                    record => {
                      val businessId = record.getAs[String]("businessId")
                      val productId = record.getAs[String]("productId")
                      val requestId = record.getAs[String]("requestId")
                     /* val recordNumber = record.getAs[Long]("recordNumber")*/
                      logger.info(s"Pre Processing BusinessId: $businessId, ProductId: $productId, RequestId: $requestId")
                      val totalRecordsProcessed = output_data_table.filter(
                        (col(("businessId")) === businessId) &&
                          (col(("productId")) === productId) &&
                          (col(("requestId")) === requestId)
                      ).agg(countDistinct("recordNumber").alias("totalRecordsProcessedSoFar"))
                      logger.info(s"Total Records $totalRecordsProcessed Executed for BusinessId: $businessId, ProductId: $productId, RequestId: $requestId")
                    }
                  )
                })*/


            }
          }
        )
        .option("checkpointLocation", "/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/spark-output-data/checkpointLocation")
        .start()
        .awaitTermination()*/


    /*    df.writeStream
          .outputMode("append")
          .format("csv")
          .option("path", "/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/spark-output-data/output-data")
          .option("checkpointLocation", "/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/spark-output-data/checkpointLocation")
          .start()
          .awaitTermination()*/

    //  val valueColumn: Column = col("value").cast("BINARY")

    /*  val convertedDf: Dataset[FileRequestLineEvent] = kafkaDf.select(valueColumn).map(
        row => {
          val value = row.getAs[Array[Byte]]("value")
          val fileRequestLineEvent: FileRequestLineEvent = Converter.getFileRequestLineEvent(value)
          // apply the postfilter logic and send me the decision..
          // fileRequestLineEvent
          println(fileRequestLineEvent)
          fileRequestLineEvent
        })(Encoders.bean(classOf[FileRequestLineEvent]))
  */
    //fileRequestLineEvent

    /* val modifiedDf = convertedDf.select(
       col("businessId"),
       col("errorMessage"),
       col("fieldLength"),
       col("fileName"),
       col("filePath"),
       col("fileRequest"), // Keep fileRequest as a map
       col("hits"), // Keep hits as a map
       col("productId"),
       col("recordNumber"),
       col("requestId"),
       col("valid")
     )*/


    /*    convertedDf.filter(col("hits").isNotNull).foreachPartition(
          partition => {
            partition.foreach(
              record => {
                println(record)
              }
            )
          }
        )*/

    /*    convertedDf.foreachPartition(
          (partition: Iterator[FileRequestLineEvent]) => {
            partition.foreach(
              record => {
                println(record)
              }
            )
          })
    */
    /* modifiedDf.writeStream
       .outputMode("append")
       .format("json")
       .option("path", "/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/output-data" )
       .option("checkpointLocation", "/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/checkpointLocation")
       .start()
       .awaitTermination()


     convertedDf.writeStream
       .foreachBatch { (batchDF: Dataset[FileRequestLineEvent], _: Long) =>
         import batchDF.sparkSession.implicits._

         // Extract distinct requestIds as a List[String]
         val requestIds = batchDF.select($"requestId").as[String].distinct().collect().toList

         // Example: print out distinct requestIds for demonstration
         println(requestIds)

         // Proceed with conditional logic based on requestIds...
       }
       .outputMode("append")
      // .trigger(Trigger.ProcessingTime("5 seconds")) // Adjust based on your needs
       .start()
       .awaitTermination()
 */
    /*    convertedDf.filter(col("hits").isNotNull).writeStream.foreachBatch(
            (batchDf: Dataset[FileRequestLineEvent], batchId: Long) => {
              println(s"Batch ID: $batchId")
              batchDf.rdd.foreach(
                record => {
                  println(record)
                }
              )
            }
          )
          .outputMode("append")
          .format("json")
          .option("path", "/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/output-data" )
          .option("checkpointLocation", "/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/checkpointLocation")
          .start()
          .awaitTermination()*/


  }

}
