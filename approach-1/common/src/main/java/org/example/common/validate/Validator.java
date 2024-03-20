package org.example.common.validate;

import org.example.common.model.FileRequestEvent;
import org.example.common.model.SparkFileSplitRequest;

import java.util.Map;

public class Validator {

    // return a pojo..
/*    public boolean validateRecord(final int fieldLength, final Map<String, Object> record) { // modify the
        System.out.println("Validating data...");
        return true;
    }*/

    public FileRequestEvent validateRecord(SparkFileSplitRequest fileSplitRequest, long index, String[] record) {
        // Your validation logic here...
        return new FileRequestEvent();
    }


}
