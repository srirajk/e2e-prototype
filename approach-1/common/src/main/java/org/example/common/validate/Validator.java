package org.example.common.validate;

import org.example.common.model.FileRequestLineEvent;
import org.example.common.model.SparkFileSplitRequest;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Validator {

    // return a pojo..
/*    public boolean validateRecord(final int fieldLength, final Map<String, Object> record) { // modify the
        System.out.println("Validating data...");
        return true;
    }*/

    public static FileRequestLineEvent validateFileRequestRecord(SparkFileSplitRequest fileSplitRequest, long index, String[] recordArray) {
        // Your validation logic here...
        return FileRequestLineEvent.builder()
                .recordNumber(index)
                .fieldLength(fileSplitRequest.getBusinessProduct().getFieldLength())
                .requestId(fileSplitRequest.getBusinessProductFileRequest().getRequestId())
                .businessId(fileSplitRequest.getBusinessProductFileRequest().getBusinessId())
                .productId(fileSplitRequest.getBusinessProductFileRequest().getProductId())
                .fileRequest(buildFileRequest(fileSplitRequest, recordArray))
                .fileName(fileSplitRequest.getBusinessProductFileRequest().getFilePath())
                .isValid(true)
                .errorMessage("")
                .filePath(fileSplitRequest.getBusinessProductFileRequest().getFilePath())
                .build();
    }

    private static Map<String, Object> buildFileRequest(SparkFileSplitRequest fileSplitRequest, String[] recordArray) {
        //map of recordArray with index being the key and value being the recordArray value
        return IntStream.range(0, recordArray.length)
                .boxed()
                .collect(Collectors.toMap(i -> String.valueOf(i), i -> recordArray[i]));
    }


}
