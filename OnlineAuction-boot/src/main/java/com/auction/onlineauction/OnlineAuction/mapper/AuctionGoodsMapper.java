package com.auction.onlineauction.OnlineAuction.mapper;

import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 拍卖商品表 Mapper 接口
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface AuctionGoodsMapper extends BaseMapper<AuctionGoods> {

    @Update("UPDATE auction_goods SET view_count = IFNULL(view_count,0) + 1 WHERE id = #{goodsId} AND del_flag = 0")
    int incrementViewCount(@Param("goodsId") Long goodsId);
}
