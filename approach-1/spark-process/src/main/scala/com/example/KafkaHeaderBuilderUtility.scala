package com.example

import org.example.common.model.FileRequestLineEvent

import java.util

object KafkaHeaderBuilderUtility {

  def extractAndBuildHeaders(totalRecordsExpected: Int, fileRequestEvent: FileRequestLineEvent) = {
    val headerJavaMap: util.Map[String, Array[Byte]] = new util.HashMap[String, Array[Byte]]()
    headerJavaMap.put("requestId", fileRequestEvent.getRequestId.getBytes)
    headerJavaMap.put("recordNumber", java.nio.ByteBuffer.allocate(java.lang.Long.BYTES).putLong(fileRequestEvent.getRecordNumber).array())
    headerJavaMap.put("fileName", fileRequestEvent.getFilePath.getBytes)
    headerJavaMap.put("fieldLength", java.nio.ByteBuffer.allocate(java.lang.Long.BYTES).putLong(fileRequestEvent.getFieldLength).array())
    headerJavaMap.put("totalRecords", java.nio.ByteBuffer.allocate(java.lang.Integer.BYTES).putInt(totalRecordsExpected).array())
    headerJavaMap.put("isValidRecord", fileRequestEvent.isValid.toString.getBytes)
    headerJavaMap
  }


}
