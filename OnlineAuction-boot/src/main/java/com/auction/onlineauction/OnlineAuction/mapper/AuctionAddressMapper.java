package com.auction.onlineauction.OnlineAuction.mapper;

import com.auction.onlineauction.OnlineAuction.entity.AuctionAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface AuctionAddressMapper extends BaseMapper<AuctionAddress> {

    @Update("UPDATE auction_user_address SET is_default = 0 WHERE user_id = #{userId} AND del_flag = 0")
    int clearDefaultByUserId(@Param("userId") Long userId);
}
