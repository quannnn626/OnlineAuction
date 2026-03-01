<template>
  <div class="goods-detail-page">
    <div class="container">
      <!-- 商品图片展示区（无图片则不展示） -->
      <div class="image-gallery" v-if="displayImageList.length > 0">
        <div class="main-image">
          <img
            :src="currentMainImage"
            alt="商品主图"
            class="main-img"
          />
        </div>
        <div class="thumbnail-list">
          <img
            v-for="(img, index) in displayImageList"
            :key="img + '-' + index"
            :src="img"
            :alt="'商品图片' + (index + 1)"
            class="thumbnail"
            :class="{ active: selectedImageIndex === index }"
            @click="selectedImageIndex = index"
          />
        </div>
      </div>

      <!-- 商品信息区 -->
      <div class="product-info">
        <h1 class="product-title">{{ goodsInfo.goodsName || "商品名称" }}</h1>
        <div class="price-section">
          <div class="price-item">
            <span class="current-price">¥ {{ goodsInfo.currentHighestPrice || goodsInfo.basePrice || "0.00" }}</span>
            <span class="price-label">{{ goodsInfo.currentHighestPrice ? "当前最高出价" : "起拍价" }}</span>
          </div>
          <div v-if="goodsInfo.currentHighestPrice" class="price-item">
            <span class="base-price-text">起拍价：¥ {{ goodsInfo.basePrice || "0.00" }}</span>
          </div>
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

        <!-- 竞拍区域 -->
        <div class="bid-section" v-if="canBid">
          <h3>参与竞拍</h3>
          <el-form :model="bidForm" :rules="bidRules" ref="bidForm" label-width="100px">
            <el-form-item label="出价金额" prop="bidPrice">
              <el-input-number
                v-model="bidForm.bidPrice"
                :min="minBidPrice"
                :precision="2"
                :step="goodsInfo.addPrice || 1"
                :controls="true"
                style="width: 200px"
              ></el-input-number>
              <span class="bid-tip">最低出价：¥ {{ minBidPrice.toFixed(2) }}</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmitBid" :loading="bidding">
                立即出价
              </el-button>
              <el-button @click="handleQuickBid" :disabled="bidding">
                快速加价（+{{ goodsInfo.addPrice || 0 }}）
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 竞拍记录列表 -->
      <div class="bid-records-section">
        <h3>竞拍记录</h3>
        <el-table :data="bidRecords" style="width: 100%" v-loading="recordsLoading">
          <el-table-column prop="buyerName" label="出价人" width="150"></el-table-column>
          <el-table-column prop="bidPrice" label="出价金额" width="150">
            <template slot-scope="scope">
              <span class="bid-price">¥ {{ scope.row.bidPrice.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="bidTime" label="出价时间" width="180">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.bidTime) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.isHighest === 1" type="success" size="small">当前最高</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 商品视频区（无视频则不展示） -->
      <div class="video-section" v-if="displayVideoList.length > 0">
        <h3>商品视频</h3>
        <video
          v-for="(video, index) in displayVideoList"
          :key="video + '-' + index"
          controls
          class="product-video"
        >
          <source :src="video" type="video/mp4" />
          您的浏览器不支持视频播放。
        </video>
      </div>
    </div>
  </div>
</template>

<script>
import { getGoodsDetail } from "@/api/goods";
import { getCategoryList } from "@/api/category";
import { submitBid, getRecordsByGoodsId } from "@/api/record";

export default {
  name: "GoodsDetail",
  data() {
    return {
      goodsInfo: {},
      categoryList: [],
      categoryNames: [],
      loading: false,
      bidRecords: [],
      recordsLoading: false,
      bidding: false,
      bidForm: {
        bidPrice: 0,
      },
      selectedImageIndex: 0,
      bidRules: {
        bidPrice: [
          { required: true, message: "请输入出价金额", trigger: "blur" },
        ],
      },
    };
  },
  computed: {
    // 是否可以竞拍
    canBid() {
      if (!this.goodsInfo.id) return false;
      const userInfo = localStorage.getItem("userInfo");
      if (!userInfo) return false;
      try {
        const user = JSON.parse(userInfo);
        // 不能竞拍自己发布的商品
        if (user.id === this.goodsInfo.sellerId) return false;
        // 必须是买方用户
        if (!user.isBuyer) return false;
        // 商品状态必须是竞拍中
        if (this.goodsInfo.goodsStatus !== 1) return false;
        // 检查时间
        const now = new Date();
        const startTime = new Date(this.goodsInfo.startTime);
        const endTime = new Date(this.goodsInfo.endTime);
        return now >= startTime && now <= endTime;
      } catch (e) {
        return false;
      }
    },
    // 最低出价
    minBidPrice() {
      if (this.goodsInfo.currentHighestPrice) {
        return parseFloat(this.goodsInfo.currentHighestPrice) + parseFloat(this.goodsInfo.addPrice || 0);
      }
      return parseFloat(this.goodsInfo.basePrice || 0);
    },
    displayImageList() {
      const files = Array.isArray(this.goodsInfo.files) ? this.goodsInfo.files : [];
      const fromFiles = files
        .filter((f) => f && f.filePath && this.isImageFile(f))
        .map((f) => this.normalizeFileUrl(f.filePath));

      if (fromFiles.length > 0) {
        return fromFiles;
      }
      return this.parseLegacyMedia(this.goodsInfo.goodsImg, true);
    },
    displayVideoList() {
      const files = Array.isArray(this.goodsInfo.files) ? this.goodsInfo.files : [];
      const fromFiles = files
        .filter((f) => f && f.filePath && this.isVideoFile(f))
        .map((f) => this.normalizeFileUrl(f.filePath));

      if (fromFiles.length > 0) {
        return fromFiles;
      }
      return this.parseLegacyMedia(this.goodsInfo.goodsImg, false).filter((v) =>
        this.isVideoFile({ filePath: v })
      );
    },
    currentMainImage() {
      if (!this.displayImageList.length) return "";
      if (this.selectedImageIndex >= this.displayImageList.length) {
        return this.displayImageList[0];
      }
      return this.displayImageList[this.selectedImageIndex];
    },
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
        this.selectedImageIndex = 0;
        // 加载分类名称
        this.loadCategoryNames();
        // 加载竞拍记录
        this.loadBidRecords();
        // 初始化出价金额
        this.bidForm.bidPrice = this.minBidPrice;
      } catch (error) {
        this.$message.error("加载商品详情失败");
      } finally {
        this.loading = false;
      }
    },
    // 加载竞拍记录
    async loadBidRecords() {
      const goodsId = this.$route.query.id;
      if (!goodsId) return;
      this.recordsLoading = true;
      try {
        const data = await getRecordsByGoodsId(goodsId, 20);
        this.bidRecords = data || [];
      } catch (error) {
      } finally {
        this.recordsLoading = false;
      }
    },
    // 提交出价
    async handleSubmitBid() {
      this.$refs.bidForm.validate(async (valid) => {
        if (!valid) return;
        if (this.bidForm.bidPrice < this.minBidPrice) {
          this.$message.error(`出价金额不能低于¥ ${this.minBidPrice.toFixed(2)}`);
          return;
        }
        this.bidding = true;
        try {
          const goodsId = this.$route.query.id;
          await submitBid(goodsId, this.bidForm.bidPrice);
          this.$message.success("出价成功！");
          // 重新加载商品详情和竞拍记录
          await this.loadGoodsDetail();
          // 更新出价金额为新的最低出价
          this.bidForm.bidPrice = this.minBidPrice;
        } catch (error) {
          this.$message.error(error.message || "出价失败，请重试");
        } finally {
          this.bidding = false;
        }
      });
    },
    // 快速加价
    handleQuickBid() {
      this.bidForm.bidPrice = this.minBidPrice;
      this.handleSubmitBid();
    },
    // 加载分类列表
    async loadCategoryList() {
      try {
        const data = await getCategoryList();
        this.categoryList = data || [];
        // 加载分类名称
        this.loadCategoryNames();
      } catch (error) {
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
    isImageFile(file) {
      const type = (file.fileType || "").toLowerCase();
      if (type === "image") return true;
      const ext = this.getFileExt(file.filePath || file.fileName || "");
      return ["jpg", "jpeg", "png", "gif", "webp", "bmp"].includes(ext);
    },
    isVideoFile(file) {
      const type = (file.fileType || "").toLowerCase();
      if (type === "video") return true;
      const ext = this.getFileExt(file.filePath || file.fileName || "");
      return ["mp4", "webm", "ogg", "mov", "m4v"].includes(ext);
    },
    getFileExt(path) {
      const clean = (path || "").split("?")[0];
      const index = clean.lastIndexOf(".");
      if (index < 0) return "";
      return clean.substring(index + 1).toLowerCase();
    },
    normalizeFileUrl(url) {
      if (!url) return "";
      if (/^https?:\/\//i.test(url)) return url;
      if (url.startsWith("/")) return url;
      return `/${url}`;
    },
    parseLegacyMedia(goodsImg, onlyImage) {
      if (!goodsImg) return [];
      const list = goodsImg
        .split(",")
        .map((v) => this.normalizeFileUrl(v.trim()))
        .filter((v) => !!v);
      if (!onlyImage) return list;
      return list.filter((v) => this.isImageFile({ filePath: v }));
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
  border: 2px solid transparent;
  cursor: pointer;
  transition: transform 0.2s;
}

.thumbnail:hover {
  transform: scale(1.05);
}

.thumbnail.active {
  border-color: #409eff;
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
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.price-item {
  margin-bottom: 10px;
}

.price-item:last-child {
  margin-bottom: 0;
}

.current-price {
  font-size: 28px;
  color: #e74c3c;
  font-weight: bold;
  margin-right: 10px;
}

.base-price-text {
  font-size: 14px;
  color: #666;
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
  display: block;
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.bid-section {
  margin-top: 30px;
  padding: 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.bid-section h3 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #333;
}

.bid-tip {
  margin-left: 15px;
  color: #999;
  font-size: 14px;
}

.bid-records-section {
  margin-top: 30px;
  padding: 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.bid-records-section h3 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #333;
}

.bid-price {
  color: #e74c3c;
  font-weight: bold;
}
</style>
