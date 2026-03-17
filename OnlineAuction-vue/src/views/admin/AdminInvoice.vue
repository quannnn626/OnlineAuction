<template>
  <div class="admin-invoice-page">
    <div class="page-header">
      <h2>发票管理</h2>
    </div>
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 110px" @change="loadData">
            <el-option label="待处理" :value="0"></el-option>
            <el-option label="已开票" :value="1"></el-option>
            <el-option label="已驳回" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="用户ID" clearable style="width: 100px"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column prop="userId" label="用户ID" width="80"></el-table-column>
      <el-table-column prop="invoiceTitle" label="发票抬头" min-width="140" show-overflow-tooltip></el-table-column>
      <el-table-column prop="taxNo" label="税号" width="120" show-overflow-tooltip></el-table-column>
      <el-table-column prop="amount" label="金额" width="90">
        <template slot-scope="scope">¥{{ scope.row.amount }}</template>
      </el-table-column>
      <el-table-column prop="invoiceType" label="类型" width="80">
        <template slot-scope="scope">{{ scope.row.invoiceType === 2 ? '专票' : '普票' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 2 ? 'info' : 'warning'" size="small">
            {{ statusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="160">
        <template slot-scope="scope">{{ formatDateTime(scope.row.applyTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template slot-scope="scope">
          <template v-if="scope.row.status === 0">
            <el-button size="mini" type="primary" @click="openHandle(scope.row, 1)">上传开票</el-button>
            <el-button size="mini" type="info" @click="openHandle(scope.row, 2)">驳回</el-button>
          </template>
          <span v-else class="text-muted">已处理</span>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-section">
      <el-pagination
        @size-change="(v) => { pagination.size = v; loadData(); }"
        @current-change="(v) => { pagination.current = v; loadData(); }"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next"
        :total="pagination.total"
        background
      />
    </div>

    <el-dialog :title="handleAction === 1 ? '上传发票' : '驳回'" :visible.sync="handleVisible" width="480px">
      <el-form v-if="handleAction === 1" :model="handleForm" label-width="90px">
        <el-form-item label="发票文件">
          <el-upload
            :action="uploadAction"
            :on-success="(res, file) => { const d = res && res.data; handleForm.fileId = (Array.isArray(d) && d[0] && d[0].id) ? d[0].id : (d && d.id) ? d.id : (typeof d === 'number') ? d : null; }"
            :show-file-list="true"
            :limit="1"
          >
            <el-button size="small" type="primary">选择文件</el-button>
          </el-upload>
          <span class="tip">上传后点击确定完成开票</span>
        </el-form-item>
      </el-form>
      <el-form v-else :model="handleForm" label-width="90px">
        <el-form-item label="驳回原因">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="3" placeholder="请输入驳回原因"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="handleLoading">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAdminInvoicePage, handleInvoice } from "@/api/invoice";

export default {
  name: "AdminInvoice",
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: { status: undefined, userId: "" },
      pagination: { current: 1, size: 10, total: 0 },
      handleVisible: false,
      handleLoading: false,
      handleAction: 1,
      handleForm: { id: null, fileId: null, handleRemark: "" },
      uploadAction: "/api/OnlineAuction/auctionFile/upload",
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
          status: this.searchForm.status,
          userId: this.searchForm.userId ? parseInt(this.searchForm.userId) : undefined,
        };
        const res = await getAdminInvoicePage(params);
        this.tableData = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.tableData = [];
      } finally {
        this.loading = false;
      }
    },
    statusText(s) {
      const m = { 0: "待处理", 1: "已开票", 2: "已驳回" };
      return m[s] || "-";
    },
    formatDateTime(v) {
      if (!v) return "-";
      const d = new Date(v);
      return isNaN(d.getTime()) ? v : d.toLocaleString("zh-CN", { hour12: false });
    },
    openHandle(row, action) {
      this.handleAction = action;
      this.handleForm = { id: row.id, fileId: null, handleRemark: "" };
      this.handleVisible = true;
    },
    async submitHandle() {
      if (this.handleAction === 1 && !this.handleForm.fileId) {
        this.$message.warning("请先上传发票文件");
        return;
      }
      if (this.handleAction === 2 && !this.handleForm.handleRemark) {
        this.$message.warning("请输入驳回原因");
        return;
      }
      this.handleLoading = true;
      try {
        await handleInvoice(this.handleForm.id, {
          status: this.handleAction,
          fileId: this.handleForm.fileId,
          handleRemark: this.handleForm.handleRemark,
        });
        this.$message.success("处理成功");
        this.handleVisible = false;
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "处理失败");
      } finally {
        this.handleLoading = false;
      }
    },
  },
};
</script>

<style scoped>
.admin-invoice-page { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; }
.filter-section { margin-bottom: 16px; }
.pagination-section { margin-top: 20px; text-align: right; }
.tip { font-size: 12px; color: #909399; margin-left: 8px; }
.text-muted { color: #909399; font-size: 12px; }
</style>
