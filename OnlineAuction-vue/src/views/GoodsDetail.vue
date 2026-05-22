<template>
  <div class="goods-detail-page">
    <div class="container">
      <div class="back-section">
        <el-button icon="el-icon-arrow-left" @click="handleBack">
          返回
        </el-button>
      </div>

      <!-- 商品图片展示区（无图片则不展示） -->
      <div
        class="image-gallery"
        :class="{ 'is-off-shelf': isOffShelf }"
        v-if="displayImageList.length > 0"
      >
        <div class="main-image">
          <img :src="currentMainImage" alt="商品主图" class="main-img" />
          <div v-if="isOffShelf" class="off-shelf-mask">
            <span class="off-shelf-text">/ 已下架 /</span>
          </div>
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

      <div class="top-actions" v-if="canComplain">
        <el-dropdown @command="handleComplaintCommand">
          <el-button type="text" class="more-btn">
            <i class="el-icon-more"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="goods">对商品投诉</el-dropdown-item>
            <el-dropdown-item command="bid">对竞价行为投诉</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>

      <!-- 商品信息区 -->
      <div class="product-info">
        <h1 class="product-title">{{ goodsInfo.goodsName || "商品名称" }}</h1>
        <div class="price-section">
          <div class="price-item">
            <span class="current-price"
              >¥
              {{
                goodsInfo.currentHighestPrice || goodsInfo.basePrice || "0.00"
              }}</span
            >
            <span class="price-label">{{
              goodsInfo.currentHighestPrice ? "当前最高出价" : "起拍价"
            }}</span>
          </div>
          <div v-if="goodsInfo.currentHighestPrice" class="price-item">
            <span class="base-price-text"
              >起拍价：¥ {{ goodsInfo.basePrice || "0.00" }}</span
            >
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

        <div class="product-actions" v-if="canUseMessageCenter">
          <el-button
            type="primary"
            plain
            icon="el-icon-chat-dot-round"
            @click="goToConsult"
          >
            咨询客服
          </el-button>
        </div>

        <!-- 竞拍区域 -->
        <div class="bid-section" v-if="canBid">
          <h3>参与竞拍</h3>
          <el-form
            :model="bidForm"
            :rules="bidRules"
            ref="bidForm"
            label-width="100px"
          >
            <el-form-item label="出价金额" prop="bidPrice">
              <el-input-number
                v-model="bidForm.bidPrice"
                :min="0"
                :precision="2"
                :step="goodsInfo.addPrice || 1"
                :controls="true"
                style="width: 200px"
              ></el-input-number>
              <span class="bid-tip"
                >最低出价：¥ {{ minBidPrice.toFixed(2) }}</span
              >
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                @click="handleSubmitBid"
                :loading="bidding"
              >
                立即出价
              </el-button>
              <el-button @click="handleQuickBid" :disabled="bidding">
                快速加价（+{{ goodsInfo.addPrice || 0 }}）
              </el-button>
              <el-button
                type="warning"
                @click="showProxyDialog"
                :disabled="bidding"
              >
                代理出价
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 竞拍记录列表 -->
      <div class="bid-records-section">
        <h3>竞拍记录</h3>
        <el-table
          :data="bidRecords"
          style="width: 100%"
          v-loading="recordsLoading"
        >
          <el-table-column
            prop="buyerName"
            label="出价人"
            width="150"
          ></el-table-column>
          <el-table-column prop="bidPrice" label="出价金额" width="150">
            <template slot-scope="scope">
              <span class="bid-price"
                >¥ {{ scope.row.bidPrice.toFixed(2) }}</span
              >
            </template>
          </el-table-column>
          <el-table-column prop="bidTime" label="出价时间" width="180">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.bidTime) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-tag
                v-if="scope.row.isHighest === 1"
                type="success"
                size="small"
                >当前最高</el-tag
              >
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

      <!-- 代理出价弹窗 -->
      <el-dialog
        title="代理出价"
        :visible.sync="proxyDialogVisible"
        width="480px"
        :close-on-click-modal="false"
      >
        <el-form
          :model="proxyForm"
          :rules="proxyRules"
          ref="proxyForm"
          label-width="120px"
        >
          <el-form-item label="当前最高出价">
            <span class="proxy-info-text"
              >¥ {{ (goodsInfo.currentHighestPrice || goodsInfo.basePrice || 0).toFixed(2) }}</span
            >
          </el-form-item>
          <el-form-item label="最低出价">
            <span class="proxy-info-text">¥ {{ minBidPrice.toFixed(2) }}</span>
          </el-form-item>
          <el-form-item label="最小加价幅度">
            <span class="proxy-info-text">¥ {{ (goodsInfo.addPrice || 0).toFixed(2) }}</span>
          </el-form-item>
          <el-form-item label="代理最高价" prop="agentMaxPrice">
            <el-input-number
              v-model="proxyForm.agentMaxPrice"
              :min="minBidPrice"
              :precision="2"
              :step="goodsInfo.addPrice || 1"
              :controls="true"
              style="width: 220px"
            ></el-input-number>
          </el-form-item>
        </el-form>
        <div class="proxy-tip">
          <i class="el-icon-info"></i>
          设置代理出价后，系统将自动以最小加价幅度为您出价，直至达到您设置的最高价。当有他人出价超过您的代理最高价时，代理出价失效。
        </div>
        <span slot="footer">
          <el-button @click="proxyDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="proxyLoading"
            @click="handleProxyBid"
          >
            确认设置
          </el-button>
        </span>
      </el-dialog>

      <el-dialog title="提交投诉" :visible.sync="complaintVisible" width="520px">
        <el-form :model="complaintForm" label-width="100px">
          <el-form-item label="投诉类型">
            <el-tag type="warning">{{
              complaintForm.complaintType === "bid" ? "竞价行为投诉" : "商品投诉"
            }}</el-tag>
          </el-form-item>
          <el-form-item label="投诉内容">
            <el-input
              v-model="complaintForm.complaintContent"
              type="textarea"
              :rows="5"
              maxlength="500"
              show-word-limit
              placeholder="请描述具体问题，如虚假描述、恶意抬价等"
            />
          </el-form-item>
        </el-form>
        <span slot="footer">
          <el-button @click="complaintVisible = false">取消</el-button>
          <el-button type="primary" :loading="complaintLoading" @click="submitGoodsComplaint">
            提交投诉
          </el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getGoodsDetail, getGoodsDetailAdmin } from "@/api/goods";
import { getCategoryListForHome } from "@/api/category";
import { submitBid, submitProxyBid, getRecordsByGoodsId } from "@/api/record";
import { submitComplaint } from "@/api/complaint";

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
      proxyDialogVisible: false,
      proxyLoading: false,
      proxyForm: {
        agentMaxPrice: 0,
      },
      proxyRules: {
        agentMaxPrice: [
          { required: true, message: "请输入代理最高价", trigger: "blur" },
        ],
      },
      complaintVisible: false,
      complaintLoading: false,
      complaintForm: {
        complaintType: "goods",
        complaintContent: "",
      },
    };
  },
  computed: {
    isOffShelf() {
      // 下架优先按 shelfStatus 判定；兼容旧数据 auditStatus=3 视为下架态
      return this.goodsInfo.shelfStatus === 0 || this.goodsInfo.auditStatus === 3;
    },
    // 是否可以竞拍
    canBid() {
      if (!this.goodsInfo.id) return false;
      const userInfo = localStorage.getItem("userInfo");
      if (!userInfo) return false;
      try {
        const user = JSON.parse(userInfo);
        // 不能竞拍自己发布的商品
        if (user.id === this.goodsInfo.sellerId) return false;
        // 买方/卖方都可竞拍（但不能竞拍自己发布的商品）
        if (!user.isBuyer && !user.isSeller) return false;
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
        return (
          parseFloat(this.goodsInfo.currentHighestPrice) +
          parseFloat(this.goodsInfo.addPrice || 0)
        );
      }
      return parseFloat(this.goodsInfo.basePrice || 0);
    },
    displayImageList() {
      const files = Array.isArray(this.goodsInfo.files)
        ? this.goodsInfo.files
        : [];
      const fromFiles = files
        .filter((f) => f && f.filePath && this.isImageFile(f))
        .map((f) => this.normalizeFileUrl(f.filePath));

      if (fromFiles.length > 0) {
        return fromFiles;
      }
      return this.parseLegacyMedia(this.goodsInfo.goodsImg, true);
    },
    displayVideoList() {
      const files = Array.isArray(this.goodsInfo.files)
        ? this.goodsInfo.files
        : [];
      const fromFiles = files
        .filter((f) => f && f.filePath && this.isVideoFile(f))
        .map((f) => this.normalizeFileUrl(f.filePath));

      if (fromFiles.length > 0) {
        return fromFiles;
      }
      return this.parseLegacyMedia(this.goodsInfo.goodsImg, false).filter((v) =>
        this.isVideoFile({ filePath: v }),
      );
    },
    currentMainImage() {
      if (!this.displayImageList.length) return "";
      if (this.selectedImageIndex >= this.displayImageList.length) {
        return this.displayImageList[0];
      }
      return this.displayImageList[this.selectedImageIndex];
    },
    // 是否可显示咨询客服（买方、卖方均可发起客服咨询；管理员等从消息中心使用管理沟通）
    canUseMessageCenter() {
      const userInfo = localStorage.getItem("userInfo");
      if (!userInfo) return false;
      try {
        const user = JSON.parse(userInfo);
        const roles = user.userRole
          ? String(user.userRole)
              .split(",")
              .map((r) => r.trim())
          : [];
        return roles.includes("1") || roles.includes("2");
      } catch (e) {
        return false;
      }
    },
    canComplain() {
      return this.canUseMessageCenter;
    },
  },
  mounted() {
    this.loadGoodsDetail();
    this.loadCategoryList();
  },
  methods: {
    handleBack() {
      // 优先返回上一页（依赖浏览器历史）；如果历史不足则兜底回到首页
      if (window.history && window.history.length > 1) {
        this.$router.back();
      } else {
        this.$router.push({ path: "/home" });
      }
    },
    // 加载商品详情
    async loadGoodsDetail() {
      const goodsId = this.$route.query.id;
      if (!goodsId) {
        this.$message.error("商品ID不能为空");
        return;
      }
      this.loading = true;
      try {
        const isAdminView = this.$route.query.from === "admin";
        const data = isAdminView
          ? await getGoodsDetailAdmin(goodsId)
          : await getGoodsDetail(goodsId);
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
          this.$message.error(
            `出价金额不能低于¥ ${this.minBidPrice.toFixed(2)}`,
          );
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
    // 显示代理出价弹窗
    showProxyDialog() {
      this.proxyForm.agentMaxPrice = this.minBidPrice;
      this.proxyDialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.proxyForm) {
          this.$refs.proxyForm.clearValidate();
        }
      });
    },
    // 提交代理出价
    handleProxyBid() {
      this.$refs.proxyForm.validate(async (valid) => {
        if (!valid) return;
        const goodsId = this.$route.query.id;
        this.proxyLoading = true;
        try {
          await submitProxyBid(goodsId, this.proxyForm.agentMaxPrice);
          this.$message.success("代理出价设置成功！系统将自动为您出价");
          this.proxyDialogVisible = false;
          await this.loadGoodsDetail();
          this.bidForm.bidPrice = this.minBidPrice;
        } catch (error) {
          this.$message.error(error.message || "代理出价设置失败");
        } finally {
          this.proxyLoading = false;
        }
      });
    },
    // 跳转咨询客服
    goToConsult() {
      const goodsId = this.$route.query.id || this.goodsInfo.id;
      if (!goodsId) {
        this.$message.warning("商品信息异常");
        return;
      }
      this.$router.push({ path: "/message", query: { goodsId } });
    },
    handleComplaintCommand(command) {
      this.complaintForm.complaintType = command === "bid" ? "bid" : "goods";
      this.complaintForm.complaintContent = "";
      this.complaintVisible = true;
    },
    async submitGoodsComplaint() {
      const content = (this.complaintForm.complaintContent || "").trim();
      if (!content) {
        this.$message.warning("请填写投诉内容");
        return;
      }
      if (!this.goodsInfo.id) {
        this.$message.warning("商品信息异常");
        return;
      }
      this.complaintLoading = true;
      try {
        await submitComplaint({
          complaintType: this.complaintForm.complaintType,
          targetId: this.goodsInfo.id,
          relatedGoodsId: this.goodsInfo.id,
          complaintContent: content,
        });
        this.$message.success("投诉提交成功");
        this.complaintVisible = false;
      } catch (e) {
        this.$message.error(e.message || "投诉提交失败");
      } finally {
        this.complaintLoading = false;
      }
    },
    // 加载分类列表（使用公开接口，普通用户无需管理员权限）
    async loadCategoryList() {
      try {
        const data = await getCategoryListForHome();
        this.categoryList = data || [];
        // 加载分类名称
        this.loadCategoryNames();
      } catch (error) {}
    },
    // 根据categoryId加载分类名称
    loadCategoryNames() {
      if (!this.goodsInfo.categoryId || !this.categoryList.length) {
        this.categoryNames = [];
        return;
      }
      // categoryId可能是逗号分隔的多个ID
      const categoryIds = this.goodsInfo.categoryId
        .split(",")
        .map((id) => id.trim());
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
    "$route.query.from"() {
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
  position: relative;
}

.top-actions {
  position: absolute;
  top: 10px;
  right: 12px;
}

.more-btn {
  font-size: 22px;
  color: #606266;
}

.image-gallery {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.main-image {
  flex: 1;
  position: relative;
}

.main-img {
  width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.image-gallery.is-off-shelf .main-img,
.image-gallery.is-off-shelf .thumbnail {
  filter: grayscale(100%);
}

.off-shelf-mask {
  position: absolute;
  inset: 0;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.off-shelf-text {
  color: #fff;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 2px;
  transform: rotate(-20deg);
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.45);
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

.product-actions {
  margin-bottom: 20px;
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

.proxy-info-text {
  font-size: 16px;
  color: #e74c3c;
  font-weight: bold;
}

.proxy-tip {
  padding: 12px;
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 4px;
  color: #e6a23c;
  font-size: 13px;
  line-height: 1.6;
  margin-top: 10px;
}

.proxy-tip i {
  margin-right: 4px;
}
</style>
