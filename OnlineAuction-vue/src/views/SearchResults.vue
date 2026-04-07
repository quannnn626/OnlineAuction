<template>
  <div class="search-results-page">
    <!-- 返回按钮 -->
    <div class="back-section">
      <el-button icon="el-icon-arrow-left" @click="handleBack"
        >返回首页</el-button
      >
    </div>

    <!-- 搜索结果标题 -->
    <div class="search-title">
      <h2>搜索结果</h2>
      <p v-if="searchKeyword">关键词: "{{ searchKeyword }}"</p>
    </div>

    <!-- 商品列表 -->
    <div class="goods-list" v-loading="loading">
      <div v-if="goodsList.length > 0">
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
                :src="getGoodsImage(goods)"
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
      <div v-else-if="!loading" class="no-results">
        <el-empty description="暂无搜索结果"></el-empty>
      </div>
    </div>
  </div>
</template>

<script>
import { searchGoods } from "@/api/goods";

export default {
  name: "SearchResults",
  data() {
    return {
      searchKeyword: "",
      goodsList: [],
      loading: false,
    };
  },
  created() {
    this.searchKeyword = this.$route.query.keyword || "";
    if (this.searchKeyword) {
      this.loadSearchResults();
    }
  },
  methods: {
    async loadSearchResults() {
      this.loading = true;
      try {
        const params = {
          keyword: this.searchKeyword,
          // 其他搜索参数
        };
        const result = await searchGoods(params);
        this.goodsList = result.list || result.records || [];
      } catch (error) {
        this.$message.error("搜索失败，请重试");
      } finally {
        this.loading = false;
      }
    },
    handleBack() {
      this.$router.push("/home");
    },
    handleGoodsClick(goods) {
      this.$router.push({ path: "/goods-detail", query: { id: goods.id } });
    },
    getGoodsImage(goods) {
      const files = goods && goods.files ? goods.files : [];
      if (Array.isArray(files) && files.length > 0) {
        const f = files.find(
          (x) =>
            x &&
            x.filePath &&
            /\.(jpg|jpeg|png|gif|webp|bmp|svg)/i.test(x.filePath),
        );
        if (f && f.filePath) {
          const p = f.filePath;
          return /^https?:\/\//i.test(p) ? p : p.startsWith("/") ? p : "/" + p;
        }
      }
      const goodsImg = goods && goods.goodsImg;
      if (goodsImg) {
        const images = String(goodsImg).split(",");
        const url = images[0] && images[0].trim();
        if (url) return url.startsWith("/") ? url : "/" + url;
      }
      return "/images/no-image.svg";
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
      return statusMap[status] || "未知状态";
    },
    getGoodsStatusClass(status) {
      const classMap = {
        0: "status-not-started",
        1: "status-bidding",
        2: "status-completed",
        3: "status-failed",
      };
      return classMap[status] || "";
    },
  },
};
</script>

<style scoped>
.search-results-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-section {
  margin-bottom: 20px;
}

.search-title {
  margin-bottom: 30px;
  text-align: center;
}

.search-title h2 {
  color: #333;
  margin-bottom: 10px;
}

.search-title p {
  color: #666;
  font-size: 16px;
}

.goods-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.goods-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fff;
}

.goods-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.goods-item-content {
  display: flex;
  padding: 15px;
}

.goods-item-info {
  flex: 1;
  margin-right: 15px;
}

.goods-item-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-item-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.goods-item-price {
  font-size: 16px;
  color: #e74c3c;
  font-weight: bold;
}

.goods-item-price-label {
  color: #666;
  font-weight: normal;
}

.goods-item-image {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.goods-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.goods-item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #f8f9fa;
  font-size: 12px;
  color: #666;
}

.goods-status {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.goods-status.status-not-started {
  background: #f0f0f0;
  color: #666;
}

.goods-status.status-bidding {
  background: #e8f5e8;
  color: #52c41a;
}

.goods-status.status-completed {
  background: #fff7e6;
  color: #fa8c16;
}

.goods-status.status-failed {
  background: #fff1f0;
  color: #f5222f;
}

.no-results {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 0;
}
</style>
