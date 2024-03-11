package com.example

import com.example.ReadTextFileAndGenerateRecord.logger
import org.apache.commons.lang3.time.StopWatch
import org.apache.logging.log4j.LogManager
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{ArrayType, BooleanType, IntegerType, LongType, StringType, StructType}

import java.util.concurrent.TimeUnit

object MergeFiles {

  private val logger = LogManager.getLogger(MergeFiles.getClass);

  def main(args: Array[String]): Unit = {

    if (args.length < 5) {
      throw new IllegalArgumentException("Master argument is missing. Please provide the master URL (e.g., local[*]), " +
        "inputFolderBasePath (e.g., /Users/srirajkadimisetty/sample-data/output-temp/), " +
        "outputFolderBasePath (e.g., /Users/srirajkadimisetty/sample-data/final-output), " +
        "requestId." +
        "configurationSplitSize (e.g., 1000)")
    }

    val master = args(0)
    val inputFolderBasePath = args(1)
    val outputFolderBasePath = args(2)
    val requestId = args(3)
    val configurationSplitSizeInString = args(4)
    val configurationSplitSize = configurationSplitSizeInString.toInt

    val appName = "MergeFiles Processor";

    val spark = SparkSession.builder()
      .appName(appName)
      .master(master) // Use "local[*]" for local development, adjust accordingly for production
      .getOrCreate()

    val sc: SparkContext  = spark.sparkContext

    val watch = new StopWatch()

    watch.start()


    val records = sc.textFile(s"$inputFolderBasePath$requestId/*.json")

    val parallelism = sc.defaultParallelism

    val totalRecords: Int = records.mapPartitionsWithIndex { (idx, iter) =>
      if (idx == parallelism - 1) { // Assuming the last partition
        Iterator.single(iter.size - 1) // Exclude the last line (footer)
      } else {
        Iterator.single(iter.size)
      }
    }.sum.toInt

    logger.info(s"totalRecords: $totalRecords")

    val numberOfPartitions = (totalRecords / configurationSplitSize)

    val repartitionedRecords = records.repartition(numberOfPartitions)

    repartitionedRecords.saveAsTextFile(s"$outputFolderBasePath/$requestId")



   // val df = spark.read.option("multiline", "true").json(s"$inputFolderBasePath$requestId")
    spark.close()
    watch.stop()

    val totalTime = watch.getTime(TimeUnit.SECONDS)

    logger.info(s"Total time it took ${totalTime} seconds for writing ${numberOfPartitions} files with a configuration split of ${configurationSplitSizeInString}")

  }



}
