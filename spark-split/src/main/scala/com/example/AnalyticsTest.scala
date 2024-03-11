package com.example

import org.apache.spark.sql.types.{ArrayType, BooleanType, IntegerType, LongType, StringType, StructType}

object AnalyticsTest {

   def main(args: Array[String]): Unit = {

     val riskSchema = ArrayType(new StructType()
       .add("name", StringType, nullable = true)
       .add("nationality", StringType, nullable = true)
       .add("reason", StringType, nullable = true)
       .add("riskScore", IntegerType, nullable = true)
     )

     val dataSchema = new StructType()
       .add("id", StringType, nullable = true)
       .add("name", StringType, nullable = true)
       .add("phone", StringType, nullable = true)
       .add("address", StringType, nullable = true)
       .add("dob", StringType, nullable = true)
       .add("ssn", StringType, nullable = true)
       .add("occupation", StringType, nullable = true)
       .add("income", StringType, nullable = true)
       .add("risk_score", IntegerType, nullable = true)
       .add("transaction_amount", StringType, nullable = true)
       .add("country", StringType, nullable = true)
       .add("email", StringType, nullable = true)
       .add("status", StringType, nullable = true)

     val filteredRecordRiskModelSchema = new StructType()
       .add("fileName", StringType, nullable = true)
       .add("requestId", StringType, nullable = true)
       .add("data", dataSchema, nullable = true)
       .add("recordNumber", LongType, nullable = true)
       .add("fieldLength", LongType, nullable = true)
       .add("risks", riskSchema, nullable = true)
       .add("hit", BooleanType, nullable = true)
      

    val spark = SparkSession.builder()
      .appName(appName)
      .master(master) // Use "local[*]" for local development, adjust accordingly for production
      .getOrCreate()

    val sc: SparkContext  = spark.sparkContext

    val watch = new StopWatch()

    watch.start()

   }

}
