package com.example.rph.temporal;


import com.example.rph.model.ExternalInvocationResponse;
import com.example.rph.model.FilteredRecordRiskModel;
import com.example.rph.model.RequestModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.List;
import java.util.Map;

@ActivityInterface
public interface ScreenActivity {

    @ActivityMethod
    ExternalInvocationResponse invokeExternalAction(final List<RequestModel> requestModels);
    //Map<RequestModel, List<ObjectNode>> invokeExternalAction(final List<RequestModel> requestModels);

    @ActivityMethod
    //List<ObjectNode> processFilters(final RequestModel requestModel, final List<ObjectNode> risks);
    FilteredRecordRiskModel processFilters(final RequestModel requestModel, final List<ObjectNode> risks);

    @ActivityMethod
    void createCase(final FilteredRecordRiskModel filteredRecordRiskModel);

    @ActivityMethod
    void writeFile(final FilteredRecordRiskModel filteredRecordRiskModel);
}
