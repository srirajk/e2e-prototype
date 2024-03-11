package com.example.rph.temporal;

import com.example.rph.model.RequestModel;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface ProcessWorkflow {


    @SignalMethod
    void triggerProcess(final List<RequestModel> requestModels);

    @WorkflowMethod
    void run(final long totalTransactions);

}
