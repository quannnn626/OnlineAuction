<template>
  <div class="special-page">
    <div class="page-header">
      <div class="title">
        <i class="el-icon-collection-tag"></i>
        <span>拍卖专场</span>
      </div>
      <div class="hint">选择专场后查看该专场内的拍品</div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="8" :md="7" :lg="6">
        <div class="panel">
          <div class="panel-title">专场列表</div>
          <el-skeleton v-if="specialLoading" :rows="6" animated />
          <el-empty v-else-if="specials.length === 0" description="暂无专场"></el-empty>
          <el-menu
            v-else
            class="special-menu"
            :default-active="String(activeSpecialId || '')"
            @select="handleSpecialSelect"
          >
            <el-menu-item v-for="s in specials" :key="s.id" :index="String(s.id)">
              <div class="menu-item">
                <div class="name">{{ s.specialName }}</div>
                <div class="desc">{{ s.specialDesc || "暂无描述" }}</div>
              </div>
            </el-menu-item>
          </el-menu>
        </div>
      </el-col>

      <el-col :xs="24" :sm="16" :md="17" :lg="18">
        <div class="panel">
          <div class="panel-title">
            <span>专场拍品</span>
            <span class="panel-sub" v-if="currentSpecial">
              {{ currentSpecial.specialName }}
            </span>
          </div>

          <div v-loading="goodsLoading" class="goods-wrap">
            <div v-if="goodsList.length > 0" class="goods-list">
              <div
                v-for="g in goodsList"
                :key="g.goodsId"
                class="goods-item"
                @click="goGoodsDetail(g)"
              >
                <div class="goods-item-content">
                  <div class="goods-item-info">
                    <div class="goods-item-title">{{ g.goodsName }}</div>
                    <div class="goods-item-desc">{{ g.goodsDesc || "暂无描述" }}</div>
                    <div class="goods-item-price">
                      <span class="goods-item-price-label">起拍价：</span>
                      ¥{{ g.basePrice }}
                      <span v-if="g.currentHighestPrice != null" class="highest">
                        当前最高：¥{{ g.currentHighestPrice }}
                      </span>
                    </div>
                  </div>
                  <div class="goods-item-image">
                    <img
                      :src="getGoodsImageUrl(g)"
                      :alt="g.goodsName"
                      @error="handleImageError"
                    />
                  </div>
                </div>
                <div class="goods-item-footer">
                  <span>
                    <i class="el-icon-time"></i>
                    {{ formatTime(g.startTime) }} - {{ formatTime(g.endTime) }}
                  </span>
                  <span class="goods-status" :class="getGoodsStatusClass(g.goodsStatus)">
                    {{ getGoodsStatusText(g.goodsStatus) }}
                  </span>
                </div>
              </div>
            </div>
            <el-empty v-else-if="!goodsLoading" description="该专场暂无拍品"></el-empty>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getSpecialList, getSpecialGoods } from "@/api/special";

export default {
  name: "Special",
  data() {
    return {
      specialLoading: false,
      goodsLoading: false,
      specials: [],
      activeSpecialId: null,
      goodsList: [],
    };
  },
  computed: {
    currentSpecial() {
      return this.specials.find((s) => s.id === this.activeSpecialId) || null;
    },
  },
  mounted() {
    this.loadSpecials();
  },
  methods: {
    async loadSpecials() {
      this.specialLoading = true;
      try {
        const list = await getSpecialList();
        this.specials = Array.isArray(list) ? list : [];
        const qid = this.$route.query && this.$route.query.id ? parseInt(this.$route.query.id, 10) : null;
        const firstId = this.specials[0] ? this.specials[0].id : null;
        const selectId = qid && this.specials.some((s) => s.id === qid) ? qid : firstId;
        this.activeSpecialId = selectId;
        if (selectId) {
          this.loadGoods(selectId);
        } else {
          this.goodsList = [];
        }
      } catch (e) {
        this.specials = [];
        this.goodsList = [];
        this.$message.error(e.message || "加载专场失败");
      } finally {
        this.specialLoading = false;
      }
    },
    handleSpecialSelect(index) {
      const id = parseInt(index, 10);
      if (!id) return;
      this.activeSpecialId = id;
      this.$router.replace({ path: "/special", query: { id } }).catch(() => {});
      this.loadGoods(id);
    },
    async loadGoods(specialId) {
      this.goodsLoading = true;
      try {
        const list = await getSpecialGoods(specialId);
        this.goodsList = Array.isArray(list) ? list : [];
      } catch (e) {
        this.goodsList = [];
        this.$message.error(e.message || "加载专场拍品失败");
      } finally {
        this.goodsLoading = false;
      }
    },
    goGoodsDetail(g) {
      const id = g.goodsId || g.id;
      if (!id) return;
      this.$router.push({ path: "/goods-detail", query: { id } });
    },
    getGoodsImageUrl(goods) {
      const files = goods.files || [];
      const firstImg = files.find((f) => f && f.filePath && /\.(jpg|jpeg|png|gif|webp|bmp|svg)/i.test(f.filePath));
      if (firstImg && firstImg.filePath) {
        const p = firstImg.filePath;
        return /^https?:\/\//i.test(p) ? p : (p.startsWith("/") ? p : "/" + p);
      }
      if (goods.goodsImg) {
        const imgs = String(goods.goodsImg).split(",");
        const url = imgs[0] && imgs[0].trim();
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
      const statusMap = { 0: "未开始", 1: "竞拍中", 2: "已成交", 3: "已流拍" };
      return statusMap[status] || "未知";
    },
    getGoodsStatusClass(status) {
      const classMap = { 0: "goods-status-upcoming", 1: "goods-status-auctioning", 2: "goods-status-ended", 3: "goods-status-ended" };
      return classMap[status] || "";
    },
  },
};
</script>

<style scoped>
.special-page {
  max-width: 1400px;
  margin: 0 auto;
}
.page-header {
  background: #fff;
  border-radius: 8px;
  padding: 18px 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}
.title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}
.hint {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
}
.panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  padding: 14px;
  margin-bottom: 16px;
}
.panel-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #0f172a;
}
.panel-sub {
  color: #64748b;
  font-weight: 400;
  font-size: 13px;
}
.special-menu {
  border-right: none;
}
.menu-item .name {
  font-size: 14px;
  color: #0f172a;
  line-height: 1.3;
}
.menu-item .desc {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.2;
  margin-top: 2px;
}
.goods-wrap {
  min-height: 220px;
}
.goods-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
@media (max-width: 992px) {
  .goods-list {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}
.goods-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.goods-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(2, 6, 23, 0.08);
}
.goods-item-content {
  display: flex;
  gap: 12px;
}
.goods-item-info {
  flex: 1;
  min-width: 0;
}
.goods-item-title {
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.goods-item-desc {
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
  height: 34px;
  overflow: hidden;
}
.goods-item-price {
  margin-top: 10px;
  color: #0f172a;
  font-size: 14px;
}
.goods-item-price-label {
  color: #64748b;
  margin-right: 4px;
}
.highest {
  margin-left: 8px;
  color: #ef4444;
  font-size: 12px;
}
.goods-item-image img {
  width: 120px;
  height: 88px;
  object-fit: cover;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
}
.goods-item-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  color: #64748b;
  font-size: 12px;
}
.goods-status {
  padding: 2px 8px;
  border-radius: 10px;
  color: #fff;
}
.goods-status-upcoming {
  background: #3b82f6;
}
.goods-status-auctioning {
  background: #22c55e;
}
.goods-status-ended {
  background: #64748b;
}
</style>

