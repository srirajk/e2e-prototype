package com.example.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilityConverter {

    static Map<Integer, String> fieldNames = new HashMap<>();

    static {
        String fields = "id,name,phone,address,dob,ssn,occupation,income,risk_score,transaction_amount,country,email,status";
        String[] fieldArray = fields.split(",");
        for (int i = 0; i < fieldArray.length; i++) {
            fieldNames.put(i, fieldArray[i]);
        }
    }

    static ObjectMapper mapper = new ObjectMapper();

    public static JsonNode convertData(List<String> records) {
        ObjectNode dataNode = mapper.createObjectNode();
        for (int i = 0; i < records.size(); i++) {
            dataNode.put(fieldNames.get(i), records.get(i));
        }
        return dataNode;
    }

}
