package com.insurance.claims.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 旅游延误险理赔申请实体
 * 
 * @author AI Assistant
 * @since 2025-06-26
 */
@Entity
@Table(name = "travel_delay_claim")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelDelayClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 申请单号
     */
    @Column(name = "claim_number", unique = true, nullable = false)
    private String claimNumber;

    /**
     * 投保人姓名
     */
    @Column(name = "policyholder_name", nullable = false)
    private String policyholderName;

    /**
     * 保单号
     */
    @Column(name = "policy_number", nullable = false)
    private String policyNumber;

    /**
     * 航班号
     */
    @Column(name = "flight_number")
    private String flightNumber;

    /**
     * 计划起飞时间
     */
    @Column(name = "scheduled_departure")
    private LocalDateTime scheduledDeparture;

    /**
     * 实际起飞时间
     */
    @Column(name = "actual_departure")
    private LocalDateTime actualDeparture;

    /**
     * 延误时长（小时）
     */
    @Column(name = "delay_hours")
    private Integer delayHours;

    /**
     * 延误原因
     */
    @Column(name = "delay_reason")
    private String delayReason;

    /**
     * 申请理赔金额
     */
    @Column(name = "claimed_amount", precision = 10, scale = 2)
    private BigDecimal claimedAmount;

    /**
     * 系统计算的理赔金额
     */
    @Column(name = "calculated_amount", precision = 10, scale = 2)
    private BigDecimal calculatedAmount;

    /**
     * 理赔状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "claim_status")
    private ClaimStatus claimStatus;

    /**
     * 审核结果
     */
    @Column(name = "approval_result")
    private String approvalResult;

    /**
     * 审核备注
     */
    @Column(name = "approval_notes", length = 1000)
    private String approvalNotes;

    /**
     * 申请时间
     */
    @Column(name = "claim_date")
    private LocalDateTime claimDate;

    /**
     * 处理时间
     */
    @Column(name = "process_date")
    private LocalDateTime processDate;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (claimDate == null) {
            claimDate = LocalDateTime.now();
        }
        if (claimStatus == null) {
            claimStatus = ClaimStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 理赔状态枚举
     */
    public enum ClaimStatus {
        PENDING("待处理"),
        APPROVED("已批准"),
        REJECTED("已拒绝"),
        PAID("已支付");

        private final String description;

        ClaimStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
