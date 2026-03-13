<template>
  <div class="bid-history-page">
    <div class="page-header">
      <h2>历史竞拍</h2>
    </div>

    <div class="filter-section">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-input
            v-model="keyword"
            placeholder="按商品名称搜索"
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
          <el-button type="info" icon="el-icon-refresh" @click="handleReset"
            >重置</el-button
          >
        </el-col>
      </el-row>
    </div>

    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="goodsId" label="商品ID" width="90" />
        <el-table-column prop="goodsName" label="商品名称" min-width="220" />
        <el-table-column prop="myBidCount" label="我的出价次数" width="120" />
        <el-table-column prop="myHighestBid" label="我的最高出价" width="140">
          <template slot-scope="scope">
            <span class="price">¥{{ scope.row.myHighestBid || "0.00" }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="currentHighestPrice"
          label="当前最高价"
          width="140"
        >
          <template slot-scope="scope">
            <span class="price"
              >¥{{ scope.row.currentHighestPrice || "0.00" }}</span
            >
          </template>
        </el-table-column>
        <el-table-column prop="latestBidTime" label="最近出价时间" width="180">
          <template slot-scope="scope">{{
            formatTime(scope.row.latestBidTime)
          }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="openDetail(scope.row)"
              >查看详情</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-section">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
      />
    </div>

    <el-dialog
      :title="`我的竞价详情 - ${detailGoodsName || ''}`"
      :visible.sync="detailVisible"
      width="860px"
    >
      <el-table
        v-loading="detailLoading"
        :data="detailData"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="buyerName" label="出价人" width="150" />
        <el-table-column prop="bidPrice" label="出价金额" width="150">
          <template slot-scope="scope">
            <span class="price">¥{{ scope.row.bidPrice || "0.00" }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bidTime" label="出价时间" width="180">
          <template slot-scope="scope">{{
            formatTime(scope.row.bidTime)
          }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isHighest === 1" type="success" size="small"
              >当前最高</el-tag
            >
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-section">
        <el-pagination
          @size-change="handleDetailSizeChange"
          @current-change="handleDetailCurrentChange"
          :current-page="detailPagination.current"
          :page-sizes="[10, 20, 50]"
          :page-size="detailPagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="detailPagination.total"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyBidGoodsPage, getMyBidRecordsByGoodsPage } from "@/api/record";

export default {
  name: "BidHistory",
  data() {
    return {
      loading: false,
      keyword: "",
      tableData: [],
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
      detailVisible: false,
      detailLoading: false,
      detailGoodsId: null,
      detailGoodsName: "",
      detailData: [],
      detailPagination: {
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
          keyword: this.keyword || undefined,
        };
        const result = await getMyBidGoodsPage(params);
        this.tableData = result.list || [];
        this.pagination.total = result.total || 0;
      } catch (e) {
        this.$message.error("加载历史竞拍失败");
      } finally {
        this.loading = false;
      }
    },
    async loadDetailData() {
      if (!this.detailGoodsId) return;
      this.detailLoading = true;
      try {
        const params = {
          current: this.detailPagination.current,
          size: this.detailPagination.size,
        };
        const result = await getMyBidRecordsByGoodsPage(
          this.detailGoodsId,
          params,
        );
        this.detailData = result.list || [];
        this.detailPagination.total = result.total || 0;
      } catch (e) {
        this.$message.error("加载竞价详情失败");
      } finally {
        this.detailLoading = false;
      }
    },
    handleSearch() {
      this.pagination.current = 1;
      this.loadData();
    },
    handleReset() {
      this.keyword = "";
      this.pagination.current = 1;
      this.loadData();
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
    openDetail(row) {
      this.detailGoodsId = row.goodsId;
      this.detailGoodsName = row.goodsName;
      this.detailPagination.current = 1;
      this.detailVisible = true;
      this.loadDetailData();
    },
    handleDetailSizeChange(size) {
      this.detailPagination.size = size;
      this.detailPagination.current = 1;
      this.loadDetailData();
    },
    handleDetailCurrentChange(current) {
      this.detailPagination.current = current;
      this.loadDetailData();
    },
    formatTime(timeStr) {
      if (!timeStr) return "-";
      const date = new Date(timeStr);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
  },
};
</script>

<style scoped>
.bid-history-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header,
.filter-section,
.table-section,
.pagination-section {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}

.page-header {
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
}

.filter-section,
.pagination-section {
  padding: 16px 20px;
}

.price {
  color: #e74c3c;
  font-weight: bold;
}
</style>
