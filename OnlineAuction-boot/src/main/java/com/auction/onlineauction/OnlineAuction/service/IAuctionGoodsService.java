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
     * 根据ID获取商品详情（前台用，仅返回已上架且审核通过的商品）
     */
    AuctionGoods getGoodsByIdForPublic(Long id);

    /**
     * 新增商品
     */
    AuctionGoods addGoods(Map<String, Object> requestData, Long sellerId);

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
     * 真删除商品（物理删除，仅超级管理员）
     */
    void hardDeleteGoods(Long id);

    /**
     * 批量真删除商品（物理删除，仅超级管理员）
     */
    void batchHardDeleteGoods(List<Long> ids);

    /**
     * 审核商品
     */
    void auditGoods(Long id, Integer auditStatus, String auditRemark);

    /**
     * 上架/下架商品（仅修改 shelf_status）
     * @param shelfStatus 0=下架 1=上架
     */
    void updateShelfStatus(Long id, Integer shelfStatus);

    /**
     * 卖家对自己商品的上下架（仅本人可操作，上架需审核通过）
     * @param shelfStatus 0=下架 1=上架
     */
    void updateShelfStatusByOwner(Long goodsId, Long sellerId, Integer shelfStatus);

    /**
     * 获取商品列表（前端API，只显示审核通过的商品）
     */
    PageInfo<AuctionGoods> getGoodsListForApi(Integer current, Integer size, String keyword, Integer status, String categoryId);

    /**
     * 搜索商品（前端API）
     */
    PageInfo<AuctionGoods> searchGoods(Integer current, Integer size, String keyword);

    /**
     * 获取我的商品列表（前端API）
     */
    PageInfo<AuctionGoods> getMyGoodsList(Integer current, Integer size, Long userId);

    /**
     * 根据时间自动更新商品状态
     * 0=未开始 1=竞拍中 2=已成交 3=已流拍
     */
    void updateGoodsStatusByTime(AuctionGoods goods);

    /**
     * 自动下架超过结束时间的商品
     */
    void autoOfflineExpiredGoods();

    /**
     * 重新申请上架（将已下架商品改为待审核状态）
     */
    void reapplyGoods(Long id, Long userId);

    /** 限时拍管理：设置商品竞拍起止时间（管理员、超级管理员、运营） */
    void setGoodsStartEndTime(Long goodsId, Map<String, Object> body);

    /** 拍卖延时：拍卖师/管理员在竞拍中延长结束时间（分钟） */
    void extendAuctionTime(Long goodsId, int minutes);

    /** 流拍：拍卖师/管理员将竞拍中商品标记为流拍 */
    void markNoSale(Long goodsId);

    /** 猜你喜欢：按点击量倒序，只展示上架且审核通过的商品 */
    List<AuctionGoods> getGuessYouLikeGoods(int limit);

    /** 热门商品分页：每页50条，按点击量倒序 */
    PageInfo<AuctionGoods> getHotGoodsPage(Integer current);

    /** 增加商品点击量（进入详情页时调用） */
    void incrementViewCount(Long goodsId);
}
