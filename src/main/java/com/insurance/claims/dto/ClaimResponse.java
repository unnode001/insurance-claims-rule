package com.insurance.claims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.insurance.claims.model.TravelDelayClaim;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 理赔申请响应DTO
 * 
 * @author AI Assistant
 * @since 2025-06-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "理赔申请响应")
public class ClaimResponse {

    @Schema(description = "申请单号", example = "CLAIM202506260001")
    private String claimNumber;

    @Schema(description = "处理状态", example = "APPROVED")
    private TravelDelayClaim.ClaimStatus status;

    @Schema(description = "是否符合理赔条件", example = "true")
    private boolean eligible;

    @Schema(description = "系统计算的理赔金额", example = "300.00")
    private BigDecimal calculatedAmount;

    @Schema(description = "理赔决策原因", example = "延误4小时，符合理赔条件")
    private String reason;

    @Schema(description = "匹配的规则名称", example = "延误4-8小时理赔规则")
    private String ruleName;

    @Schema(description = "规则执行详情")
    private List<String> ruleDetails;

    @Schema(description = "是否需要人工审核", example = "false")
    private boolean requiresManualReview;

    @Schema(description = "审核建议")
    private String reviewSuggestion;

    @Schema(description = "风险等级", example = "LOW")
    private String riskLevel;

    @Schema(description = "处理时间", example = "2025-06-26 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processTime;

    @Schema(description = "申请详情")
    private ClaimDetails claimDetails;

    /**
     * 申请详情内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "申请详情")
    public static class ClaimDetails {

        @Schema(description = "投保人姓名", example = "张三")
        private String policyholderName;

        @Schema(description = "保单号", example = "POL123456789")
        private String policyNumber;

        @Schema(description = "航班号", example = "CZ3251")
        private String flightNumber;

        @Schema(description = "延误时长（小时）", example = "4")
        private Integer delayHours;

        @Schema(description = "延误原因", example = "天气原因")
        private String delayReason;

        @Schema(description = "申请金额", example = "300.00")
        private BigDecimal claimedAmount;

        @Schema(description = "申请时间", example = "2025-06-26 14:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime claimDate;
    }
}
