package com.auction.onlineauction.OnlineAuction.mapper;

import com.auction.onlineauction.OnlineAuction.entity.AuctionFeeRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AuctionFeeRecordMapper extends BaseMapper<AuctionFeeRecord> {

    @Select("SELECT order_id FROM auction_fee_record WHERE fee_type = 1 AND del_flag = 0 AND order_id IS NOT NULL")
    List<Long> selectOrderIdsWithCommissionSettled();
}
