package com.auction.onlineauction.OnlineAuction.mapper;

import com.auction.onlineauction.OnlineAuction.entity.AuctionDeposit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 保证金记录表 Mapper 接口
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface AuctionDepositMapper extends BaseMapper<AuctionDeposit> {

    /** 用户保证金汇总分页（按用户名/昵称搜索） */
    List<Map<String, Object>> selectDepositUserSummaryPage(@Param("userName") String userName);

    /** 平台保证金汇总（总额、可用、冻结、用户数） */
    Map<String, Object> selectPlatformSummary();
}
