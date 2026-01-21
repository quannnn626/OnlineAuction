package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 拍卖商品表 服务类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface IAuctionGoodsService extends IService<AuctionGoods> {

    /**
     * 分页查询商品列表（包含文件信息）
     */
    PageInfo<AuctionGoods> getGoodsPage(Integer current, Integer size, String goodsName, String categoryId, Integer auditStatus, Integer goodsStatus);

    /**
     * 获取所有商品列表（不分页，包含文件信息）
     */
    List<AuctionGoods> getGoodsList();

    /**
     * 根据ID获取商品详情（包含文件信息）
     */
    AuctionGoods getGoodsById(Long id);

    /**
     * 新增商品
     */
    AuctionGoods addGoods(Map<String, Object> requestData);

    /**
     * 更新商品
     */
    AuctionGoods updateGoods(Long id, Map<String, Object> requestData);

    /**
     * 删除商品（逻辑删除）
     */
    void deleteGoods(Long id);

    /**
     * 批量删除商品
     */
    void batchDeleteGoods(List<Long> ids);

    /**
     * 审核商品
     */
    void auditGoods(Long id, Integer auditStatus, String auditRemark);

    /**
     * 获取商品列表（前端API，只显示审核通过的商品）
     */
    PageInfo<AuctionGoods> getGoodsListForApi(Integer current, Integer size, String keyword, Integer status);

    /**
     * 搜索商品（前端API）
     */
    PageInfo<AuctionGoods> searchGoods(Integer current, Integer size, String keyword);

    /**
     * 获取我的商品列表（前端API）
     */
    PageInfo<AuctionGoods> getMyGoodsList(Integer current, Integer size, Long userId);
}
