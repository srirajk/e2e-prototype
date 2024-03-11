package com.example.rph.service;

import com.example.rph.model.RequestModel;
import com.example.rph.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class RecordKafkaBatchListener {

    private final WorkflowService workflowService;

    private final String aDefault = "default";


    public RecordKafkaBatchListener(@Autowired final WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @KafkaListener(
            topics = "test-data",
            containerFactory = "kafkaListenerContainerFactory")
    public void commonListenerForMultipleTopics(ConsumerRecord<String, String> record) {
        //log.info("ConsumerRecord :: {}", record);
        Map<String, byte[]> currentRecordHeaders = StreamSupport.stream(record.headers().spliterator(), false)
                .collect(Collectors.toMap(header -> header.key(), header -> header.value()));
        final String requestId = currentRecordHeaders.containsKey("requestId") ? new String(currentRecordHeaders.get("requestId")) : aDefault;
        if (!aDefault.equalsIgnoreCase(requestId)) {
            final RequestModel model = new RequestModel();
            model.setRequestId(requestId);
            model.setData(Utility.convertToObjectNode(record.value()));
            model.setFileName(new String(new String(currentRecordHeaders.get("fileName"))));
            model.setFieldLength(Utility.bytesToLong(currentRecordHeaders.get("fieldLength")));
            model.setRecordNumber(Utility.bytesToLong(currentRecordHeaders.get("recordNumber")));
            model.setRecordCount(Utility.bytesToInt(currentRecordHeaders.get("recordCount")));
            this.workflowService.invokeWorkflow(requestId, List.of(model));
        } else {
            log.debug("ignore on offset :: {}", record.offset());
        }
    }

   // @KafkaListener(topics = "test-data", containerFactory = "kafkaListenerContainerFactory")
    //public void listenWithHeadersNew(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {
    public void listenWithHeadersNew(final List<ConsumerRecord<String, String>> records) {
        Map<String, List<List<RequestModel>>> groupedBatches = new HashMap<>();
        for (ConsumerRecord<String, String> record : records) {
            log.info("Record :: {} :: {}", record.offset(), record.value());
            final String aDefault = "default";
            Map<String, byte[]> currentRecordHeaders = StreamSupport.stream(record.headers().spliterator(), false)
                    .collect(Collectors.toMap(header -> header.key(), header -> header.value()));
            final String requestId = currentRecordHeaders.containsKey("requestId") ? new String(currentRecordHeaders.get("requestId")) : aDefault;
            if (!aDefault.equalsIgnoreCase(requestId)) {
                groupedBatches.putIfAbsent(requestId, new ArrayList<>());

                List<List<RequestModel>> batches = groupedBatches.get(requestId);

                RequestModel model = new RequestModel();
                model.setRequestId(requestId);
                model.setData(Utility.convertToObjectNode(record.value()));
                model.setFileName(new String(new String(currentRecordHeaders.get("fileName"))));
                model.setFieldLength(Utility.bytesToLong(currentRecordHeaders.get("fieldLength")));
                model.setRecordNumber(Utility.bytesToLong(currentRecordHeaders.get("recordNumber")));
                // Ensure there's at least one batch to start with
                if (batches.isEmpty()) {
                    batches.add(new ArrayList<>());
                }

                List<RequestModel> currentBatch = batches.get(batches.size() - 1);

                // If the current batch is full (100 messages), start a new batch
                if (currentBatch.size() == 100) {
                    List<RequestModel> newBatch = new ArrayList<>();
                    newBatch.add(model);
                    batches.add(newBatch);
                } else {
                    // Otherwise, add the current record to the existing batch
                    currentBatch.add(model);
                }
            }
            processGroupedMessages(groupedBatches);
        }
      //  acknowledgment.acknowledge();
    }

    private void processGroupedMessages(final Map<String, List<List<RequestModel>>> groupedBatches) {
        groupedBatches.entrySet().forEach(
                entry -> {
                    log.info("For RequestId {}, there are {} number of batches received.", entry.getKey(), entry.getValue().size());
                    entry.getValue().parallelStream().forEach(
                            lst -> this.workflowService.invokeWorkflow(entry.getKey(), lst));
                }
        );
    }


}
