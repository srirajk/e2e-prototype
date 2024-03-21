package com.example

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, countDistinct}

object ReadData {


/*  file-record-events-notification --from-beginning
  {"businessId":"business123","productId":"product123","requestId":"0e2ba389-ee06-4955-a97d-69a81c7394c8","totalRecords":10}*/


  def main(args: Array[String]): Unit = {

    val confMap = Map(
      "spark.sql.extensions" -> "io.delta.sql.DeltaSparkSessionExtension",
      "spark.sql.catalog.spark_catalog" -> "org.apache.spark.sql.delta.catalog.DeltaCatalog"
    )

    val spark = SparkSession.builder()
      .appName("Demo")
      .master("local[2]") // Use "local[*]" for local development, adjust accordingly for production
      .config(confMap)
      .getOrCreate()

    val output_data_table = spark.read
      .format("delta")
      .load("/Users/srirajkadimisetty/projects/demos/e2e-prototype/approach-1/spark-process/spark-output-data/output_data")

    val businessId = "business123"
    val productId = "product123"
    val requestId = "0424d107-0d88-466e-aa02-478012366e0ab"

    val totalRecordsProcessedDF = output_data_table.filter(
      (col(("businessId")) === businessId) &&
        (col(("productId")) === productId) &&
        (col(("requestId")) === requestId)
    ).agg(countDistinct("recordNumber").alias("totalRecordsProcessedSoFar"))

    val totalRecords = if (totalRecordsProcessedDF.count() > 0) {
      totalRecordsProcessedDF.first().getAs[Long](0)
    } else {
      0
    }



   println(s"Total Records $totalRecords Executed for BusinessId: $businessId, ProductId: $productId, RequestId: $requestId")

   /* batchDf.dropDuplicates("requestId").foreachPartition(
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
