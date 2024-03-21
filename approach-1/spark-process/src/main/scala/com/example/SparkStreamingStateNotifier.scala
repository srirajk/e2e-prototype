package com.example

import com.example.SparkStreamingAggregator.logger
import org.apache.logging.log4j.LogManager
import org.apache.spark.sql.{DataFrame, ForeachWriter, Row, SparkSession}
import org.apache.spark.sql.execution.streaming.sources.ForeachWrite
import org.apache.spark.sql.functions.{col, countDistinct, from_json}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.example.common.utility.Utility
import org.example.{Converter, KafkaProducerUtility}


object SparkStreamingStateNotifier {

  private val logger = LogManager.getLogger(SparkStreamingStateNotifier.getClass);

  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      throw new IllegalArgumentException(
        "ApplicationConfigFileLocation (e.g., /Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/src/main/resources/kafka-streaming-state-notifier.json), ")
    }

    val applicationConfigFileLocation = args(0)
    val converter = new Converter();
    val config = converter.getKafkaStreamingConfigConfig(applicationConfigFileLocation);

    val confMap = Map(
      "spark.sql.extensions" -> "io.delta.sql.DeltaSparkSessionExtension",
      "spark.sql.catalog.spark_catalog" -> "org.apache.spark.sql.delta.catalog.DeltaCatalog"
    )

    val appName = config.getAppDetails.getAppName;
    val master = config.getAppDetails.getMaster;
    val groupId = config.getKafka.getGroupId
    val checkpointLocation = config.getAppDetails.getCheckpointLocation + Converter.removeExtraSpacesAndSpecialCharacters(appName)
    val deltalakeTable = config.getDeltalake.getStorageLocation + config.getDeltalake.getTableName

    Utility.createDirectoryIfNotExists(deltalakeTable)


    val spark = SparkSession.builder()
      .appName(appName)
      .master(master)
      .config(confMap)
      .getOrCreate()

    val schema = new StructType()
      .add(StructField("businessId", StringType, true))
      .add(StructField("productId", StringType, true))
      .add(StructField("requestId", StringType, true))
      .add(StructField("totalRecords", IntegerType, true))


    val kafkaBrokers = config.getKafka.getBrokers

    val kafkaDf = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", config.getKafka.getNotificationTopic)
      .option("startingOffsets", "earliest")
      .option("group.id", groupId)
      .option("kafkaConsumer.pollTimeoutMs", "5000")
      .load()

    var df = kafkaDf.selectExpr("CAST(value AS STRING) as jsonStr")
      .select(from_json(col("jsonStr"), schema).as("data"))
      .select("data.*")

    df.writeStream.foreachBatch({
        (batchDf: DataFrame, batchId: Long) => {
          logger.warn(s"Started writing the batch of records for batchId :: $batchId")
          val sparkSession = batchDf.sparkSession
          import sparkSession.implicits._
          val screening_data = sparkSession.read.format("delta").load(deltalakeTable)
          val joinedDf = batchDf.alias("batch")
            .join(
              screening_data.alias("screening"),
              $"batch.businessId" === $"screening.businessId" &&
                $"batch.productId" === $"screening.productId" &&
                $"batch.requestId" === $"screening.requestId", "left_outer"
            )

          val resultDf = joinedDf.groupBy("batch.businessId", "batch.productId", "batch.requestId", "batch.totalRecords")
            .agg(countDistinct($"screening.recordNumber").alias("totalRecordsProcessedSoFar"))

          /*resultDf.select("businessId", "productId", "requestId", "totalRecords", "totalRecordsProcessedSoFar")
            .as[(String, String, String, Int, Long)]
            .collect()
            .foreach {
              case (businessId, productId, requestId, totalRecords, totalRecordsProcessed) => {
                if (totalRecordsProcessed == totalRecords) {
                  logger.info(s"Finished Processing Total Records: $totalRecords for BusinessId: $businessId, ProductId: $productId, RequestId: $requestId")

                } else if (businessId == null || businessId.isEmpty ||
                  productId == null || productId.isEmpty ||
                  requestId == null || requestId.isEmpty) {
                  logger.error(s"One or more required fields are missing or not found: businessId :: ${businessId}, productId :: ${productId}, " +
                    s"requestId :: ${requestId}")
                }
                else {
                  logger.info(s"So far Processed $totalRecordsProcessed out of $totalRecords for BusinessId: $businessId, ProductId: $productId, RequestId: $requestId")
                }
              }
            }
        }*/
          resultDf.foreachPartition(
            (partition: Iterator[Row]) => {
              val kafkaProducerUtility = new KafkaProducerUtility(config.getKafka.getBrokers, "file-request-status-updates") // Initialize it here
              partition.foreach(
                record => {
                  val businessId = record.getAs[String]("businessId")
                  val productId = record.getAs[String]("productId")
                  val requestId = record.getAs[String]("requestId")
                  val totalRecords = record.getAs[Int]("totalRecords")
                  val totalRecordsProcessed = record.getAs[Long]("totalRecordsProcessedSoFar")
                  if (businessId == null || businessId.isEmpty ||
                    productId == null || productId.isEmpty ||
                    requestId == null || requestId.isEmpty) {
                    logger.info(s"One or more required fields are missing or not found: businessId :: ${businessId}, productId :: ${productId}, " +
                      s"requestId :: ${requestId}")
                  }
                  else {
                    logger.info(s"So far Processed $totalRecordsProcessed out of $totalRecords for BusinessId: $businessId, ProductId: $productId, RequestId: $requestId")
                    val json = Utility.getObjectMapper.createObjectNode()
                    json.put("businessId", businessId)
                    json.put("productId", productId)
                    json.put("requestId", requestId)
                    json.put("totalRecords", totalRecords)
                    json.put("totalRecordsProcessed", totalRecordsProcessed)
                    json.put("source", "SparkStreamingStateNotifier")
                    kafkaProducerUtility.produceRecord(requestId, json, null)
                  }
                })
              kafkaProducerUtility.shutdown()
            })
          logger.warn(s"Finished writing the batch of records for batchId :: $batchId")
        }
      })
      .option("checkpointLocation", checkpointLocation)
      .start()
      .awaitTermination()






    /* df.writeStream
       .foreachBatch({
           (batchDf: DataFrame, batchId: Long) => {
             val sparkSession = batchDf.sparkSession
             val screening_results = sparkSession.read.format("delta").load(deltalakeTable)
             batchDf.foreach(
               record => {
                 val businessId = record.getAs[String]("businessId")
                 val productId = record.getAs[String]("productId")
                 val requestId = record.getAs[String]("requestId")
                 val totalRecords = record.getAs[Int]("totalRecords")
                 val totalRecordsProcessedDf = screening_results.filter(
                   (col(("businessId")) === businessId) &&
                     (col(("productId")) === productId) &&
                     (col(("requestId")) === requestId)
                 ).agg(countDistinct("recordNumber").alias("totalRecordsProcessedSoFar"))

                 val totalRecordsProcessed = if (totalRecordsProcessedDf.count() > 0) {
                   totalRecordsProcessedDf.first().getAs[Long](0)
                 } else {
                   0
                 }
                 if (totalRecordsProcessed == totalRecords) {
                   logger.info(s"Finished Processing Total Records :: $totalRecords Executed for BusinessId: $businessId, ProductId: $productId, RequestId: $requestId")
                 } else {
                   logger.info(s"So far Processed $totalRecordsProcessed out of Executed for BusinessId: $businessId, ProductId: $productId, RequestId: $requestId")
                 }
               }
             )
           }
         })
       .option("checkpointLocation", checkpointLocation)
       .start()
       .awaitTermination()*/

    spark.close()

  }

}
