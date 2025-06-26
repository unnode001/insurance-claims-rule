package com.insurance.claims.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.insurance.claims.model.ClaimDecision;
import com.insurance.claims.model.TravelDelayClaim;

/**
 * 简单规则引擎服务（不使用Drools）
 * 用于测试基础功能，稍后替换为完整的Drools实现
 * 
 * @author AI Assistant
 * @since 2025-06-26
 */
@Service
public class SimpleRuleEngineService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRuleEngineService.class);

    /**
     * 执行理赔规则决策
     * 
     * @param claim 理赔申请
     * @return 理赔决策结果
     */
    public ClaimDecision executeClaimRules(TravelDelayClaim claim) {
        logger.info("开始执行理赔规则，申请单号: {}", claim.getClaimNumber());

        try {
            // 创建决策对象
            ClaimDecision decision = ClaimDecision.builder()
                    .eligible(false)
                    .compensationAmount(BigDecimal.ZERO)
                    .decisionTime(LocalDateTime.now())
                    .ruleDetails(new ArrayList<>())
                    .build();

            // 数据验证
            if (!validateClaimData(claim)) {
                decision.setReason("关键信息缺失：请补充完整的航班时间、保单号和航班号信息");
                decision.setRuleName("信息完整性检查规则");
                decision.setRiskLevel(ClaimDecision.RiskLevel.HIGH);
                decision.setRequiresManualReview(true);
                decision.setReviewSuggestion("请客户补充完整的申请信息后重新提交");
                logger.warn("数据验证失败: {}", decision.getReason());
                return decision;
            }

            // 计算延误时长
            int delayHours = calculateDelayHours(claim);
            claim.setDelayHours(delayHours);

            // 执行业务规则
            if (delayHours < 0) {
                // 提前起飞
                decision.setEligible(false);
                decision.setCompensationAmount(BigDecimal.ZERO);
                decision.setReason("航班提前起飞，不符合延误理赔条件");
                decision.setRuleName("提前起飞规则");
                decision.setRiskLevel(ClaimDecision.RiskLevel.LOW);
                decision.setRequiresManualReview(false);

            } else if (delayHours < 4) {
                // 延误不足4小时
                decision.setEligible(false);
                decision.setCompensationAmount(BigDecimal.ZERO);
                decision.setReason("延误" + delayHours + "小时，不足4小时不符合理赔条件");
                decision.setRuleName("延误不足4小时拒赔规则");
                decision.setRiskLevel(ClaimDecision.RiskLevel.LOW);
                decision.setRequiresManualReview(false);

            } else if (delayHours >= 4 && delayHours < 8) {
                // 延误4-8小时，赔付300元
                decision.setEligible(true);
                decision.setCompensationAmount(new BigDecimal("300.00"));
                decision.setReason("延误" + delayHours + "小时，符合4-8小时理赔条件");
                decision.setRuleName("延误4-8小时理赔规则");
                decision.setRiskLevel(ClaimDecision.RiskLevel.LOW);
                decision.setRequiresManualReview(false);

            } else if (delayHours >= 8 && delayHours <= 24) {
                // 延误8小时以上，赔付600元
                decision.setEligible(true);
                decision.setCompensationAmount(new BigDecimal("600.00"));
                decision.setReason("延误" + delayHours + "小时，符合8小时以上理赔条件");
                decision.setRuleName("延误8小时以上理赔规则");
                decision.setRiskLevel(ClaimDecision.RiskLevel.LOW);
                decision.setRequiresManualReview(false);

            } else {
                // 延误超过24小时，需要人工审核
                decision.setEligible(false);
                decision.setCompensationAmount(BigDecimal.ZERO);
                decision.setReason("延误时长异常（" + delayHours + "小时），需要人工审核");
                decision.setRuleName("异常延误审核规则");
                decision.setRiskLevel(ClaimDecision.RiskLevel.HIGH);
                decision.setRequiresManualReview(true);
                decision.setReviewSuggestion("延误时长超过24小时，建议核实航班信息和延误原因");
            }

            // 检查申请金额是否与系统计算一致
            if (claim.getClaimedAmount() != null && decision.isEligible()) {
                if (claim.getClaimedAmount().compareTo(decision.getCompensationAmount()) != 0) {
                    decision.setRequiresManualReview(true);
                    decision.setRiskLevel(ClaimDecision.RiskLevel.MEDIUM);
                    decision.setReviewSuggestion("申请金额与系统计算不一致，建议人工核实");
                    logger.warn("申请金额{}与系统计算金额{}不一致",
                            claim.getClaimedAmount(), decision.getCompensationAmount());
                }
            }

            // 记录决策结果
            logDecisionResult(claim, decision);

            return decision;

        } catch (Exception e) {
            logger.error("规则执行异常，申请单号: {}", claim.getClaimNumber(), e);

            // 返回异常处理决策
            return ClaimDecision.builder()
                    .eligible(false)
                    .compensationAmount(BigDecimal.ZERO)
                    .reason("系统异常，请联系客服处理")
                    .ruleName("异常处理规则")
                    .decisionTime(LocalDateTime.now())
                    .riskLevel(ClaimDecision.RiskLevel.HIGH)
                    .requiresManualReview(true)
                    .reviewSuggestion("系统执行规则时发生异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 验证理赔申请数据的完整性
     * 
     * @param claim 理赔申请
     * @return 验证结果
     */
    public boolean validateClaimData(TravelDelayClaim claim) {
        if (claim == null) {
            logger.warn("理赔申请对象为空");
            return false;
        }

        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        if (claim.getPolicyNumber() == null || claim.getPolicyNumber().trim().isEmpty()) {
            errors.append("保单号不能为空; ");
            isValid = false;
        }

        if (claim.getFlightNumber() == null || claim.getFlightNumber().trim().isEmpty()) {
            errors.append("航班号不能为空; ");
            isValid = false;
        }

        if (claim.getScheduledDeparture() == null) {
            errors.append("计划起飞时间不能为空; ");
            isValid = false;
        }

        if (claim.getActualDeparture() == null) {
            errors.append("实际起飞时间不能为空; ");
            isValid = false;
        }

        if (claim.getPolicyholderName() == null || claim.getPolicyholderName().trim().isEmpty()) {
            errors.append("投保人姓名不能为空; ");
            isValid = false;
        }

        if (!isValid) {
            logger.warn("理赔申请数据验证失败: {}", errors.toString());
        }

        return isValid;
    }

    /**
     * 计算延误时长
     * 
     * @param claim 理赔申请
     * @return 延误时长（小时）
     */
    public int calculateDelayHours(TravelDelayClaim claim) {
        if (claim.getScheduledDeparture() == null || claim.getActualDeparture() == null) {
            return 0;
        }

        long delayMinutes = java.time.Duration.between(
                claim.getScheduledDeparture(),
                claim.getActualDeparture()).toMinutes();

        // 向上取整到小时，如果是负数（提前起飞）则保持负数
        int delayHours = (int) Math.ceil(delayMinutes / 60.0);

        logger.debug("计算延误时长: 计划起飞 {}, 实际起飞 {}, 延误 {} 小时",
                claim.getScheduledDeparture(),
                claim.getActualDeparture(),
                delayHours);

        return delayHours;
    }

    /**
     * 记录决策结果
     */
    private void logDecisionResult(TravelDelayClaim claim, ClaimDecision decision) {
        logger.info("=== 理赔决策结果 ===");
        logger.info("申请单号: {}", claim.getClaimNumber());
        logger.info("延误时长: {} 小时", claim.getDelayHours());
        logger.info("决策结果: {}", decision.isEligible() ? "符合理赔条件" : "不符合理赔条件");
        logger.info("理赔金额: {} 元", decision.getCompensationAmount());
        logger.info("决策原因: {}", decision.getReason());
        logger.info("匹配规则: {}", decision.getRuleName());
        logger.info("风险等级: {}", decision.getRiskLevel());
        logger.info("需要人工审核: {}", decision.isRequiresManualReview() ? "是" : "否");
        logger.info("==================");
    }
}
