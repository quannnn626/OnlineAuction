<template>
  <div class="admin-order-page">
    <div class="page-header">
      <h2>竞拍订单</h2>
    </div>
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="订单编号" clearable style="width: 180px"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.orderStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="待付款" :value="0"></el-option>
            <el-option label="待发货" :value="1"></el-option>
            <el-option label="待收货" :value="2"></el-option>
            <el-option label="已完成" :value="3"></el-option>
            <el-option label="已悔拍" :value="4"></el-option>
            <el-option label="已退款" :value="5"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="canManageOrder" label="买方ID">
          <el-input v-model="searchForm.buyerId" placeholder="买方用户ID（筛选）" clearable style="width: 130px"></el-input>
        </el-form-item>
        <el-form-item v-if="canManageOrder" label="卖方ID">
          <el-input v-model="searchForm.sellerId" placeholder="卖方用户ID（筛选）" clearable style="width: 130px"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="table-section">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="160"></el-table-column>
        <el-table-column prop="goodsName" label="商品名称" min-width="120"></el-table-column>
        <el-table-column prop="buyerName" label="买方" width="100"></el-table-column>
        <el-table-column prop="sellerName" label="卖方" width="100"></el-table-column>
        <el-table-column prop="dealPrice" label="成交价" width="100"></el-table-column>
        <el-table-column prop="depositAmount" label="保证金" width="90"></el-table-column>
        <el-table-column prop="remainAmount" label="尾款" width="90"></el-table-column>
        <el-table-column prop="orderStatus" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.orderStatus)">{{ getStatusText(scope.row.orderStatus) }}</el-tag>
            <span v-if="scope.row.confirmationNo" class="confirmation-no" title="成交确认书">📄{{ scope.row.confirmationNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payDeadline" label="尾款截止" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.payDeadline) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="物流" width="140" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.expressCompany">{{ scope.row.expressCompany }}<br>{{ scope.row.expressNo }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="canConfirmDeal && !scope.row.confirmDealAt && (scope.row.orderStatus === 0 || scope.row.orderStatus === 1)" size="mini" type="info" icon="el-icon-bangzhu" @click="handleConfirmDeal(scope.row)">
              落槌确认
            </el-button>
            <el-button v-if="canConfirmDeal && scope.row.orderStatus === 0" size="mini" type="warning" icon="el-icon-warning-outline" @click="handleMarkDefault(scope.row)">
              标记悔拍
            </el-button>
            <el-button v-if="canManageOrder && scope.row.orderStatus === 0" size="mini" type="success" icon="el-icon-check" @click="handleSettle(scope.row)">
              确认结算
            </el-button>
            <el-button v-if="canShip(scope.row)" size="mini" type="primary" icon="el-icon-truck" @click="handleShip(scope.row)">
              发货
            </el-button>
            <el-button v-if="canManageOrder && scope.row.orderStatus === 2" size="mini" type="success" icon="el-icon-check" @click="handleConfirmReceipt(scope.row)">
              确认收货
            </el-button>
            <el-button
              v-if="canManageOrder && canRefund(scope.row)"
              size="mini"
              type="warning"
              icon="el-icon-refresh-left"
              @click="handleRefund(scope.row)"
            >
              退款
            </el-button>
            <template v-if="!canManageOrder && !canConfirmDeal && !canShip(scope.row)">
              <span class="text-muted">仅查看</span>
            </template>
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

    <el-dialog title="发货" :visible.sync="shipVisible" width="450px">
      <el-form :model="shipForm" ref="shipFormRef" label-width="100px">
        <el-form-item label="订单号">
          <span>{{ shipForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="快递公司" prop="expressCompany" required>
          <el-input v-model="shipForm.expressCompany" placeholder="如：顺丰速运、中通快递" clearable></el-input>
        </el-form-item>
        <el-form-item label="快递单号" prop="expressNo" required>
          <el-input v-model="shipForm.expressNo" placeholder="请输入快递单号" clearable></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" @click="submitShip" :loading="shipLoading">确认发货</el-button>
      </div>
    </el-dialog>

    <el-dialog title="退款" :visible.sync="refundVisible" width="450px">
      <el-form :model="refundForm" ref="refundFormRef" label-width="80px">
        <el-form-item label="订单号">
          <span>{{ refundForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="退款金额">
          <span>{{ refundForm.depositAmount }} 元（保证金）</span>
        </el-form-item>
        <el-form-item label="退款原因" prop="remark">
          <el-input v-model="refundForm.remark" type="textarea" :rows="3" placeholder="可选"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="warning" @click="submitRefund" :loading="refundLoading">确认退款</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminOrderPage,
  updateOrderStatus,
  processRefund,
  confirmDeal,
  shipOrder,
  markOrderDefault,
} from "@/api/order";

export default {
  name: "AdminOrder",
  computed: {
    // 财务、管理员、超级管理员可操作订单（结算、发货、退款）；客服仅查看
    canManageOrder() {
      const userInfo = localStorage.getItem("userInfo");
      if (!userInfo) return false;
      try {
        const user = JSON.parse(userInfo);
        const roles = user.userRole ? String(user.userRole).split(",").map((r) => r.trim()) : [];
        return roles.some((r) => ["3", "4", "7"].includes(r));
      } catch (e) {
        return false;
      }
    },
    // 拍卖师、管理员、超级管理员可落槌确认
    canConfirmDeal() {
      const userInfo = localStorage.getItem("userInfo");
      if (!userInfo) return false;
      try {
        const user = JSON.parse(userInfo);
        const roles = user.userRole ? String(user.userRole).split(",").map((r) => r.trim()) : [];
        return roles.some((r) => ["3", "4", "5"].includes(r));
      } catch (e) {
        return false;
      }
    },
    // 卖方本人、管理员、超级管理员、运营可发货
    currentUserId() {
      try {
        const user = JSON.parse(localStorage.getItem("userInfo") || "{}");
        return user.id || user.userId || null;
      } catch (e) {
        return null;
      }
    },
  },
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: { orderNo: "", orderStatus: undefined, buyerId: "", sellerId: "" },
      pagination: { current: 1, size: 10, total: 0 },
      refundVisible: false,
      refundLoading: false,
      refundForm: { orderId: null, orderNo: "", depositAmount: null, remark: "" },
      shipVisible: false,
      shipLoading: false,
      shipForm: { orderId: null, orderNo: "", expressCompany: "", expressNo: "" },
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    canRefund(row) {
      return row.orderStatus !== 4 && row.orderStatus !== 5;
    },
    canShip(row) {
      if (row.orderStatus !== 1) return false;
      const roles = (() => {
        try {
          const user = JSON.parse(localStorage.getItem("userInfo") || "{}");
          return user.userRole ? String(user.userRole).split(",").map((r) => r.trim()) : [];
        } catch (e) {
          return [];
        }
      })();
      if (roles.some((r) => ["3", "4", "8"].includes(r))) return true;
      const uid = this.currentUserId;
      return uid && row.sellerId && String(uid) === String(row.sellerId);
    },
    handleConfirmDeal(row) {
      this.$confirm("确认对该订单执行落槌确认？将生成成交确认书编号并锁定买受人。", "落槌确认", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      })
        .then(async () => {
          try {
            await confirmDeal(row.id);
            this.$message.success("落槌确认成功");
            this.loadData();
          } catch (e) {
            this.$message.error(e.message || "操作失败");
          }
        })
        .catch(() => {});
    },
    handleShip(row) {
      this.shipForm = { orderId: row.id, orderNo: row.orderNo, expressCompany: "", expressNo: "" };
      this.shipVisible = true;
    },
    async submitShip() {
      if (!this.shipForm.expressCompany || !this.shipForm.expressNo) {
        this.$message.warning("请填写快递公司和快递单号");
        return;
      }
      this.shipLoading = true;
      try {
        await shipOrder(this.shipForm.orderId, this.shipForm.expressCompany, this.shipForm.expressNo);
        this.$message.success("发货成功");
        this.shipVisible = false;
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "发货失败");
      } finally {
        this.shipLoading = false;
      }
    },
    async loadData() {
      this.loading = true;
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          orderStatus: this.searchForm.orderStatus,
          orderNo: this.searchForm.orderNo || undefined,
        };
        if (this.searchForm.buyerId) params.buyerId = parseInt(this.searchForm.buyerId);
        if (this.searchForm.sellerId) params.sellerId = parseInt(this.searchForm.sellerId);
        const res = await getAdminOrderPage(params);
        this.tableData = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.tableData = [];
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.pagination.current = 1;
      this.loadData();
    },
    handleReset() {
      this.searchForm = { orderNo: "", orderStatus: undefined, buyerId: "", sellerId: "" };
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
    handleSettle(row) {
      this.$confirm("确认买方已付尾款，标记为待发货？", "订单结算", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      })
        .then(async () => {
          try {
            await updateOrderStatus(row.id, 1);
            this.$message.success("结算成功");
            this.loadData();
          } catch (e) {
            this.$message.error(e.message || "操作失败");
          }
        })
        .catch(() => {});
    },
    handleMarkDefault(row) {
      this.$confirm(
        "确认买方未付款/悔拍？将扣除保证金，订单标记为已悔拍，商品恢复上架供拍卖师再次上架。",
        "标记悔拍",
        { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" }
      )
        .then(async () => {
          try {
            await markOrderDefault(row.id);
            this.$message.success("已标记悔拍");
            this.loadData();
          } catch (e) {
            this.$message.error(e.message || "操作失败");
          }
        })
        .catch(() => {});
    },
    handleConfirmReceipt(row) {
      this.$confirm("确认买方已收货，标记为已完成？", "确认收货", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      })
        .then(async () => {
          try {
            await updateOrderStatus(row.id, 3);
            this.$message.success("操作成功");
            this.loadData();
          } catch (e) {
            this.$message.error(e.message || "操作失败");
          }
        })
        .catch(() => {});
    },
    handleRefund(row) {
      this.refundForm = {
        orderId: row.id,
        orderNo: row.orderNo,
        depositAmount: row.depositAmount,
        remark: "",
      };
      this.refundVisible = true;
    },
    async submitRefund() {
      this.refundLoading = true;
      try {
        await processRefund(this.refundForm.orderId, this.refundForm.remark);
        this.$message.success("退款成功");
        this.refundVisible = false;
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "退款失败");
      } finally {
        this.refundLoading = false;
      }
    },
    getStatusText(s) {
      const map = { 0: "待付款", 1: "待发货", 2: "待收货", 3: "已完成", 4: "已悔拍", 5: "已退款" };
      return map[s] || "-";
    },
    getStatusTagType(s) {
      const map = { 0: "warning", 1: "primary", 2: "primary", 3: "success", 4: "info", 5: "info" };
      return map[s] || "info";
    },
    formatDateTime(val) {
      if (!val) return "-";
      const d = new Date(val);
      return isNaN(d.getTime()) ? val : d.toLocaleString("zh-CN", { hour12: false });
    },
  },
};
</script>

<style scoped>
.admin-order-page { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; }
.filter-section { margin-bottom: 20px; }
.table-section { background: #fff; padding: 20px; border-radius: 4px; }
.pagination-section { margin-top: 20px; text-align: right; }
.text-muted { color: #909399; font-size: 12px; }
.confirmation-no { display: block; font-size: 11px; color: #67c23a; margin-top: 2px; }
</style>
