<template>
  <div class="goods-management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>拍品列表</h2>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品名称"
            clearable
            @keyup.enter.native="handleSearch"
          >
            <el-button
              slot="append"
              icon="el-icon-search"
              @click="handleSearch"
            ></el-button>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="categoryFilter"
            placeholder="商品分类"
            clearable
            filterable
            @change="handleFilter"
          >
            <el-option label="全部分类" value=""></el-option>
            <el-option
              v-for="cat in categoryList"
              :key="cat.id"
              :label="cat.categoryName"
              :value="String(cat.id)"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="statusFilter"
            placeholder="选择状态"
            clearable
            @change="handleFilter"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="未开始" value="0"></el-option>
            <el-option label="竞拍中" value="1"></el-option>
            <el-option label="已成交" value="2"></el-option>
            <el-option label="已流拍" value="3"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="info" icon="el-icon-refresh" @click="handleReset"
            >重置</el-button
          >
        </el-col>
      </el-row>
    </div>

    <!-- 商品表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="goodsList"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="商品图片" width="120">
          <template slot-scope="scope">
            <el-image
              :src="getGoodsImage(scope.row.goodsImg)"
              :preview-src-list="[getGoodsImage(scope.row.goodsImg)]"
              style="width: 80px; height: 80px; object-fit: cover"
              fit="cover"
            >
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="goodsName" label="商品名称" min-width="200">
          <template slot-scope="scope">
            <div class="goods-name">{{ scope.row.goodsName }}</div>
            <div class="goods-desc">
              {{ scope.row.goodsDesc || "暂无描述" }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="basePrice" label="起拍价" width="120">
          <template slot-scope="scope">
            <span class="price">¥{{ scope.row.basePrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="goodsStatus" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.goodsStatus)">
              {{ getStatusText(scope.row.goodsStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="拍卖时间" min-width="180">
          <template slot-scope="scope">
            <div>{{ formatTime(scope.row.startTime) }}</div>
            <div>至 {{ formatTime(scope.row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-view"
              @click="handleView(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { getGoodsList } from "@/api/goods";
import { getCategoryListForHome } from "@/api/category";
import { getRecommendList } from "@/api/recommend";

export default {
  name: "Goods",
  data() {
    return {
      goodsList: [],
      loading: false,
      categoryList: [],
      searchKeyword: "",
      statusFilter: "",
      categoryFilter: "",
      categoryRecommendOrderIds: [],
      categoryRecommendOrderForCategoryId: null,
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
    };
  },
  created() {
    // 支持从首页分类跳转带入的 categoryId
    const qid = this.$route && this.$route.query ? this.$route.query.categoryId : null;
    if (qid !== null && qid !== undefined && String(qid).trim() !== "") {
      this.categoryFilter = String(qid);
    }
    this.loadCategories();
    this.loadData();
  },
  methods: {
    async loadCategories() {
      try {
        const res = await getCategoryListForHome();
        this.categoryList = Array.isArray(res) ? res : [];
      } catch (e) {
        this.categoryList = [];
      }
    },
    async loadData() {
      this.loading = true;
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          keyword: this.searchKeyword,
          status: this.statusFilter,
          categoryId: this.categoryFilter || undefined,
        };
        const result = await getGoodsList(params);
        // PageInfo结构：list(实际是records)和total
        let list = result.list || result.records || [];
        // 分类推荐排序：把推荐商品排到最前面（其余商品仍保留），展示“分类下所有商品”
        if (this.categoryFilter) {
          await this.ensureCategoryRecommendOrder();
          if (this.categoryRecommendOrderIds && this.categoryRecommendOrderIds.length > 0) {
            const orderIndex = new Map(this.categoryRecommendOrderIds.map((id, i) => [id, i]));
            const recommended = list
              .filter((g) => orderIndex.has(g.id))
              .sort((a, b) => orderIndex.get(a.id) - orderIndex.get(b.id));
            const remaining = list.filter((g) => !orderIndex.has(g.id));
            // 非推荐商品按点击量降序，推荐过的商品整体排在点击量前面
            const v = (g) => g.viewCount ?? g.view_count ?? 0;
            remaining.sort((a, b) => v(b) - v(a));
            list = [...recommended, ...remaining];
          } else {
            // 无推荐位时，整表按点击量排序
            const v = (g) => g.viewCount ?? g.view_count ?? 0;
            list = [...list].sort((a, b) => v(b) - v(a));
          }
        }
        this.goodsList = list;
        this.pagination.total = result.total || 0;
      } catch (error) {
        this.$message.error("加载失败，请重试");
      } finally {
        this.loading = false;
      }
    },
    async ensureCategoryRecommendOrder() {
      const categoryId = this.categoryFilter;
      if (!categoryId) return;
      const cid = parseInt(categoryId, 10);
      if (!cid || this.categoryRecommendOrderForCategoryId === cid) return;
      this.categoryRecommendOrderForCategoryId = cid;
      try {
        const recs = await getRecommendList("category", cid);
        const rows = Array.isArray(recs) ? recs : [];
        this.categoryRecommendOrderIds = rows.map((r) => r.goodsId).filter((id) => id != null);
      } catch (e) {
        this.categoryRecommendOrderIds = [];
      }
    },
    handleSearch() {
      this.pagination.current = 1;
      this.loadData();
    },
    handleFilter() {
      this.pagination.current = 1;
      this.categoryRecommendOrderForCategoryId = null;
      this.loadData();
    },
    handleReset() {
      this.searchKeyword = "";
      this.statusFilter = "";
      this.categoryFilter = "";
      this.pagination.current = 1;
      this.categoryRecommendOrderForCategoryId = null;
      this.categoryRecommendOrderIds = [];
      this.loadData();
    },
    handleView(goods) {
      this.$router.push({ path: "/goods-detail", query: { id: goods.id } });
    },
    handleSizeChange(size) {
      this.pagination.size = size;
      this.pagination.current = 1;
      this.loadData();
    },
    handleCurrentChange(current) {
      this.pagination.current = current;
      this.loadData();
    },
    getGoodsImage(goodsImg) {
      if (!goodsImg) {
        return "/images/no-image.svg";
      }
      const images = goodsImg.split(",");
      return images[0].trim();
    },
    getStatusType(status) {
      const typeMap = {
        0: "info",
        1: "success",
        2: "warning",
        3: "danger",
      };
      return typeMap[status] || "info";
    },
    getStatusText(status) {
      const textMap = {
        0: "未开始",
        1: "竞拍中",
        2: "已成交",
        3: "已流拍",
      };
      return textMap[status] || "未知";
    },
    formatTime(timeStr) {
      if (!timeStr) return "";
      const date = new Date(timeStr);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      const hours = date.getHours().toString().padStart(2, "0");
      const minutes = date.getMinutes().toString().padStart(2, "0");
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
  },
};
</script>

<style scoped>
.goods-management-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.filter-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-section {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.goods-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.goods-desc {
  color: #666;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  color: #e74c3c;
  font-weight: bold;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 80px;
  height: 80px;
  background: #f5f5f5;
  color: #909399;
  font-size: 24px;
}

.recommend-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.recommend-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.recommend-list {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.recommend-item {
  width: 220px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #f8fafc;
  cursor: pointer;
  padding: 12px;
  display: flex;
  gap: 10px;
  align-items: center;
  transition: all 0.15s ease;
}

.recommend-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(2, 6, 23, 0.08);
}

.recommend-img img {
  width: 90px;
  height: 70px;
  object-fit: cover;
  border-radius: 8px;
  background: #fff;
}

.recommend-info {
  flex: 1;
  min-width: 0;
}

.recommend-name {
  font-weight: 600;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 6px;
}

.recommend-price {
  color: #e74c3c;
  font-weight: 600;
  margin-bottom: 6px;
}

.recommend-status {
  margin-top: 2px;
}
</style>
