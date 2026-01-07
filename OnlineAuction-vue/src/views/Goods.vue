<template>
  <div class="goods-management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>拍卖商品</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          上架商品
        </el-button>
        <el-button type="info" icon="el-icon-user" @click="handleMyGoods">
          我的商品
        </el-button>
      </div>
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

export default {
  name: "Goods",
  data() {
    return {
      goodsList: [],
      loading: false,
      searchKeyword: "",
      statusFilter: "",
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
    };
  },
  created() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          keyword: this.searchKeyword,
          status: this.statusFilter,
        };
        const result = await getGoodsList(params);
        this.goodsList = result.list || [];
        this.pagination.total = result.total || 0;
      } catch (error) {
        console.error("加载商品列表失败:", error);
        this.$message.error("加载失败，请重试");
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.pagination.current = 1;
      this.loadData();
    },
    handleFilter() {
      this.pagination.current = 1;
      this.loadData();
    },
    handleReset() {
      this.searchKeyword = "";
      this.statusFilter = "";
      this.pagination.current = 1;
      this.loadData();
    },
    handleAdd() {
      this.$message.info("上架商品功能开发中...");
      // TODO: 跳转到上架商品页面
    },
    handleView(goods) {
      this.$router.push({ path: "/goods-detail", query: { id: goods.id } });
    },
    handleMyGoods() {
      this.$router.push("/my-goods");
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
</style>
