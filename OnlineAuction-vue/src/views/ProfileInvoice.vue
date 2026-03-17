<template>
  <div class="profile-invoice-page">
    <div class="section-header">
      <h4>发票申请</h4>
      <el-button type="primary" size="small" icon="el-icon-plus" @click="applyVisible = true">申请发票</el-button>
    </div>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column prop="invoiceTitle" label="发票抬头" min-width="140" show-overflow-tooltip></el-table-column>
      <el-table-column prop="amount" label="金额" width="100">
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
    </el-table>
    <div v-if="pagination.total > 0" class="pagination-wrap">
      <el-pagination
        small
        :current-page="pagination.current"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="(p) => { pagination.current = p; loadData(); }"
      />
    </div>

    <el-dialog title="申请发票" :visible.sync="applyVisible" width="480px" @close="applyForm = {}">
      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="90px">
        <el-form-item label="发票抬头" prop="invoiceTitle">
          <el-input v-model="applyForm.invoiceTitle" placeholder="请输入发票抬头"></el-input>
        </el-form-item>
        <el-form-item label="税号">
          <el-input v-model="applyForm.taxNo" placeholder="选填"></el-input>
        </el-form-item>
        <el-form-item label="开票金额" prop="amount">
          <el-input-number v-model="applyForm.amount" :min="0.01" :precision="2" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="发票类型">
          <el-radio-group v-model="applyForm.invoiceType">
            <el-radio :label="1">普票</el-radio>
            <el-radio :label="2">专票</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply" :loading="applyLoading">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMyInvoicePage, applyInvoice } from "@/api/invoice";

export default {
  name: "ProfileInvoice",
  data() {
    return {
      loading: false,
      list: [],
      pagination: { current: 1, size: 10, total: 0 },
      applyVisible: false,
      applyLoading: false,
      applyForm: { invoiceTitle: "", taxNo: "", amount: null, invoiceType: 1 },
      applyRules: {
        invoiceTitle: [{ required: true, message: "请输入发票抬头", trigger: "blur" }],
        amount: [{ required: true, message: "请输入开票金额", trigger: "blur" }],
      },
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const res = await getMyInvoicePage({
          current: this.pagination.current,
          size: this.pagination.size,
        });
        this.list = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.list = [];
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
    submitApply() {
      this.$refs.applyFormRef.validate(async (valid) => {
        if (!valid) return;
        this.applyLoading = true;
        try {
          await applyInvoice({
            invoiceTitle: this.applyForm.invoiceTitle,
            taxNo: this.applyForm.taxNo,
            amount: this.applyForm.amount,
            invoiceType: this.applyForm.invoiceType,
          });
          this.$message.success("申请成功，请等待财务处理");
          this.applyVisible = false;
          this.loadData();
        } catch (e) {
          this.$message.error(e.message || "申请失败");
        } finally {
          this.applyLoading = false;
        }
      });
    },
  },
};
</script>

<style scoped>
.profile-invoice-page { padding: 16px 0; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-header h4 { margin: 0; }
.pagination-wrap { margin-top: 16px; }
</style>
