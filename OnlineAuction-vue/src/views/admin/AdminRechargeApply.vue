<template>
  <div class="admin-recharge-apply-page">
    <div class="page-header">
      <h2>充值申请审核</h2>
    </div>
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 110px" @change="loadData">
            <el-option label="待审核" :value="0"></el-option>
            <el-option label="已通过" :value="1"></el-option>
            <el-option label="已驳回" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column prop="userId" label="用户ID" width="90"></el-table-column>
      <el-table-column prop="amount" label="申请金额" width="100">
        <template slot-scope="scope">¥{{ scope.row.amount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 2 ? 'info' : 'warning'" size="small">
            {{ statusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applyRemark" label="申请备注" min-width="120" show-overflow-tooltip></el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="160">
        <template slot-scope="scope">{{ formatDateTime(scope.row.applyTime) }}</template>
      </el-table-column>
      <el-table-column prop="handleTime" label="处理时间" width="160">
        <template slot-scope="scope">{{ formatDateTime(scope.row.handleTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template slot-scope="scope">
          <template v-if="scope.row.status === 0">
            <el-button size="mini" type="success" @click="approve(scope.row)">通过</el-button>
            <el-button size="mini" type="info" @click="reject(scope.row)">驳回</el-button>
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

    <el-dialog title="驳回原因" :visible.sync="rejectVisible" width="400px">
      <el-input v-model="rejectRemark" type="textarea" :rows="3" placeholder="选填"></el-input>
      <span slot="footer">
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReject" :loading="rejectLoading">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminRechargeApplyPage,
  approveRechargeApply,
  rejectRechargeApply,
} from "@/api/rechargeApply";

export default {
  name: "AdminRechargeApply",
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: { status: undefined },
      pagination: { current: 1, size: 10, total: 0 },
      rejectVisible: false,
      rejectRemark: "",
      rejectLoading: false,
      rejectId: null,
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
        };
        const res = await getAdminRechargeApplyPage(params);
        this.tableData = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.tableData = [];
      } finally {
        this.loading = false;
      }
    },
    statusText(s) {
      const m = { 0: "待审核", 1: "已通过", 2: "已驳回" };
      return m[s] || "-";
    },
    formatDateTime(v) {
      if (!v) return "-";
      const d = new Date(v);
      return isNaN(d.getTime()) ? v : d.toLocaleString("zh-CN", { hour12: false });
    },
    async approve(row) {
      try {
        await this.$confirm("通过后将自动为该用户充值相应金额，确定吗？", "通过申请", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "info",
        });
        await approveRechargeApply(row.id);
        this.$message.success("已通过并完成充值");
        this.loadData();
      } catch (e) {
        if (e !== "cancel") this.$message.error(e.message || "操作失败");
      }
    },
    reject(row) {
      this.rejectId = row.id;
      this.rejectRemark = "";
      this.rejectVisible = true;
    },
    async submitReject() {
      this.rejectLoading = true;
      try {
        await rejectRechargeApply(this.rejectId, { handleRemark: this.rejectRemark });
        this.$message.success("已驳回");
        this.rejectVisible = false;
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "操作失败");
      } finally {
        this.rejectLoading = false;
      }
    },
  },
};
</script>

<style scoped>
.admin-recharge-apply-page { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; }
.filter-section { margin-bottom: 16px; }
.pagination-section { margin-top: 20px; text-align: right; }
.text-muted { color: #909399; font-size: 12px; }
</style>
