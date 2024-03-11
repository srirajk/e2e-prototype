package com.example.rph.temporal;

import com.example.rph.model.ExternalInvocationResponse;
import com.example.rph.model.FilteredRecordRiskModel;
import com.example.rph.model.RequestModel;
import com.example.rph.service.MockRiskDataService;
import com.example.rph.utility.Utility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Component
@ActivityImpl(taskQueues = "DemoTaskQueue")
@Slf4j
public class ScreenActivityImpl implements ScreenActivity {

    private final String baseFilePath = "/Users/srirajkadimisetty/sample-data/output-temp/";

    private MockRiskDataService mockRiskDataService;

    public ScreenActivityImpl(@Autowired final MockRiskDataService mockRiskDataService) {
        this.mockRiskDataService = mockRiskDataService;
    }

    @Override
    public ExternalInvocationResponse invokeExternalAction(final List<RequestModel> requestModels) {
        Map<Long, List<ObjectNode>> responses = new HashMap<>();
        requestModels.forEach(
                model -> {
                    final Long recordNumber = model.getRecordNumber();
                    if ((recordNumber % 2) == 0) {
                        final List<ObjectNode> randomRiskDataReferences =
                                mockRiskDataService.getRandomRiskDataReferences();
                        responses.put(recordNumber, randomRiskDataReferences);
                    } else {
                        responses.put(recordNumber, null);
                    }
                }
        );
        return new ExternalInvocationResponse(responses);
    }

    //Test code for the processFilters method
    @Override
    public FilteredRecordRiskModel processFilters(final RequestModel requestModel, final List<ObjectNode> risks) {
        FilteredRecordRiskModel riskModel = new FilteredRecordRiskModel();
        riskModel.setRequestId(requestModel.getRequestId());
        riskModel.setFieldLength(requestModel.getFieldLength());
        riskModel.setRecordNumber(requestModel.getRecordNumber());
        riskModel.setFileName(requestModel.getFileName());
        riskModel.setData(requestModel.getData());
        if (Objects.isNull(risks)) {
            riskModel.setHit(false);
            return riskModel;
        } else {
            riskModel.setHit(true);
            riskModel.setRisks(new ArrayList<>(risks.size()));
            risks.forEach(
                    risk -> {
                        int riskId = risk.get("riskId").asInt();
                        riskModel.getRisks().add(mockRiskDataService.getRiskData(riskId));
                    }
            );
            return riskModel;
        }
    }

    @Override
    public void createCase(final FilteredRecordRiskModel filteredRecordRiskModel) {
        if (filteredRecordRiskModel.isHit()) {
            log.info("match found for the requestId {}, fileName {}, recordNumber {}", filteredRecordRiskModel.getRequestId(), filteredRecordRiskModel.getFileName(), filteredRecordRiskModel.getRecordNumber());
        } else {
            log.info("match not found for the requestId {}, fileName {}, recordNumber {}", filteredRecordRiskModel.getRequestId(), filteredRecordRiskModel.getFileName(), filteredRecordRiskModel.getRecordNumber());
        }
    }

    @Override
    public void writeFile(final FilteredRecordRiskModel filteredRecordRiskModel) {
        try {
            String line = Utility.mapper.writeValueAsString(filteredRecordRiskModel);
            final String requestId = filteredRecordRiskModel.getRequestId();
            Path path = Path.of(baseFilePath + requestId);
            checkIfFolderExists(path);
            final String fileName = filteredRecordRiskModel.getRequestId() + "_" + String.valueOf(filteredRecordRiskModel.getRecordNumber()) + ".json";
            Path filePath = path.resolve(fileName);
            Files.writeString(filePath, line, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write data :: " + filteredRecordRiskModel);
        }
    }

    private void checkIfFolderExists(final Path path) {
        if (Files.exists(path)) {
            return;
        }
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create folder :: " + path);
        }
    }
}
