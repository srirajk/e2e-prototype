package com.example

import org.apache.spark.SparkContext
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{Column, Dataset, Encoders, SparkSession}
import org.example.Converter
import org.example.common.model.FileRequestLineEvent

object SparkStreamingAggregator {

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      throw new IllegalArgumentException("App Name is missing. Please provide the App Name (e.g., KafkaSparkStreamingAggregator), " +
        "Master argument is missing. Please provide the master URL (e.g., local[*]), " +
        "ApplicationConfigFileLocation (e.g., /Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/src/main/resources/kafka-streaming-config.json), ")
    }

    val appName = args(0);
    val master = args(1)
    val applicationConfigFileLocation = args(2)

    val converter = new Converter();
    val config = converter.getKafkaStreamingConfigConfig(applicationConfigFileLocation);

    val spark = SparkSession.builder()
      .appName(appName)
      .master(master)
      .getOrCreate()


    val kafkaDf = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", config.getKafka.getBrokers)
      .option("subscribe", config.getKafka.getTopic)
      .option("startingOffsets", "earliest")
      .option("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
      .option("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
      .option("group.id", config.getKafka.getGroupId)
      .load()

    val valueColumn: Column = col("value").cast("BINARY")

    val convertedDf: Dataset[FileRequestLineEvent] = kafkaDf.select(valueColumn).map(
      row => {
        val value = row.getAs[Array[Byte]]("value")
        val fileRequestLineEvent: FileRequestLineEvent = Converter.getFileRequestLineEvent(value)
        // apply the postfilter logic and send me the decision..
        // fileRequestLineEvent
        println(fileRequestLineEvent)
        fileRequestLineEvent
      })(Encoders.bean(classOf[FileRequestLineEvent]))

    //fileRequestLineEvent

    val modifiedDf = convertedDf.select(
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
    )


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
    modifiedDf.writeStream
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
