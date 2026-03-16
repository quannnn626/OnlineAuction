<template>
  <div class="admin-seller-audit-page">
    <div class="page-header">
      <h2>卖家资质审核</h2>
    </div>

    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.userName"
            placeholder="搜索用户名"
            clearable
            style="width: 180px"
          ></el-input>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select
            v-model="searchForm.sellerAuditStatus"
            placeholder="全部"
            clearable
            style="width: 140px"
          >
            <el-option label="待审核" :value="1"></el-option>
            <el-option label="审核通过" :value="2"></el-option>
            <el-option label="审核驳回" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-section">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="id" label="用户ID" width="90"></el-table-column>
        <el-table-column prop="userName" label="用户名" width="150"></el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="120"></el-table-column>
        <el-table-column prop="phone" label="手机号" width="130"></el-table-column>
        <el-table-column prop="sellerAuditStatus" label="资质状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.sellerAuditStatus)">
              {{ getStatusText(scope.row.sellerAuditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sellerAuditApplyTime" label="申请时间" width="170">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.sellerAuditApplyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="sellerAuditTime" label="审核时间" width="170">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.sellerAuditTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleView(scope.row)">查看详情</el-button>
            <el-button
              v-if="scope.row.sellerAuditStatus === 1"
              size="mini"
              type="primary"
              @click="handleAudit(scope.row)"
            >
              审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-section">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next"
        :total="pagination.total"
        background
      ></el-pagination>
    </div>

    <!-- 详情&审核弹窗 -->
    <el-dialog
      :title="auditDialogTitle"
      :visible.sync="auditDialogVisible"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-descriptions title="用户信息" :column="2" size="small" border>
        <el-descriptions-item label="用户ID">{{ auditData.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ auditData.userName }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">
          {{ auditData.realName || "未填写" }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">{{ auditData.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ auditData.email || "未填写" }}
        </el-descriptions-item>
      </el-descriptions>

      <el-descriptions title="资质信息" :column="2" size="small" border style="margin-top: 16px;">
        <el-descriptions-item label="当前状态">
          {{ getStatusText(auditData.sellerAuditStatus) }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(auditData.sellerAuditApplyTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="审核时间">
          {{ formatDateTime(auditData.sellerAuditTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="驳回原因">
          <span v-if="auditData.sellerAuditStatus === 3">
            {{ auditData.sellerAuditRemark || "未填写" }}
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="files-section" v-if="fileList && fileList.length">
        <h4>资质材料</h4>
        <div class="files-grid">
          <div v-for="f in fileList" :key="f.id" class="file-item">
            <template v-if="f.fileType === 'image'">
              <el-image
                :src="getFileUrl(f.filePath)"
                :preview-src-list="[getFileUrl(f.filePath)]"
                fit="cover"
                style="width: 120px; height: 80px;"
              ></el-image>
              <div class="file-name">{{ f.fileName }}</div>
            </template>
            <template v-else-if="f.fileType === 'video'">
              <video
                :src="getFileUrl(f.filePath)"
                controls
                style="width: 180px; height: 100px;"
              ></video>
              <div class="file-name">{{ f.fileName }}</div>
            </template>
            <template v-else>
              <a :href="getFileUrl(f.filePath)" target="_blank" rel="noopener">
                {{ f.fileName || f.filePath }}
              </a>
            </template>
          </div>
        </div>
      </div>
      <el-empty v-else description="未上传资质材料" style="margin-top: 16px;" />

      <div v-if="auditData.sellerAuditStatus === 1" style="margin-top: 20px;">
        <el-form :model="auditForm" label-width="100px">
          <el-form-item label="审核结果">
            <el-radio-group v-model="auditForm.auditStatus">
              <el-radio :label="2">审核通过</el-radio>
              <el-radio :label="3">审核驳回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="驳回原因" v-if="auditForm.auditStatus === 3">
            <el-input
              type="textarea"
              v-model="auditForm.auditRemark"
              :rows="3"
              maxlength="200"
              show-word-limit
              placeholder="请填写驳回原因"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="auditDialogVisible = false">关 闭</el-button>
        <el-button
          v-if="auditData.sellerAuditStatus === 1"
          type="primary"
          @click="submitAudit"
          :loading="auditSubmitting"
        >
          提交审核
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getSellerAuditPage, auditSeller } from "@/api/user";
import { getFilesByIds } from "@/api/file";

export default {
  name: "AdminSellerAudit",
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: {
        userName: "",
        sellerAuditStatus: undefined,
      },
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
      auditDialogVisible: false,
      auditDialogTitle: "卖家资质详情",
      auditData: {},
      auditForm: {
        auditStatus: 2,
        auditRemark: "",
      },
      auditSubmitting: false,
      fileList: [],
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          userName: this.searchForm.userName || undefined,
          sellerAuditStatus: this.searchForm.sellerAuditStatus,
        };
        const res = await getSellerAuditPage(params);
        this.tableData = res && res.list ? res.list : [];
        this.pagination.total = res && res.total ? res.total : 0;
      } catch (e) {
        this.tableData = [];
        this.pagination.total = 0;
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.pagination.current = 1;
      this.loadData();
    },
    handleReset() {
      this.searchForm = { userName: "", sellerAuditStatus: undefined };
      this.pagination.current = 1;
      this.loadData();
    },
    handleSizeChange(v) {
      this.pagination.size = v;
      this.pagination.current = 1;
      this.loadData();
    },
    handleCurrentChange(v) {
      this.pagination.current = v;
      this.loadData();
    },
    getStatusText(s) {
      if (s === 1) return "待审核";
      if (s === 2) return "审核通过";
      if (s === 3) return "审核驳回";
      return "-";
    },
    getStatusTagType(s) {
      if (s === 1) return "warning";
      if (s === 2) return "success";
      if (s === 3) return "danger";
      return "info";
    },
    formatDateTime(val) {
      if (!val) return "-";
      const d = new Date(val);
      return isNaN(d.getTime()) ? val : d.toLocaleString("zh-CN", { hour12: false });
    },
    async handleView(row) {
      this.auditDialogTitle = "卖家资质详情";
      this.auditData = { ...row };
      this.auditForm = {
        auditStatus: 2,
        auditRemark: "",
      };
      await this.loadFiles(row);
      this.auditDialogVisible = true;
    },
    async handleAudit(row) {
      this.auditDialogTitle = "卖家资质审核";
      this.auditData = { ...row };
      this.auditForm = {
        auditStatus: 2,
        auditRemark: "",
      };
      await this.loadFiles(row);
      this.auditDialogVisible = true;
    },
    async loadFiles(row) {
      this.fileList = [];
      const ids = row.sellerCertificateFiles;
      if (!ids) return;
      try {
        const res = await getFilesByIds(ids);
        this.fileList = res || [];
      } catch (e) {
        this.fileList = [];
      }
    },
    getFileUrl(path) {
      if (!path) return "";
      if (path.startsWith("http")) return path;
      // 静态资源通过 /upload 代理
      return path;
    },
    async submitAudit() {
      if (this.auditData.sellerAuditStatus !== 1) {
        this.auditDialogVisible = false;
        return;
      }
      if (this.auditForm.auditStatus === 3 && !this.auditForm.auditRemark.trim()) {
        this.$message.warning("审核驳回时必须填写驳回原因");
        return;
      }
      this.auditSubmitting = true;
      try {
        await auditSeller(this.auditData.id, this.auditForm.auditStatus, this.auditForm.auditRemark);
        this.$message.success("审核成功");
        this.auditDialogVisible = false;
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "审核失败");
      } finally {
        this.auditSubmitting = false;
      }
    },
  },
};
</script>

<style scoped>
.admin-seller-audit-page {
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
}
.filter-section {
  margin-bottom: 16px;
}
.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}
.pagination-section {
  margin-top: 20px;
  text-align: right;
}
.files-section {
  margin-top: 20px;
}
.files-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.file-item {
  text-align: center;
}
.file-name {
  margin-top: 4px;
  font-size: 12px;
  color: #606266;
  max-width: 180px;
  word-break: break-all;
}
</style>

