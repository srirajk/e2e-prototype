package com.example.rph.temporal;

import com.example.rph.model.ExternalInvocationResponse;
import com.example.rph.model.FilteredRecordRiskModel;
import com.example.rph.model.RequestModel;
import com.example.rph.utility.Utility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WorkflowImpl(taskQueues = "DemoTaskQueue")
@Slf4j
public class ProcessWorkflowImpl implements ProcessWorkflow {

    private boolean workflowCompleted = false;
    private long finishedTransactionCount = 0;

    private long totalTransactions = 0;
    private long startedTransactionCount = 0;

    private final ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(5))
            .setRetryOptions(
                    RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setMaximumInterval(Duration.ofSeconds(10))
                            .build())
            .build();

    public void triggerProcess(final List<RequestModel> requestModels) {
        startedTransactionCount++;
        final ScreenActivity screenActivity = Workflow.newActivityStub(ScreenActivity.class, options);
        ExternalInvocationResponse externalInvocationOutput = screenActivity.invokeExternalAction(requestModels);
        //log.info("External Invocation Output {}", externalInvocationOutput);
        requestModels.forEach(
                requestModel -> {
                    List<ObjectNode> riskIdRefs = externalInvocationOutput.getResponses().get(requestModel.getRecordNumber());
                    FilteredRecordRiskModel filteredRecordRiskModel = screenActivity.processFilters(requestModel, riskIdRefs);
                    //log.info("filteredRecordRiskModel {}", filteredRecordRiskModel);
                    screenActivity.createCase(filteredRecordRiskModel);
                    screenActivity.writeFile(filteredRecordRiskModel);
                }
        );
        finishedTransactionCount++;
    }


    @Override
    public void run(long totalTransactions) {
        log.info("workflow started");
        this.totalTransactions = totalTransactions;
        Workflow.await(() -> {
            if (finishedTransactionCount == totalTransactions) {
                return true;
            } else {
                return false;
            }
        });
        log.info("workflow ended");
    }
}
