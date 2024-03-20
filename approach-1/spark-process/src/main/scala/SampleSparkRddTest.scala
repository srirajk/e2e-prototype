import org.apache.spark.SparkContext
import org.apache.spark.sql.{SQLContext, SparkSession}

object SampleSparkRddTest extends App {

  private val appName = "SparkDemo";
  val spark = SparkSession.builder()
    .master("local[1]")
    .appName(appName)
    .getOrCreate();

  private val sparkContext: SparkContext = spark.sparkContext
  sparkContext.setLogLevel("INFO")
  private val sqlContext: SQLContext = spark.sqlContext


  println("First SparkContext:")
  println("APP Name :"+sparkContext.appName);
  println("Deploy Mode :"+sparkContext.deployMode);
  println("Master :"+sparkContext.master);


  spark.close();


}
