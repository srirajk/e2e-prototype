package com.example.rph.service;

import com.example.rph.model.Risk;
import com.example.rph.utility.Utility;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.InputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MockRiskDataService {


    private List<ObjectNode> riskData;
    private Map<Integer, ObjectNode> riskMapData;
    private int riskDataLength;

    private Random random = new Random();

    public MockRiskDataService(@Autowired ResourceLoader resourceLoader) throws IOException {
        riskData = new ArrayList<>();
        InputStream inputStream = getClass().getResourceAsStream("/risk-mockdata.json");
        ArrayNode data = Utility.mapper.readValue(inputStream, ArrayNode.class);


        for (JsonNode node : data) {
            riskData.add((ObjectNode) node);
        }
        riskDataLength = riskData.size();
        riskMapData = IntStream.range(0, riskData.size())
                .boxed()
                .collect(Collectors.toMap(
                        index -> index,
                        index -> riskData.get(index)
                ));
    }

    public List<ObjectNode> getRandomRiskDataReferences() {
        return IntStream.rangeClosed(1, 4)
                .mapToObj(number -> {
                    final int randomNumber = random.nextInt(riskDataLength);
                    ObjectNode riskDataRandom = getRiskDataRandom(randomNumber);
                    ObjectNode objectNode = Utility.mapper.createObjectNode();
                    objectNode.put("riskId", randomNumber);
                    return objectNode;
                })
                .collect(Collectors.toList());
    }

    public Risk getRiskData(final int index) {
        try {
            ObjectNode data = this.riskData.get(index);
            return Risk.builder()
                    .name(data.get("name").asText())
                    .nationality(data.get("name").asText())
                    .riskScore(data.get("risk_score").asInt())
                    .reason(data.get("reason").asText())
                    .build();
        } catch (Exception ex) {
            return new Risk();
        }
    }

    public ObjectNode getRiskDataRandom(final int index) {
        try {
            return this.riskData.get(index);
        } catch (Exception ex) {
            return riskData.get(0);
        }
    }
}

