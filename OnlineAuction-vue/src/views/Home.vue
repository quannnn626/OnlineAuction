<template>
  <div class="home-container">
    <!-- 顶部搜索区域 -->
    <div class="search-section">
      <div class="search-wrapper">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入商品名称、关键词搜索"
          size="large"
          @keyup.enter.native="handleSearch"
        >
          <el-button
            slot="append"
            icon="el-icon-search"
            @click="handleSearch"
          ></el-button>
        </el-input>
      </div>
    </div>

    <!-- 轮播图区域 -->
    <div class="banner-section">
      <el-carousel
        :interval="4000"
        type="card"
        height="400px"
        class="banner-carousel"
        indicator-position="outside"
      >
        <el-carousel-item
          v-for="(banner, index) in bannerList"
          :key="index"
          @click.native="handleBannerClick(banner)"
        >
          <div
            class="banner-item"
            :style="{
              backgroundImage:
                'url(' +
                (banner.bannerImg || '/images/banner-placeholder.svg') +
                ')',
            }"
          >
            <div
              style="
                text-align: center;
                background: rgba(0, 0, 0, 0.3);
                padding: 20px;
                border-radius: 8px;
              "
            >
              <h2 style="margin: 0 0 10px 0; color: #fff">
                {{ banner.goodsName || "轮播图 " + (index + 1) }}
              </h2>
              <p style="margin: 0; font-size: 16px; color: #fff">
                点击查看详情
              </p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 商品类别区域 -->
    <div class="category-section">
      <div class="category-title"><i class="el-icon-menu"></i> 商品分类</div>
      <div class="category-nav" v-loading="categoryLoading">
        <!-- 全部按钮 -->
        <div
          class="category-nav-item"
          :class="{ active: selectedCategoryId === 0 }"
          @click="handleCategoryClick(0)"
        >
          全部
        </div>
        <!-- 一级分类导航 -->
        <div
          v-for="category in level1Categories"
          :key="category.id"
          class="category-nav-item"
          :class="{ active: selectedCategoryId === category.id }"
          @mouseenter="handleCategoryHover(category)"
          @mouseleave="handleCategoryLeave"
          @click="handleCategoryClick(category.id)"
        >
          {{ category.categoryName }}
          <!-- 悬浮下拉面板（只有当有子分类时才显示） -->
          <div
            v-if="hoveredCategoryId === category.id && hasChildren(category)"
            class="category-dropdown"
            @mouseenter="handleDropdownEnter"
            @mouseleave="handleDropdownLeave"
          >
            <div class="category-dropdown-content">
              <!-- 遍历二级分类 -->
              <div
                v-for="level2Category in category.children"
                :key="level2Category.id"
                class="category-dropdown-row"
              >
                <!-- 二级分类（左侧） -->
                <div
                  class="category-level2"
                  @click.stop="handleCategoryClick(level2Category.id)"
                >
                  {{ level2Category.categoryName }}
                </div>
                <!-- 三级分类（右侧） -->
                <div class="category-level3-list">
                  <span
                    v-for="level3Category in (level2Category.children || [])"
                    :key="level3Category.id"
                    class="category-level3-item"
                    @click.stop="handleCategoryClick(level3Category.id)"
                  >
                    {{ level3Category.categoryName }}
                  </span>
                  <span
                    v-if="!level2Category.children || level2Category.children.length === 0"
                    class="category-level3-empty"
                  >
                    暂无三级分类
                  </span>
                </div>
              </div>
              <!-- 如果二级分类为空，显示提示 -->
              <div
                v-if="!category.children || category.children.length === 0"
                class="category-dropdown-empty"
              >
                暂无二级分类
              </div>
            </div>
          </div>
        </div>
        <div
          v-if="!categoryLoading && level1Categories.length === 0"
          class="category-empty"
        >
          暂无分类数据
        </div>
      </div>
    </div>

    <!-- 猜你喜欢商品列表区域 -->
    <div class="goods-section">
      <div class="goods-title"><i class="el-icon-thumb"></i> 猜你喜欢</div>
      <div class="goods-list" v-if="goodsList.length > 0">
        <div
          v-for="goods in goodsList"
          :key="goods.id"
          class="goods-item"
          @click="handleGoodsClick(goods)"
        >
          <div class="goods-item-content">
            <div class="goods-item-info">
              <div class="goods-item-title">{{ goods.goodsName }}</div>
              <div class="goods-item-desc">
                {{ goods.goodsDesc || "暂无描述" }}
              </div>
              <div class="goods-item-price">
                <span class="goods-item-price-label">起拍价：</span>
                ¥{{ goods.basePrice }}
              </div>
            </div>
            <div class="goods-item-image">
              <img
                :src="getGoodsImage(goods.goodsImg)"
                :alt="goods.goodsName"
                @error="handleImageError"
              />
            </div>
          </div>
          <div class="goods-item-footer">
            <span>
              <i class="el-icon-time"></i>
              {{ formatTime(goods.startTime) }} -
              {{ formatTime(goods.endTime) }}
            </span>
            <span
              class="goods-status"
              :class="getGoodsStatusClass(goods.goodsStatus)"
            >
              {{ getGoodsStatusText(goods.goodsStatus) }}
            </span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无商品数据"></el-empty>
    </div>
  </div>
</template>

<script>
import { getCategoryTreeForHome } from "@/api/category";

export default {
  name: "Home",
  data() {
    return {
      searchKeyword: "",
      bannerList: [
        {
          id: 1,
          bannerImg: "/images/banner-placeholder.svg",
          goodsId: 1,
          goodsName: "精品古董花瓶",
        },
        {
          id: 2,
          bannerImg: "/images/banner-placeholder.svg",
          goodsId: 2,
          goodsName: "限量版名表",
        },
        {
          id: 3,
          bannerImg: "/images/banner-placeholder.svg",
          goodsId: 3,
          goodsName: "名家字画",
        },
      ],
      categoryTree: [], // 完整的分类树数据
      level1Categories: [], // 一级分类列表
      selectedCategoryId: 0,
      hoveredCategoryId: null, // 当前悬浮的一级分类ID
      categoryLoading: false,
      dropdownTimer: null, // 用于延迟隐藏下拉面板
      goodsList: [
        {
          id: 1,
          goodsName: "清代青花瓷花瓶",
          goodsDesc:
            "清代乾隆年间青花瓷花瓶，保存完好，具有很高的收藏价值。瓶身绘有精美的花鸟图案，工艺精湛。",
          goodsImg: "/images/goods-placeholder.svg",
          basePrice: 50000.0,
          startTime: "2025-01-01 10:00:00",
          endTime: "2025-01-05 18:00:00",
          goodsStatus: 1,
        },
      ],
    };
  },
  mounted() {
    this.loadCategories();
  },
  methods: {
    // 加载商品分类树
    async loadCategories() {
      this.categoryLoading = true;
      try {
        const data = await getCategoryTreeForHome(false); // 只获取启用的分类（公开接口，无需管理员权限）
        this.categoryTree = data || [];
        // 提取一级分类
        this.level1Categories = this.categoryTree.filter(
          (cat) => cat.level === 1
        );
      } catch (error) {
        console.error("加载商品分类失败:", error);
        this.categoryTree = [];
        this.level1Categories = [];
      } finally {
        this.categoryLoading = false;
      }
    },
    // 处理分类悬浮
    handleCategoryHover(category) {
      // 清除之前的定时器
      if (this.dropdownTimer) {
        clearTimeout(this.dropdownTimer);
        this.dropdownTimer = null;
      }
      this.hoveredCategoryId = category.id;
    },
    // 处理分类离开
    handleCategoryLeave() {
      // 延迟隐藏下拉面板，避免鼠标移动到下拉面板时闪烁
      this.dropdownTimer = setTimeout(() => {
        this.hoveredCategoryId = null;
      }, 200);
    },
    // 处理下拉面板进入
    handleDropdownEnter() {
      // 清除隐藏定时器
      if (this.dropdownTimer) {
        clearTimeout(this.dropdownTimer);
        this.dropdownTimer = null;
      }
    },
    // 处理下拉面板离开
    handleDropdownLeave() {
      this.hoveredCategoryId = null;
    },
    // 检查分类是否有子分类
    hasChildren(category) {
      return category.children && category.children.length > 0;
    },
    handleSearch() {
      if (!this.searchKeyword.trim()) {
        this.$message.warning("请输入搜索关键词");
        return;
      }
      this.$router.push({
        path: "/search-results",
        query: { keyword: this.searchKeyword.trim() },
      });
    },
    handleBannerClick(banner) {
      if (banner.goodsId) {
        this.$router.push({
          path: "/goods-detail",
          query: { id: banner.goodsId },
        });
      }
    },
    handleCategoryClick(categoryId) {
      this.selectedCategoryId = categoryId;
      // 跳转到商品列表页面，带上分类ID参数
      if (categoryId === 0) {
        this.$router.push({ path: "/goods" });
      } else {
        this.$router.push({
          path: "/goods",
          query: { categoryId: categoryId },
        });
      }
    },
    handleGoodsClick(goods) {
      this.$router.push({ path: "/goods-detail", query: { id: goods.id } });
    },
    getGoodsImage(goodsImg) {
      if (!goodsImg) {
        return "/images/no-image.svg";
      }
      const images = goodsImg.split(",");
      return images[0].trim();
    },
    handleImageError(event) {
      event.target.src = "/images/no-image.svg";
    },
    formatTime(timeStr) {
      if (!timeStr) return "";
      const date = new Date(timeStr);
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      const hours = date.getHours().toString().padStart(2, "0");
      const minutes = date.getMinutes().toString().padStart(2, "0");
      return month + "-" + day + " " + hours + ":" + minutes;
    },
    getGoodsStatusText(status) {
      const statusMap = {
        0: "未开始",
        1: "竞拍中",
        2: "已成交",
        3: "已流拍",
      };
      return statusMap[status] || "未知";
    },
    getGoodsStatusClass(status) {
      const classMap = {
        0: "goods-status-upcoming",
        1: "goods-status-auctioning",
        2: "goods-status-ended",
        3: "goods-status-ended",
      };
      return classMap[status] || "";
    },
  },
};
</script>

<style scoped>
.home-container {
  max-width: 1400px;
  margin: 0 auto;
}

.search-section {
  background: #fff;
  padding: 30px 40px;
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.banner-section {
  margin-bottom: 30px;
}

.banner-carousel {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.banner-item {
  height: 400px;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.banner-item:hover {
  opacity: 0.9;
}

.category-section {
  background: #fff;
  padding: 30px;
  margin-bottom: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.category-title {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #409eff;
}

.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  position: relative;
}

.category-nav-item {
  position: relative;
  flex: 0 0 auto;
  padding: 12px 24px;
  background: #f5f7fa;
  border-radius: 0;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  border-right: 1px solid #e4e7ed;
  font-size: 14px;
  color: #303133;
}

.category-nav-item:first-child {
  border-top-left-radius: 4px;
  border-bottom-left-radius: 4px;
}

.category-nav-item:last-child {
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
  border-right: 2px solid transparent;
}

.category-nav-item:hover {
  background: #ecf5ff;
  color: #409eff;
  z-index: 10;
}

.category-nav-item.active {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

/* 下拉面板样式 */
.category-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 600px;
  max-width: 900px;
  z-index: 1000;
  margin-top: 2px;
  padding: 20px;
  animation: fadeInDown 0.2s ease-out;
  max-height: 500px;
  overflow-y: auto;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.category-dropdown-content {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.category-dropdown-row {
  display: flex;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.category-dropdown-row:last-child {
  border-bottom: none;
}

.category-dropdown-row:hover {
  background-color: #f5f7fa;
}

.category-level2 {
  flex: 0 0 140px;
  font-weight: 500;
  color: #303133;
  padding-right: 20px;
  cursor: pointer;
  transition: color 0.2s;
  font-size: 14px;
  line-height: 1.8;
  word-break: keep-all;
}

.category-level2:hover {
  color: #409eff;
}

.category-level3-list {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.category-level3-item {
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.category-level3-item:hover {
  background: #ecf5ff;
  color: #409eff;
}

.category-level3-empty {
  font-size: 12px;
  color: #909399;
  font-style: italic;
}

.category-dropdown-empty {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
  font-size: 14px;
}

/* 下拉面板滚动条样式 */
.category-dropdown::-webkit-scrollbar {
  width: 6px;
}

.category-dropdown::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 3px;
}

.category-dropdown::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.category-dropdown::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

.category-empty {
  width: 100%;
  text-align: center;
  color: #909399;
  font-size: 14px;
  padding: 20px 0;
}

.goods-section {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.goods-title {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #409eff;
}

.goods-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.goods-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  background: #fff;
}

.goods-item:hover {
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.goods-item-content {
  display: flex;
  padding: 15px;
}

.goods-item-info {
  flex: 1;
  min-width: 0;
  padding-right: 15px;
}

.goods-item-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-item-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.goods-item-price {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.goods-item-price-label {
  font-size: 12px;
  color: #909399;
  margin-right: 5px;
}

.goods-item-image {
  flex-shrink: 0;
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f7fa;
}

.goods-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.goods-item-footer {
  padding: 10px 15px;
  background: #f5f7fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.goods-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.goods-status-auctioning {
  background: #f0f9ff;
  color: #409eff;
}

.goods-status-upcoming {
  background: #fef0f0;
  color: #f56c6c;
}

.goods-status-ended {
  background: #f5f7fa;
  color: #909399;
}
</style>
