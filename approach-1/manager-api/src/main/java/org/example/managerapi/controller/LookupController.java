package org.example.managerapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.common.model.BusinessProduct;
import org.example.managerapi.model.FileEventStatus;
import org.example.managerapi.service.BusinessProductLookupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "LookUp", description = "Controller to query business products and file event status")
@RequestMapping("/api/v1/lookup")
public class LookupController {

    private final BusinessProductLookupService businessProductLookupService;

    public LookupController(BusinessProductLookupService businessProductLookupService) {
        this.businessProductLookupService = businessProductLookupService;
    }

    @Operation(summary = "Get business product by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = BusinessProduct.class)))})
    @GetMapping("/businessProduct/{businessId}/productId/{productId}")
    @ResponseBody
    public BusinessProduct getBusinessProductByBusinessIdAndProductId(
            @Parameter(description = "ID of the business", required = true, example = "business123") @PathVariable("businessId") final String businessId,
            @Parameter(description = "ID of the product", required = true, example = "product123") @PathVariable("productId") final String productId) {
        return businessProductLookupService.getBusinessProductMetadata(businessId, productId);
    }

    @Operation(summary = "Get file event status by request ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = FileEventStatus.class)))})
    @GetMapping("/fileEventStatus/{requestId}")
    @ResponseBody
    public FileEventStatus getFileEventStatus(@Parameter(description = "Request ID") @PathVariable("requestId") String requestId) {
        return businessProductLookupService.getFileEventStatus(requestId);
    }
}