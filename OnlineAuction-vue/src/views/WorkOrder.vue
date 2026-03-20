<template>
  <div class="work-order-page">
    <div class="page-header">
      <h2>{{ isAdminReviewer ? "工单复核" : "工单处理" }}</h2>
    </div>

    <div class="toolbar">
      <el-select
        v-if="isCustomerService || isAdminReviewer"
        v-model="statusFilter"
        placeholder="处理状态"
        clearable
        style="width: 160px"
        @change="loadData"
      >
        <el-option label="全部状态" :value="''"></el-option>
        <el-option label="待处理" :value="0"></el-option>
        <el-option label="处理中" :value="1"></el-option>
        <el-option label="待复核" :value="2"></el-option>
        <el-option label="已完成" :value="3"></el-option>
        <el-option label="已驳回" :value="4"></el-option>
      </el-select>
      <el-select
        v-if="isAdminReviewer"
        v-model="typeFilter"
        placeholder="工单类型"
        clearable
        style="width: 160px"
        @change="loadData"
      >
        <el-option label="全部类型" value=""></el-option>
        <el-option label="商品" value="goods"></el-option>
        <el-option label="订单" value="order"></el-option>
        <el-option label="竞拍" value="bid"></el-option>
        <el-option label="留言" value="message"></el-option>
        <el-option label="风控" value="risk"></el-option>
        <el-option label="其他" value="other"></el-option>
      </el-select>
      <el-button size="small" icon="el-icon-refresh" @click="loadData">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" stripe>
      <el-table-column prop="workNo" label="工单号" width="170"></el-table-column>
      <el-table-column prop="workType" label="类型" width="100">
        <template slot-scope="scope">
          <el-tag size="small">{{ getTypeText(scope.row.workType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="relatedId" label="关联ID" width="90"></el-table-column>
      <el-table-column prop="title" label="标题" min-width="180"></el-table-column>
      <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip></el-table-column>
      <el-table-column prop="userName" label="提交人" width="120"></el-table-column>
      <el-table-column prop="workStatus" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.workStatus)" size="small">
            {{ getStatusText(scope.row.workStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handleResult" label="处理结果" min-width="180" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="170">
        <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column v-if="isCustomerService" label="操作" width="120">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" :disabled="scope.row.workStatus >= 3" @click="openProcessDialog(scope.row)">处理</el-button>
        </template>
      </el-table-column>
      <el-table-column v-if="isAdminReviewer" label="操作" width="120">
        <template slot-scope="scope">
          <el-button size="mini" type="warning" :disabled="scope.row.workStatus !== 2" @click="openReviewDialog(scope.row)">复核</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap" v-if="pagination.total > 0">
      <el-pagination
        :current-page="pagination.current"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog title="客服处理工单" :visible.sync="processVisible" width="620px">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="工单状态">
          <el-select v-model="processForm.workStatus" style="width: 100%">
            <el-option label="处理中" :value="1"></el-option>
            <el-option label="提交管理员复核" :value="2"></el-option>
            <el-option label="客服直接关闭" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="processForm.workStatus === 2" label="建议处罚">
          <el-select v-model="processForm.penaltyType" style="width: 100%">
            <el-option label="无处罚" value="NONE"></el-option>
            <el-option label="警告" value="WARN"></el-option>
            <el-option label="扣保证金" value="DEDUCT_DEPOSIT"></el-option>
            <el-option label="禁用账号" value="BAN_USER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="processForm.workStatus === 2" label="处罚对象ID">
          <el-input v-model.number="processForm.penaltyTargetUserId" placeholder="选填：建议处罚对象用户ID"></el-input>
        </el-form-item>
        <el-form-item v-if="processForm.workStatus === 2 && processForm.penaltyType === 'DEDUCT_DEPOSIT'" label="扣款金额">
          <el-input-number v-model="processForm.penaltyAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input v-model="processForm.handleResult" type="textarea" :rows="5" maxlength="1000" show-word-limit placeholder="请填写客服核实和处理意见" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" :loading="processLoading" @click="submitProcess">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="管理员复核" :visible.sync="reviewVisible" width="620px">
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="复核结果">
          <el-radio-group v-model="reviewForm.approve">
            <el-radio :label="true">通过（执行处罚）</el-radio>
            <el-radio :label="false">驳回（不处罚）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="reviewForm.approve" label="处罚类型">
          <el-select v-model="reviewForm.penaltyType" style="width: 100%">
            <el-option label="无处罚" value="NONE"></el-option>
            <el-option label="警告" value="WARN"></el-option>
            <el-option label="扣保证金" value="DEDUCT_DEPOSIT"></el-option>
            <el-option label="禁用账号" value="BAN_USER"></el-option>
            <el-option label="解封账号" value="UNBAN_USER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="reviewForm.approve" label="处罚对象ID">
          <el-input v-model.number="reviewForm.penaltyTargetUserId" placeholder="处罚对象用户ID"></el-input>
        </el-form-item>
        <el-form-item v-if="reviewForm.approve && reviewForm.penaltyType === 'DEDUCT_DEPOSIT'" label="扣款金额">
          <el-input-number v-model="reviewForm.penaltyAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="复核意见">
          <el-input v-model="reviewForm.reviewResult" type="textarea" :rows="5" maxlength="1000" show-word-limit placeholder="请填写最终复核结论"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="submitReview">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getServiceWorkOrderPage, serviceProcessWorkOrder, getAdminReviewWorkOrderPage, adminReviewWorkOrder } from "@/api/workOrder";

export default {
  name: "WorkOrder",
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: { current: 1, size: 10, total: 0 },
      isCustomerService: false,
      isAdminReviewer: false,
      statusFilter: "",
      typeFilter: "",
      processVisible: false,
      processLoading: false,
      processRowId: null,
      processForm: {
        workStatus: 1,
        handleResult: "",
        penaltyType: "NONE",
        penaltyTargetUserId: null,
        penaltyAmount: 0,
      },
      reviewVisible: false,
      reviewLoading: false,
      reviewRowId: null,
      reviewForm: {
        approve: true,
        reviewResult: "",
        penaltyType: "NONE",
        penaltyTargetUserId: null,
        penaltyAmount: 0,
      },
    };
  },
  mounted() {
    try {
      const user = JSON.parse(localStorage.getItem("userInfo") || "{}");
      const roles = user.userRole ? String(user.userRole).split(",").map((r) => r.trim()) : [];
      this.isCustomerService = roles.includes("6");
      this.isAdminReviewer = roles.includes("3");
    } catch (e) {
      this.isCustomerService = false;
      this.isAdminReviewer = false;
    }
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const params = { current: this.pagination.current, size: this.pagination.size };
        if (this.statusFilter !== "" && this.statusFilter !== null && this.statusFilter !== undefined) {
          params.workStatus = this.statusFilter;
        }
        if (this.isAdminReviewer && this.typeFilter && this.typeFilter.trim() !== "") {
          params.workType = this.typeFilter;
        }
        const res = this.isAdminReviewer
          ? await getAdminReviewWorkOrderPage(params)
          : await getServiceWorkOrderPage(params);
        this.tableData = (res && (res.list || res.records)) || [];
        this.pagination.total = (res && res.total) || 0;
      } finally {
        this.loading = false;
      }
    },
    handleCurrentChange(page) {
      this.pagination.current = page;
      this.loadData();
    },
    openProcessDialog(row) {
      this.processRowId = row.id;
      this.processForm = {
        workStatus: row.workStatus === 3 ? 3 : (row.workStatus === 2 ? 2 : 1),
        handleResult: row.handleResult || "",
        penaltyType: row.penaltyType || "NONE",
        penaltyTargetUserId: row.penaltyTargetUserId || null,
        penaltyAmount: row.penaltyAmount || 0,
      };
      this.processVisible = true;
    },
    async submitProcess() {
      if ((this.processForm.workStatus === 2 || this.processForm.workStatus === 3) && !this.processForm.handleResult.trim()) {
        return this.$message.warning("请填写处理意见");
      }
      this.processLoading = true;
      try {
        await serviceProcessWorkOrder(this.processRowId, this.processForm);
        this.$message.success("处理成功");
        this.processVisible = false;
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "处理失败");
      } finally {
        this.processLoading = false;
      }
    },
    openReviewDialog(row) {
      this.reviewRowId = row.id;
      this.reviewForm = {
        approve: true,
        reviewResult: "",
        penaltyType: row.penaltyType || "NONE",
        penaltyTargetUserId: row.penaltyTargetUserId || null,
        penaltyAmount: row.penaltyAmount || 0,
      };
      this.reviewVisible = true;
    },
    async submitReview() {
      if (!this.reviewForm.reviewResult || !this.reviewForm.reviewResult.trim()) {
        return this.$message.warning("请填写复核意见");
      }
      this.reviewLoading = true;
      try {
        await adminReviewWorkOrder(this.reviewRowId, this.reviewForm);
        this.$message.success("复核完成");
        this.reviewVisible = false;
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "复核失败");
      } finally {
        this.reviewLoading = false;
      }
    },
    getTypeText(type) {
      const m = { goods: "商品", order: "订单", bid: "竞拍", message: "留言", risk: "风控", other: "其他" };
      return m[type] || "其他";
    },
    getStatusText(status) {
      const m = { 0: "待处理", 1: "处理中", 2: "待复核", 3: "已完成", 4: "已驳回" };
      return m[status] || "-";
    },
    getStatusType(status) {
      const m = { 0: "warning", 1: "primary", 2: "info", 3: "success", 4: "danger" };
      return m[status] || "info";
    },
    formatTime(v) {
      if (!v) return "-";
      const d = new Date(v);
      return isNaN(d.getTime()) ? v : d.toLocaleString("zh-CN", { hour12: false });
    },
  },
};
</script>

<style scoped>
.work-order-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.toolbar { margin-bottom: 12px; display: flex; gap: 10px; }
.pagination-wrap { margin-top: 16px; text-align: right; }
</style>
