package com.insurance.claims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 旅游延误险理赔申请请求DTO
 * 
 * @author AI Assistant
 * @since 2025-06-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "旅游延误险理赔申请请求")
public class TravelDelayClaimRequest {

    @Schema(description = "投保人姓名", example = "张三", required = true)
    @NotBlank(message = "投保人姓名不能为空")
    @Size(max = 50, message = "投保人姓名长度不能超过50个字符")
    private String policyholderName;

    @Schema(description = "保单号", example = "POL123456789", required = true)
    @NotBlank(message = "保单号不能为空")
    @Pattern(regexp = "^POL\\d{9}$", message = "保单号格式不正确，应为POL开头后跟9位数字")
    private String policyNumber;

    @Schema(description = "航班号", example = "CZ3251", required = true)
    @NotBlank(message = "航班号不能为空")
    @Pattern(regexp = "^[A-Z]{2}\\d{3,4}$", message = "航班号格式不正确")
    private String flightNumber;

    @Schema(description = "计划起飞时间", example = "2025-06-26 08:30:00", required = true)
    @NotNull(message = "计划起飞时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledDeparture;

    @Schema(description = "实际起飞时间", example = "2025-06-26 12:30:00", required = true)
    @NotNull(message = "实际起飞时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualDeparture;

    @Schema(description = "延误原因", example = "天气原因")
    @Size(max = 200, message = "延误原因长度不能超过200个字符")
    private String delayReason;

    @Schema(description = "申请理赔金额", example = "300.00")
    @DecimalMin(value = "0.01", message = "申请理赔金额必须大于0")
    @DecimalMax(value = "9999.99", message = "申请理赔金额不能超过9999.99")
    @Digits(integer = 4, fraction = 2, message = "金额格式不正确")
    private BigDecimal claimedAmount;

    /**
     * 计算延误时长（小时）
     */
    public int calculateDelayHours() {
        if (scheduledDeparture == null || actualDeparture == null) {
            return 0;
        }

        long delayMinutes = java.time.Duration.between(scheduledDeparture, actualDeparture).toMinutes();
        return Math.max(0, (int) Math.ceil(delayMinutes / 60.0));
    }
}
