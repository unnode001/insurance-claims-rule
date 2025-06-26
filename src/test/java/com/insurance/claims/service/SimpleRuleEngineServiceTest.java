package com.insurance.claims.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.insurance.claims.model.ClaimDecision;
import com.insurance.claims.model.TravelDelayClaim;

/**
 * 简单规则引擎服务测试
 * 
 * @author AI Assistant
 * @since 2025-06-26
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class SimpleRuleEngineServiceTest {

    private SimpleRuleEngineService ruleEngineService;

    @BeforeEach
    void setUp() {
        ruleEngineService = new SimpleRuleEngineService();
    }

    @Test
    @DisplayName("测试延误时长计算")
    void testCalculateDelayHours() {
        // 准备测试数据
        TravelDelayClaim claim = TravelDelayClaim.builder()
                .scheduledDeparture(LocalDateTime.of(2025, 6, 26, 8, 30))
                .actualDeparture(LocalDateTime.of(2025, 6, 26, 12, 30))
                .build();

        // 执行测试
        int delayHours = ruleEngineService.calculateDelayHours(claim);

        // 验证结果
        assertEquals(4, delayHours, "4小时延误计算错误");
    }

    @Test
    @DisplayName("测试4-8小时延误理赔")
    void testDelay4To8Hours() {
        // 准备测试数据
        TravelDelayClaim claim = TravelDelayClaim.builder()
                .claimNumber("TEST001")
                .policyNumber("POL123456789")
                .flightNumber("CZ3251")
                .scheduledDeparture(LocalDateTime.of(2025, 6, 26, 8, 30))
                .actualDeparture(LocalDateTime.of(2025, 6, 26, 12, 30))
                .policyholderName("张三")
                .delayHours(4)
                .build();

        // 执行规则
        ClaimDecision decision = ruleEngineService.executeClaimRules(claim);

        // 验证结果
        assertTrue(decision.isEligible(), "4小时延误应该符合理赔条件");
        assertEquals(new BigDecimal("300.00"), decision.getCompensationAmount(), "4小时延误理赔金额应为300元");
        assertEquals("延误4小时，符合4-8小时理赔条件", decision.getReason());
    }

    @Test
    @DisplayName("测试8小时以上延误理赔")
    void testDelay8HoursOrMore() {
        // 准备测试数据
        TravelDelayClaim claim = TravelDelayClaim.builder()
                .claimNumber("TEST002")
                .policyNumber("POL123456789")
                .flightNumber("CZ3251")
                .scheduledDeparture(LocalDateTime.of(2025, 6, 26, 8, 30))
                .actualDeparture(LocalDateTime.of(2025, 6, 26, 16, 30))
                .policyholderName("李四")
                .delayHours(8)
                .build();

        // 执行规则
        ClaimDecision decision = ruleEngineService.executeClaimRules(claim);

        // 验证结果
        assertTrue(decision.isEligible(), "8小时延误应该符合理赔条件");
        assertEquals(new BigDecimal("600.00"), decision.getCompensationAmount(), "8小时延误理赔金额应为600元");
        assertEquals("延误8小时，符合8小时以上理赔条件", decision.getReason());
    }

    @Test
    @DisplayName("测试不足4小时延误拒赔")
    void testDelayLessThan4Hours() {
        // 准备测试数据
        TravelDelayClaim claim = TravelDelayClaim.builder()
                .claimNumber("TEST003")
                .policyNumber("POL123456789")
                .flightNumber("CZ3251")
                .scheduledDeparture(LocalDateTime.of(2025, 6, 26, 8, 30))
                .actualDeparture(LocalDateTime.of(2025, 6, 26, 11, 0))
                .policyholderName("王五")
                .delayHours(3)
                .build();

        // 执行规则
        ClaimDecision decision = ruleEngineService.executeClaimRules(claim);

        // 验证结果
        assertFalse(decision.isEligible(), "3小时延误不应该符合理赔条件");
        assertEquals(BigDecimal.ZERO, decision.getCompensationAmount(), "3小时延误理赔金额应为0元");
        assertEquals("延误3小时，不足4小时不符合理赔条件", decision.getReason());
    }

    @Test
    @DisplayName("测试数据验证 - 完整数据")
    void testValidateClaimData_ValidData() {
        // 准备完整的测试数据
        TravelDelayClaim claim = TravelDelayClaim.builder()
                .policyNumber("POL123456789")
                .flightNumber("CZ3251")
                .scheduledDeparture(LocalDateTime.now().minusHours(6))
                .actualDeparture(LocalDateTime.now().minusHours(2))
                .policyholderName("张三")
                .build();

        // 执行验证
        boolean isValid = ruleEngineService.validateClaimData(claim);

        // 验证结果
        assertTrue(isValid, "完整数据应该通过验证");
    }

    @Test
    @DisplayName("测试数据验证 - 缺少保单号")
    void testValidateClaimData_MissingPolicyNumber() {
        // 准备缺少保单号的测试数据
        TravelDelayClaim claim = TravelDelayClaim.builder()
                .flightNumber("CZ3251")
                .scheduledDeparture(LocalDateTime.now().minusHours(6))
                .actualDeparture(LocalDateTime.now().minusHours(2))
                .policyholderName("张三")
                .build();

        // 执行验证
        boolean isValid = ruleEngineService.validateClaimData(claim);

        // 验证结果
        assertFalse(isValid, "缺少保单号应该验证失败");
    }
}
