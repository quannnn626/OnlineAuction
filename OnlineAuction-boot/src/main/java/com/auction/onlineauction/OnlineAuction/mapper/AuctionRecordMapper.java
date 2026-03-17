package com.auction.onlineauction.OnlineAuction.mapper;

import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 竞拍记录表 Mapper 接口
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface AuctionRecordMapper extends BaseMapper<AuctionRecord> {

    /**
     * 使用悲观锁查询商品信息（SELECT FOR UPDATE）
     * @param goodsId 商品ID
     * @return 商品信息
     */
    com.auction.onlineauction.OnlineAuction.entity.AuctionGoods selectGoodsForUpdate(@Param("goodsId") Long goodsId);

    /**
     * 更新商品当前最高出价
     * @param goodsId 商品ID
     * @param currentHighestPrice 当前最高出价
     * @return 更新行数
     */
    @Update("UPDATE auction_goods SET current_highest_price = #{currentHighestPrice}, bid_count = bid_count + 1 WHERE id = #{goodsId}")
    int updateGoodsHighestPrice(@Param("goodsId") Long goodsId, @Param("currentHighestPrice") BigDecimal currentHighestPrice);

    /**
     * 将商品的所有竞拍记录的is_highest设置为0
     * @param goodsId 商品ID
     * @return 更新行数
     */
    @Update("UPDATE auction_record SET is_highest = 0 WHERE goods_id = #{goodsId} AND del_flag = 0")
    int clearHighestFlag(@Param("goodsId") Long goodsId);

    /**
     * 查询商品的最高出价记录
     * @param goodsId 商品ID
     * @return 最高出价记录
     */
    @Select("SELECT * FROM auction_record WHERE goods_id = #{goodsId} AND del_flag = 0 AND is_highest = 1 ORDER BY bid_time DESC LIMIT 1")
    AuctionRecord selectHighestRecord(@Param("goodsId") Long goodsId);

    /**
     * 查询商品的竞拍记录列表（按出价时间倒序）
     * @param goodsId 商品ID
     * @param limit 限制数量
     * @return 竞拍记录列表
     */
    @Select("SELECT ar.*, u.nick_name as buyer_name FROM auction_record ar " +
            "LEFT JOIN auction_user u ON ar.buyer_id = u.id " +
            "WHERE ar.goods_id = #{goodsId} AND ar.del_flag = 0 " +
            "ORDER BY ar.bid_time DESC LIMIT #{limit}")
    List<AuctionRecord> selectRecordsByGoodsId(@Param("goodsId") Long goodsId, @Param("limit") Integer limit);

    @Select("<script>" +
            "SELECT g.id AS goodsId, g.goods_name AS goodsName, g.current_highest_price AS currentHighestPrice, " +
            "g.start_time AS startTime, g.end_time AS endTime, g.goods_status AS goodsStatus, " +
            "COUNT(r.id) AS myBidCount, MAX(r.bid_time) AS latestBidTime, MAX(r.bid_price) AS myHighestBid " +
            "FROM auction_record r " +
            "INNER JOIN auction_goods g ON r.goods_id = g.id " +
            "WHERE r.del_flag = 0 AND g.del_flag = 0 AND r.buyer_id = #{buyerId} " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND g.goods_name LIKE CONCAT('%', #{keyword}, '%') " +
            "</if> " +
            "GROUP BY g.id, g.goods_name, g.current_highest_price, g.start_time, g.end_time, g.goods_status " +
            "ORDER BY latestBidTime DESC" +
            "</script>")
    List<Map<String, Object>> selectMyBidGoodsPage(@Param("buyerId") Long buyerId, @Param("keyword") String keyword);

    @Select("<script>" +
            "SELECT g.id AS goodsId, g.goods_name AS goodsName, g.current_highest_price AS currentHighestPrice, " +
            "g.start_time AS startTime, g.end_time AS endTime, g.goods_status AS goodsStatus, " +
            "u.nick_name AS sellerName, COUNT(r.id) AS totalBidCount, MAX(r.bid_time) AS latestBidTime " +
            "FROM auction_goods g " +
            "INNER JOIN auction_record r ON r.goods_id = g.id AND r.del_flag = 0 " +
            "LEFT JOIN auction_user u ON g.seller_id = u.id " +
            "WHERE g.del_flag = 0 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND g.goods_name LIKE CONCAT('%', #{keyword}, '%') " +
            "</if> " +
            "GROUP BY g.id, g.goods_name, g.current_highest_price, g.start_time, g.end_time, g.goods_status, u.nick_name " +
            "ORDER BY latestBidTime DESC" +
            "</script>")
    List<Map<String, Object>> selectAdminBidGoodsPage(@Param("keyword") String keyword);

    @Select("SELECT ar.*, u.nick_name as buyer_name FROM auction_record ar " +
            "LEFT JOIN auction_user u ON ar.buyer_id = u.id " +
            "WHERE ar.goods_id = #{goodsId} AND ar.del_flag = 0 " +
            "ORDER BY ar.bid_time DESC")
    List<AuctionRecord> selectRecordsPageByGoodsId(@Param("goodsId") Long goodsId);

    @Select("SELECT ar.*, u.nick_name as buyer_name FROM auction_record ar " +
            "LEFT JOIN auction_user u ON ar.buyer_id = u.id " +
            "WHERE ar.goods_id = #{goodsId} AND ar.buyer_id = #{buyerId} AND ar.del_flag = 0 " +
            "ORDER BY ar.bid_time DESC")
    List<AuctionRecord> selectMyRecordsPageByGoodsId(@Param("goodsId") Long goodsId, @Param("buyerId") Long buyerId);

    /** 按买方分页查询竞拍记录（含商品名） */
    List<AuctionRecord> selectRecordsByBuyerIdPage(@Param("buyerId") Long buyerId);
}
