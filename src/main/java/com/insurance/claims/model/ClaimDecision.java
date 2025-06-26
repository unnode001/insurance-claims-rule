package com.insurance.claims.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 理赔决策结果模型
 * 用于封装规则引擎执行后的决策结果
 * 
 * @author AI Assistant
 * @since 2025-06-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDecision {

    /**
     * 是否符合理赔条件
     */
    private boolean eligible;

    /**
     * 理赔金额
     */
    private BigDecimal compensationAmount;

    /**
     * 决策原因
     */
    private String reason;

    /**
     * 匹配的规则名称
     */
    private String ruleName;

    /**
     * 规则匹配的详细信息
     */
    private List<String> ruleDetails;

    /**
     * 决策时间
     */
    private LocalDateTime decisionTime;

    /**
     * 风险等级
     */
    private RiskLevel riskLevel;

    /**
     * 是否需要人工审核
     */
    private boolean requiresManualReview;

    /**
     * 审核建议
     */
    private String reviewSuggestion;

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中风险"),
        HIGH("高风险");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 创建成功的理赔决策
     */
    public static ClaimDecision approved(BigDecimal amount, String reason, String ruleName) {
        return ClaimDecision.builder()
                .eligible(true)
                .compensationAmount(amount)
                .reason(reason)
                .ruleName(ruleName)
                .decisionTime(LocalDateTime.now())
                .riskLevel(RiskLevel.LOW)
                .requiresManualReview(false)
                .build();
    }

    /**
     * 创建拒绝的理赔决策
     */
    public static ClaimDecision rejected(String reason) {
        return ClaimDecision.builder()
                .eligible(false)
                .compensationAmount(BigDecimal.ZERO)
                .reason(reason)
                .decisionTime(LocalDateTime.now())
                .riskLevel(RiskLevel.LOW)
                .requiresManualReview(false)
                .build();
    }

    /**
     * 创建需要人工审核的决策
     */
    public static ClaimDecision requiresReview(String reason, String suggestion) {
        return ClaimDecision.builder()
                .eligible(false)
                .compensationAmount(BigDecimal.ZERO)
                .reason(reason)
                .decisionTime(LocalDateTime.now())
                .riskLevel(RiskLevel.MEDIUM)
                .requiresManualReview(true)
                .reviewSuggestion(suggestion)
                .build();
    }
}
