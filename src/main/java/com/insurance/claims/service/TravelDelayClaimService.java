package com.insurance.claims.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.claims.dto.ClaimResponse;
import com.insurance.claims.dto.TravelDelayClaimRequest;
import com.insurance.claims.model.ClaimDecision;
import com.insurance.claims.model.TravelDelayClaim;
import com.insurance.claims.repository.TravelDelayClaimRepository;

/**
 * 旅游延误险理赔业务服务
 * 
 * @author AI Assistant
 * @since 2025-06-26
 */
@Service
@Transactional
public class TravelDelayClaimService {

    private static final Logger logger = LoggerFactory.getLogger(TravelDelayClaimService.class);

    @Autowired
    private TravelDelayClaimRepository claimRepository;

    @Autowired
    private SimpleRuleEngineService ruleEngineService;

    /**
     * 处理理赔申请
     * 
     * @param request 理赔申请请求
     * @return 理赔处理结果
     */
    public ClaimResponse processClaim(TravelDelayClaimRequest request) {
        logger.info("开始处理理赔申请，投保人: {}, 保单号: {}",
                request.getPolicyholderName(), request.getPolicyNumber());

        try {
            // 1. 转换请求为实体对象
            TravelDelayClaim claim = convertRequestToClaim(request);

            // 2. 计算延误时长
            int delayHours = ruleEngineService.calculateDelayHours(claim);
            claim.setDelayHours(delayHours);

            // 3. 数据验证
            if (!ruleEngineService.validateClaimData(claim)) {
                throw new IllegalArgumentException("理赔申请数据不完整或格式错误");
            }

            // 4. 保存申请记录
            claim = claimRepository.save(claim);
            logger.info("理赔申请已保存，申请单号: {}", claim.getClaimNumber());

            // 5. 执行规则引擎决策
            ClaimDecision decision = ruleEngineService.executeClaimRules(claim);

            // 6. 更新申请状态和结果
            updateClaimWithDecision(claim, decision);
            claim = claimRepository.save(claim);

            // 7. 构建响应结果
            ClaimResponse response = buildClaimResponse(claim, decision);

            logger.info("理赔申请处理完成，申请单号: {}, 结果: {}",
                    claim.getClaimNumber(),
                    decision.isEligible() ? "批准" : "拒绝");

            return response;

        } catch (Exception e) {
            logger.error("处理理赔申请失败", e);
            throw new RuntimeException("处理理赔申请失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据申请单号查询理赔申请
     * 
     * @param claimNumber 申请单号
     * @return 理赔申请信息
     */
    public Optional<TravelDelayClaim> getClaimByNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber);
    }

    /**
     * 根据保单号查询理赔申请列表
     * 
     * @param policyNumber 保单号
     * @return 理赔申请列表
     */
    public List<TravelDelayClaim> getClaimsByPolicyNumber(String policyNumber) {
        return claimRepository.findByPolicyNumber(policyNumber);
    }

    /**
     * 查询需要人工审核的申请
     * 
     * @return 需要审核的申请列表
     */
    public List<TravelDelayClaim> getClaimsRequiringReview() {
        return claimRepository.findClaimsRequiringReview();
    }

    /**
     * 查询今日申请
     * 
     * @return 今日申请列表
     */
    public List<TravelDelayClaim> getTodayClaims() {
        return claimRepository.findTodayClaims();
    }

    /**
     * 人工审核申请
     * 
     * @param claimNumber 申请单号
     * @param approved    是否批准
     * @param notes       审核备注
     * @return 更新后的申请信息
     */
    public TravelDelayClaim manualReview(String claimNumber, boolean approved, String notes) {
        logger.info("开始人工审核，申请单号: {}, 结果: {}", claimNumber, approved ? "批准" : "拒绝");

        TravelDelayClaim claim = claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new IllegalArgumentException("申请单号不存在: " + claimNumber));

        claim.setClaimStatus(approved ? TravelDelayClaim.ClaimStatus.APPROVED : TravelDelayClaim.ClaimStatus.REJECTED);
        claim.setApprovalResult(approved ? "人工审核通过" : "人工审核拒绝");
        claim.setApprovalNotes(notes);
        claim.setProcessDate(LocalDateTime.now());

        claim = claimRepository.save(claim);

        logger.info("人工审核完成，申请单号: {}, 状态: {}", claimNumber, claim.getClaimStatus());
        return claim;
    }

    /**
     * 转换请求对象为实体对象
     */
    private TravelDelayClaim convertRequestToClaim(TravelDelayClaimRequest request) {
        return TravelDelayClaim.builder()
                .claimNumber(generateClaimNumber())
                .policyholderName(request.getPolicyholderName())
                .policyNumber(request.getPolicyNumber())
                .flightNumber(request.getFlightNumber())
                .scheduledDeparture(request.getScheduledDeparture())
                .actualDeparture(request.getActualDeparture())
                .delayReason(request.getDelayReason())
                .claimedAmount(request.getClaimedAmount())
                .claimStatus(TravelDelayClaim.ClaimStatus.PENDING)
                .claimDate(LocalDateTime.now())
                .build();
    }

    /**
     * 生成申请单号
     */
    private String generateClaimNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        return "CLAIM" + datePart + timePart;
    }

    /**
     * 根据决策结果更新申请信息
     */
    private void updateClaimWithDecision(TravelDelayClaim claim, ClaimDecision decision) {
        claim.setCalculatedAmount(decision.getCompensationAmount());
        claim.setApprovalResult(decision.getReason());
        claim.setProcessDate(LocalDateTime.now());

        if (decision.isRequiresManualReview()) {
            claim.setClaimStatus(TravelDelayClaim.ClaimStatus.PENDING);
            claim.setApprovalNotes("需要人工审核: " + decision.getReviewSuggestion());
        } else {
            claim.setClaimStatus(decision.isEligible() ? TravelDelayClaim.ClaimStatus.APPROVED
                    : TravelDelayClaim.ClaimStatus.REJECTED);
        }
    }

    /**
     * 构建响应对象
     */
    private ClaimResponse buildClaimResponse(TravelDelayClaim claim, ClaimDecision decision) {
        ClaimResponse.ClaimDetails details = ClaimResponse.ClaimDetails.builder()
                .policyholderName(claim.getPolicyholderName())
                .policyNumber(claim.getPolicyNumber())
                .flightNumber(claim.getFlightNumber())
                .delayHours(claim.getDelayHours())
                .delayReason(claim.getDelayReason())
                .claimedAmount(claim.getClaimedAmount())
                .claimDate(claim.getClaimDate())
                .build();

        return ClaimResponse.builder()
                .claimNumber(claim.getClaimNumber())
                .status(claim.getClaimStatus())
                .eligible(decision.isEligible())
                .calculatedAmount(decision.getCompensationAmount())
                .reason(decision.getReason())
                .ruleName(decision.getRuleName())
                .ruleDetails(decision.getRuleDetails())
                .requiresManualReview(decision.isRequiresManualReview())
                .reviewSuggestion(decision.getReviewSuggestion())
                .riskLevel(decision.getRiskLevel() != null ? decision.getRiskLevel().name() : null)
                .processTime(claim.getProcessDate())
                .claimDetails(details)
                .build();
    }
}
