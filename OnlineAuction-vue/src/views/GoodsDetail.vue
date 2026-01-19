<template>
  <div class="goods-detail-page">
    <div class="container">
      <!-- 商品图片展示区 -->
      <div class="image-gallery">
        <div class="main-image">
          <img
            src="https://via.placeholder.com/600x400?text=商品主图"
            alt="商品主图"
            class="main-img"
          />
        </div>
        <div class="thumbnail-list">
          <img
            src="https://via.placeholder.com/150x100?text=图片1"
            alt="图片1"
            class="thumbnail"
          />
          <img
            src="https://via.placeholder.com/150x100?text=图片2"
            alt="图片2"
            class="thumbnail"
          />
          <img
            src="https://via.placeholder.com/150x100?text=图片3"
            alt="图片3"
            class="thumbnail"
          />
          <img
            src="https://via.placeholder.com/150x100?text=图片4"
            alt="图片4"
            class="thumbnail"
          />
        </div>
      </div>

      <!-- 商品信息区 -->
      <div class="product-info">
        <h1 class="product-title">{{ goodsInfo.goodsName || "商品名称" }}</h1>
        <div class="price-section">
          <span class="current-price">¥ {{ goodsInfo.basePrice || "0.00" }}</span>
          <span class="price-label">起拍价</span>
        </div>
        <div class="product-category" v-if="categoryNames.length > 0">
          <span class="category-label">商品分类：</span>
          <el-tag
            v-for="(name, index) in categoryNames"
            :key="index"
            type="info"
            size="small"
            style="margin-right: 8px"
          >
            {{ name }}
          </el-tag>
        </div>
        <div class="product-description">
          <h3>商品描述</h3>
          <p>{{ goodsInfo.goodsDesc || "暂无描述" }}</p>
        </div>
        <div class="product-details">
          <h3>商品详情</h3>
          <ul>
            <li>起拍价：¥ {{ goodsInfo.basePrice || "0.00" }}</li>
            <li>最小加价：¥ {{ goodsInfo.addPrice || "0.00" }}</li>
            <li>拍卖开始时间：{{ formatDateTime(goodsInfo.startTime) }}</li>
            <li>拍卖结束时间：{{ formatDateTime(goodsInfo.endTime) }}</li>
            <li>商品状态：{{ getGoodsStatusText(goodsInfo.goodsStatus) }}</li>
          </ul>
        </div>
      </div>

      <!-- 商品视频区 -->
      <div class="video-section">
        <h3>商品视频</h3>
        <video controls class="product-video">
          <source
            src="https://www.w3schools.com/html/mov_bbb.mp4"
            type="video/mp4"
          />
          您的浏览器不支持视频播放。
        </video>
      </div>
    </div>
  </div>
</template>

<script>
import { getGoodsDetail } from "@/api/goods";
import { getCategoryList } from "@/api/category";

export default {
  name: "GoodsDetail",
  data() {
    return {
      goodsInfo: {},
      categoryList: [],
      categoryNames: [],
      loading: false,
    };
  },
  mounted() {
    this.loadGoodsDetail();
    this.loadCategoryList();
  },
  methods: {
    // 加载商品详情
    async loadGoodsDetail() {
      const goodsId = this.$route.query.id;
      if (!goodsId) {
        this.$message.error("商品ID不能为空");
        return;
      }
      this.loading = true;
      try {
        const data = await getGoodsDetail(goodsId);
        this.goodsInfo = data || {};
        // 加载分类名称
        this.loadCategoryNames();
      } catch (error) {
        console.error("加载商品详情失败:", error);
        this.$message.error("加载商品详情失败");
      } finally {
        this.loading = false;
      }
    },
    // 加载分类列表
    async loadCategoryList() {
      try {
        const data = await getCategoryList();
        this.categoryList = data || [];
        // 加载分类名称
        this.loadCategoryNames();
      } catch (error) {
        console.error("加载分类列表失败:", error);
      }
    },
    // 根据categoryId加载分类名称
    loadCategoryNames() {
      if (!this.goodsInfo.categoryId || !this.categoryList.length) {
        this.categoryNames = [];
        return;
      }
      // categoryId可能是逗号分隔的多个ID
      const categoryIds = this.goodsInfo.categoryId.split(",").map((id) => id.trim());
      this.categoryNames = categoryIds
        .map((id) => {
          const category = this.categoryList.find((cat) => cat.id == id);
          return category ? category.categoryName : null;
        })
        .filter((name) => name !== null);
    },
    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return "-";
      const date = new Date(dateTime);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
    // 获取商品状态文本
    getGoodsStatusText(status) {
      const statusMap = {
        0: "未开始",
        1: "竞拍中",
        2: "已成交",
        3: "已流拍",
      };
      return statusMap[status] || "未知";
    },
  },
  watch: {
    "$route.query.id"() {
      this.loadGoodsDetail();
    },
  },
};
</script>

<style scoped>
.goods-detail-page {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.image-gallery {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.main-image {
  flex: 1;
}

.main-img {
  width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.thumbnail-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.thumbnail {
  width: 150px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.2s;
}

.thumbnail:hover {
  transform: scale(1.05);
}

.product-info {
  margin-bottom: 30px;
}

.product-title {
  font-size: 28px;
  margin-bottom: 20px;
  color: #333;
}

.price-section {
  margin-bottom: 20px;
}

.current-price {
  font-size: 24px;
  color: #e74c3c;
  font-weight: bold;
  margin-right: 10px;
}

.original-price {
  font-size: 18px;
  color: #999;
  text-decoration: line-through;
}

.product-description,
.product-details {
  margin-bottom: 20px;
}

.product-description h3,
.product-details h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #333;
}

.product-description p {
  line-height: 1.6;
  color: #666;
}

.product-details ul {
  list-style: none;
  padding: 0;
}

.product-details li {
  padding: 5px 0;
  border-bottom: 1px solid #eee;
  color: #666;
}

.product-category {
  margin-bottom: 20px;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.category-label {
  color: #666;
  font-size: 14px;
  margin-right: 8px;
}

.price-label {
  font-size: 14px;
  color: #999;
  margin-left: 10px;
}

.video-section {
  margin-top: 30px;
}

.video-section h3 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #333;
}

.product-video {
  width: 100%;
  max-width: 600px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
