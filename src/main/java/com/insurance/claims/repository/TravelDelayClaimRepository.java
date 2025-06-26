package com.insurance.claims.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.insurance.claims.model.TravelDelayClaim;

/**
 * 旅游延误险理赔申请数据访问接口
 * 
 * @author AI Assistant
 * @since 2025-06-26
 */
@Repository
public interface TravelDelayClaimRepository extends JpaRepository<TravelDelayClaim, Long> {

    /**
     * 根据申请单号查询
     */
    Optional<TravelDelayClaim> findByClaimNumber(String claimNumber);

    /**
     * 根据保单号查询所有理赔申请
     */
    List<TravelDelayClaim> findByPolicyNumber(String policyNumber);

    /**
     * 根据投保人姓名查询
     */
    List<TravelDelayClaim> findByPolicyholderName(String policyholderName);

    /**
     * 根据理赔状态查询
     */
    List<TravelDelayClaim> findByClaimStatus(TravelDelayClaim.ClaimStatus claimStatus);

    /**
     * 根据航班号查询
     */
    List<TravelDelayClaim> findByFlightNumber(String flightNumber);

    /**
     * 查询指定时间范围内的申请
     */
    @Query("SELECT c FROM TravelDelayClaim c WHERE c.claimDate BETWEEN :startDate AND :endDate")
    List<TravelDelayClaim> findByClaimDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 查询延误时长大于指定小时数的申请
     */
    List<TravelDelayClaim> findByDelayHoursGreaterThanEqual(Integer delayHours);

    /**
     * 查询需要人工审核的申请（基于延误时长）
     */
    @Query("SELECT c FROM TravelDelayClaim c WHERE c.delayHours > 24 OR c.claimStatus = 'PENDING'")
    List<TravelDelayClaim> findClaimsRequiringReview();

    /**
     * 统计各状态的申请数量
     */
    @Query("SELECT c.claimStatus, COUNT(c) FROM TravelDelayClaim c GROUP BY c.claimStatus")
    List<Object[]> countByClaimStatus();

    /**
     * 查询今日申请
     */
    @Query("SELECT c FROM TravelDelayClaim c WHERE DATE(c.claimDate) = CURRENT_DATE")
    List<TravelDelayClaim> findTodayClaims();

    /**
     * 检查申请单号是否存在
     */
    boolean existsByClaimNumber(String claimNumber);

    /**
     * 查询指定保单号在指定日期范围内的申请数量
     */
    @Query("SELECT COUNT(c) FROM TravelDelayClaim c WHERE c.policyNumber = :policyNumber " +
            "AND c.claimDate BETWEEN :startDate AND :endDate")
    Long countByPolicyNumberAndDateRange(
            @Param("policyNumber") String policyNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
