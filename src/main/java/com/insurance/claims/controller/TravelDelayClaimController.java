package com.insurance.claims.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.claims.dto.ClaimResponse;
import com.insurance.claims.dto.TravelDelayClaimRequest;
import com.insurance.claims.model.TravelDelayClaim;
import com.insurance.claims.service.TravelDelayClaimService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 旅游延误险理赔API控制器
 * 
 * @author AI Assistant
 * @since 2025-06-26
 */
@RestController
@RequestMapping("/claims")
@Validated
@Tag(name = "旅游延误险理赔", description = "旅游延误险理赔相关API")
public class TravelDelayClaimController {

    private static final Logger logger = LoggerFactory.getLogger(TravelDelayClaimController.class);

    @Autowired
    private TravelDelayClaimService claimService;

    /**
     * 提交理赔申请
     */
    @PostMapping("/submit")
    @Operation(summary = "提交理赔申请", description = "提交旅游延误险理赔申请，系统将自动判断是否符合理赔条件")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "申请提交成功", content = @Content(schema = @Schema(implementation = ClaimResponse.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "系统内部错误")
    })
    public ResponseEntity<ClaimResponse> submitClaim(
            @Parameter(description = "理赔申请信息", required = true) @Valid @RequestBody TravelDelayClaimRequest request) {

        logger.info("收到理赔申请，投保人: {}", request.getPolicyholderName());

        try {
            ClaimResponse response = claimService.processClaim(request);

            logger.info("理赔申请处理完成，申请单号: {}", response.getClaimNumber());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("理赔申请参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            logger.error("处理理赔申请时发生系统错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 查询理赔申请详情
     */
    @GetMapping("/{claimNumber}")
    @Operation(summary = "查询理赔申请详情", description = "根据申请单号查询理赔申请的详细信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = TravelDelayClaim.class))),
            @ApiResponse(responseCode = "404", description = "申请单号不存在")
    })
    public ResponseEntity<TravelDelayClaim> getClaimDetails(
            @Parameter(description = "申请单号", required = true, example = "CLAIM20250626143000") @PathVariable @NotBlank String claimNumber) {

        logger.info("查询理赔申请详情，申请单号: {}", claimNumber);

        return claimService.getClaimByNumber(claimNumber)
                .map(claim -> {
                    logger.info("找到理赔申请，状态: {}", claim.getClaimStatus());
                    return ResponseEntity.ok(claim);
                })
                .orElseGet(() -> {
                    logger.warn("申请单号不存在: {}", claimNumber);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 根据保单号查询理赔申请列表
     */
    @GetMapping("/policy/{policyNumber}")
    @Operation(summary = "根据保单号查询理赔申请", description = "查询指定保单号下的所有理赔申请")
    public ResponseEntity<List<TravelDelayClaim>> getClaimsByPolicy(
            @Parameter(description = "保单号", required = true, example = "POL123456789") @PathVariable @NotBlank String policyNumber) {

        logger.info("查询保单理赔申请，保单号: {}", policyNumber);

        List<TravelDelayClaim> claims = claimService.getClaimsByPolicyNumber(policyNumber);

        logger.info("找到 {} 条理赔申请记录", claims.size());
        return ResponseEntity.ok(claims);
    }

    /**
     * 查询需要人工审核的申请
     */
    @GetMapping("/review/pending")
    @Operation(summary = "查询待审核申请", description = "查询所有需要人工审核的理赔申请")
    public ResponseEntity<List<TravelDelayClaim>> getPendingReviews() {

        logger.info("查询待审核申请列表");

        List<TravelDelayClaim> claims = claimService.getClaimsRequiringReview();

        logger.info("找到 {} 条待审核申请", claims.size());
        return ResponseEntity.ok(claims);
    }

    /**
     * 查询今日申请
     */
    @GetMapping("/today")
    @Operation(summary = "查询今日申请", description = "查询今天提交的所有理赔申请")
    public ResponseEntity<List<TravelDelayClaim>> getTodayClaims() {

        logger.info("查询今日申请列表");

        List<TravelDelayClaim> claims = claimService.getTodayClaims();

        logger.info("今日共有 {} 条申请", claims.size());
        return ResponseEntity.ok(claims);
    }

    /**
     * 人工审核申请
     */
    @PostMapping("/{claimNumber}/review")
    @Operation(summary = "人工审核申请", description = "对指定申请进行人工审核")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "审核完成"),
            @ApiResponse(responseCode = "404", description = "申请单号不存在"),
            @ApiResponse(responseCode = "400", description = "审核参数错误")
    })
    public ResponseEntity<TravelDelayClaim> manualReview(
            @Parameter(description = "申请单号", required = true) @PathVariable @NotBlank String claimNumber,
            @Parameter(description = "审核参数", required = true) @Valid @RequestBody ManualReviewRequest request) {

        logger.info("开始人工审核，申请单号: {}, 结果: {}", claimNumber, request.approved);

        try {
            TravelDelayClaim updatedClaim = claimService.manualReview(
                    claimNumber,
                    request.approved,
                    request.notes);

            logger.info("人工审核完成，申请单号: {}, 状态: {}", claimNumber, updatedClaim.getClaimStatus());
            return ResponseEntity.ok(updatedClaim);

        } catch (IllegalArgumentException e) {
            logger.warn("申请单号不存在: {}", claimNumber);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("人工审核时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 系统健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "系统健康检查", description = "检查理赔系统的运行状态")
    public ResponseEntity<Map<String, Object>> healthCheck() {

        logger.debug("执行系统健康检查");

        Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "service", "Insurance Claims Rule Engine",
                "version", "1.0.0");

        return ResponseEntity.ok(health);
    }

    /**
     * 人工审核请求内部类
     */
    @Schema(description = "人工审核请求")
    public static class ManualReviewRequest {

        @Schema(description = "是否批准", example = "true", required = true)
        public boolean approved;

        @Schema(description = "审核备注", example = "经核实，延误确实由天气原因造成，批准理赔")
        public String notes;
    }
}
