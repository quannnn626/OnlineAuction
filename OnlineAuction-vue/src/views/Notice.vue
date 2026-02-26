<template>
  <div class="notice-page">
    <div class="page-header">
      <h2><i class="el-icon-bell"></i> 竞拍公告</h2>
    </div>

    <!-- 搜索 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索公告标题或内容"
            clearable
            prefix-icon="el-icon-search"
            @keyup.enter.native="handleSearch"
            style="width: 280px"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">
            搜索
          </el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 无权限提示 -->
    <div v-if="noPermission" class="no-permission">
      <el-alert
        title="无权限"
        type="warning"
        description="您没有查看竞拍公告的权限，仅买方、卖方和超级管理员可查看。"
        show-icon
      >
      </el-alert>
    </div>

    <!-- 公告列表 -->
    <div v-else class="notice-list" v-loading="loading">
      <el-card
        v-for="item in noticeList"
        :key="item.id"
        class="notice-card"
        shadow="hover"
        @click.native="handleViewDetail(item)"
      >
        <div class="notice-item">
          <div class="notice-top">
            <el-tag v-if="item.isTop === 1" type="danger" size="mini">
              置顶
            </el-tag>
            <span class="notice-title">{{ item.noticeTitle }}</span>
          </div>
          <div class="notice-time">
            {{ formatDateTime(item.publishTime || item.createTime) }}
          </div>
          <div class="notice-summary" v-if="item.noticeContent">
            {{ getSummary(item.noticeContent) }}
          </div>
        </div>
      </el-card>

      <el-empty
        v-if="!loading && noticeList.length === 0"
        description="暂无公告"
      ></el-empty>
    </div>

    <!-- 分页 -->
    <div
      class="pagination-section"
      v-if="!noPermission && noticeList.length > 0"
    >
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next"
        :total="pagination.total"
        background
      >
      </el-pagination>
    </div>

    <!-- 公告详情对话框 -->
    <el-dialog
      title="公告详情"
      :visible.sync="detailVisible"
      width="700px"
      class="notice-detail-dialog"
    >
      <div v-if="currentNotice" class="notice-detail">
        <div class="detail-header">
          <h3>{{ currentNotice.noticeTitle }}</h3>
          <div class="detail-meta">
            <el-tag v-if="currentNotice.isTop === 1" type="danger" size="mini">
              置顶
            </el-tag>
            <span class="publish-time">
              {{ formatDateTime(currentNotice.publishTime || currentNotice.createTime) }}
            </span>
          </div>
        </div>
        <div class="detail-content" v-html="formatContent(currentNotice.noticeContent)"></div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPublishedNoticePage, getNoticeDetail } from "@/api/notice";

export default {
  name: "Notice",
  data() {
    return {
      loading: false,
      noPermission: false,
      noticeList: [],
      searchForm: { keyword: "" },
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
      detailVisible: false,
      currentNotice: null,
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      this.noPermission = false;
      try {
        const res = await getPublishedNoticePage({
          current: this.pagination.current,
          size: this.pagination.size,
          keyword: this.searchForm.keyword || undefined,
        });
        if (res && res.list) {
          this.noticeList = res.list;
          this.pagination.total = res.total || 0;
        } else {
          this.noticeList = [];
          this.pagination.total = 0;
        }
      } catch (error) {
        const msg = error.message || "";
        if (msg.indexOf("无权限") !== -1 || msg.indexOf("权限") !== -1) {
          this.noPermission = true;
          this.noticeList = [];
        }
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.pagination.current = 1;
      this.loadData();
    },
    handleReset() {
      this.searchForm.keyword = "";
      this.pagination.current = 1;
      this.loadData();
    },
    handleSizeChange(val) {
      this.pagination.size = val;
      this.pagination.current = 1;
      this.loadData();
    },
    handleCurrentChange(val) {
      this.pagination.current = val;
      this.loadData();
    },
    async handleViewDetail(item) {
      try {
        const res = await getNoticeDetail(item.id);
        this.currentNotice = res;
        this.detailVisible = true;
      } catch (error) {
        this.$message.error(error.message || "获取详情失败");
      }
    },
    getSummary(content, maxLen = 80) {
      if (!content) return "";
      const text = content.replace(/<[^>]+>/g, "").trim();
      return text.length > maxLen ? text.substring(0, maxLen) + "…" : text;
    },
    formatContent(content) {
      if (!content) return "";
      return content.replace(/\n/g, "<br/>");
    },
    formatDateTime(val) {
      if (!val) return "-";
      const d = new Date(val);
      if (isNaN(d.getTime())) return val;
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      const h = String(d.getHours()).padStart(2, "0");
      const min = String(d.getMinutes()).padStart(2, "0");
      return `${y}-${m}-${day} ${h}:${min}`;
    },
  },
};
</script>

<style scoped>
.notice-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.page-header h2 i {
  margin-right: 8px;
}

.search-section {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.no-permission {
  padding: 40px 0;
}

.notice-list {
  min-height: 200px;
}

.notice-card {
  margin-bottom: 12px;
  cursor: pointer;
}

.notice-card:hover {
  border-color: #409eff;
}

.notice-item {
  padding: 4px 0;
}

.notice-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.notice-title {
  font-weight: 500;
  font-size: 16px;
  color: #303133;
}

.notice-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.notice-summary {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.pagination-section {
  margin-top: 20px;
  text-align: center;
}

/* 详情对话框 */
.notice-detail {
  padding: 0 10px;
}

.detail-header h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #303133;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.publish-time {
  font-size: 13px;
  color: #909399;
}

.detail-content {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}
</style>
