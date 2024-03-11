package com.example.rph.service;

import com.example.rph.model.RequestModel;
import com.example.rph.temporal.ProcessWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class WorkflowService {

    private WorkflowClient workflowClient;

    public WorkflowService(@Autowired WorkflowClient client) {
        this.workflowClient = client;
    }

    public void invokeWorkflow(final String requestId, final List<RequestModel> batchRequest) {
        final ProcessWorkflow processWorkflow = getWorkflowStub(requestId, batchRequest.get(0).getRecordCount());
        processWorkflow.triggerProcess(batchRequest);
        log.info("signalled the trigger process for the requestId {} and records  {}", requestId, batchRequest.size());

    }

    private ProcessWorkflow getWorkflowStub(final String requestId, final long recordCount) {
            final ProcessWorkflow processWorkflow = workflowClient.newWorkflowStub(
                    ProcessWorkflow.class,
                    WorkflowOptions.newBuilder()
                            .setTaskQueue("DemoTaskQueue")
                            .setWorkflowId(requestId)
                            .build());
            try {
                WorkflowClient.start(processWorkflow::run, recordCount);
            } catch (WorkflowExecutionAlreadyStarted exception) {
                //ignore
                log.debug("workflow already started so not starting the workflow for requestId :: "+requestId);
            } catch (Exception ex) {
                throw new RuntimeException("Unable to start the workflow for requestId :: "+requestId);
            }
            return workflowClient.newWorkflowStub(ProcessWorkflow.class, requestId);
        }
    }
