<template>
  <div class="profile-order-page">
    <div class="filter-bar">
      <el-radio-group v-model="roleType" @change="handleRoleChange">
        <el-radio-button label="buyer">我的竞拍订单</el-radio-button>
        <el-radio-button label="seller">我的销售订单</el-radio-button>
      </el-radio-group>
      <el-select v-model="searchStatus" placeholder="全部状态" clearable @change="loadData" style="width: 120px; margin-left: 12px">
        <el-option label="待付款" :value="0"></el-option>
        <el-option label="待发货" :value="1"></el-option>
        <el-option label="待收货" :value="2"></el-option>
        <el-option label="已完成" :value="3"></el-option>
        <el-option label="已悔拍" :value="4"></el-option>
        <el-option label="已退款" :value="5"></el-option>
      </el-select>
    </div>
    <el-table v-loading="loading" :data="tableData" stripe>
      <el-table-column prop="orderNo" label="订单号" width="160"></el-table-column>
      <el-table-column prop="goodsId" label="商品ID" width="80"></el-table-column>
      <el-table-column prop="dealPrice" label="成交价" width="100"></el-table-column>
      <el-table-column prop="depositAmount" label="保证金" width="90"></el-table-column>
      <el-table-column prop="remainAmount" label="尾款" width="90"></el-table-column>
      <el-table-column prop="orderStatus" label="状态" width="90">
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.orderStatus)" size="small">
            {{ getStatusText(scope.row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payDeadline" label="尾款截止" width="160">
        <template slot-scope="scope">{{ formatDateTime(scope.row.payDeadline) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="物流" width="160">
        <template slot-scope="scope">
          <span v-if="scope.row.expressCompany">{{ scope.row.expressCompany }} {{ scope.row.expressNo }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template slot-scope="scope">
          <template v-if="roleType === 'buyer'">
            <el-button v-if="scope.row.orderStatus === 0" size="mini" type="primary" @click="handlePay(scope.row)">
              去付款
            </el-button>
            <el-button v-if="scope.row.orderStatus === 2" size="mini" type="success" @click="handleConfirmReceipt(scope.row)">
              确认收货
            </el-button>
          </template>
          <template v-else-if="roleType === 'seller'">
            <el-button v-if="scope.row.orderStatus === 1" size="mini" type="primary" @click="openShipDialog(scope.row)">
              发货
            </el-button>
          </template>
          <el-button size="mini" type="text" @click="openComplaintDialog(scope.row)">
            投诉
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog title="发货" :visible.sync="shipVisible" width="450px">
      <el-form :model="shipForm" label-width="90px">
        <el-form-item label="订单号">{{ shipForm.orderNo }}</el-form-item>
        <el-form-item label="快递公司" required>
          <el-input v-model="shipForm.expressCompany" placeholder="如：顺丰速运、中通快递" clearable></el-input>
        </el-form-item>
        <el-form-item label="快递单号" required>
          <el-input v-model="shipForm.expressNo" placeholder="请输入快递单号" clearable></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" @click="submitShip">确认发货</el-button>
      </span>
    </el-dialog>
    <el-dialog title="订单投诉" :visible.sync="complaintVisible" width="520px">
      <el-form :model="complaintForm" label-width="100px">
        <el-form-item label="订单号">
          <span>{{ complaintForm.orderNo || "-" }}</span>
        </el-form-item>
        <el-form-item label="投诉内容">
          <el-input
            v-model="complaintForm.complaintContent"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请说明订单相关问题"
          />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="complaintVisible = false">取消</el-button>
        <el-button type="primary" :loading="complaintLoading" @click="submitOrderComplaint">提交投诉</el-button>
      </span>
    </el-dialog>
    <el-empty v-if="!loading && tableData.length === 0" description="暂无订单"></el-empty>
    <div class="pagination-wrap" v-if="pagination.total > 0">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20]"
        :page-size="pagination.size"
        layout="total, prev, pager, next"
        :total="pagination.total"
      ></el-pagination>
    </div>
  </div>
</template>

<script>
import { getMyOrderPage, payOrder, confirmReceipt, shipOrder } from "@/api/order";
import { submitComplaint } from "@/api/complaint";

export default {
  name: "ProfileOrder",
  data() {
    return {
      loading: false,
      roleType: "buyer",
      searchStatus: undefined,
      tableData: [],
      pagination: { current: 1, size: 10, total: 0 },
      shipVisible: false,
      shipLoading: false,
      shipForm: { orderId: null, orderNo: "", expressCompany: "", expressNo: "" },
      complaintVisible: false,
      complaintLoading: false,
      complaintForm: {
        orderId: null,
        orderNo: "",
        goodsId: null,
        complaintContent: "",
      },
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    handleRoleChange() {
      this.pagination.current = 1;
      this.loadData();
    },
    async loadData() {
      this.loading = true;
      try {
        const res = await getMyOrderPage({
          current: this.pagination.current,
          size: this.pagination.size,
          orderStatus: this.searchStatus,
          role: this.roleType,
        });
        this.tableData = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.tableData = [];
      } finally {
        this.loading = false;
      }
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
    handlePay(row) {
      this.$confirm("确认已完成付款？", "支付确认", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      })
        .then(async () => {
          try {
            await payOrder(row.id);
            this.$message.success("支付成功");
            this.loadData();
          } catch (e) {
            this.$message.error(e.message || "支付失败");
          }
        })
        .catch(() => {});
    },
    handleConfirmReceipt(row) {
      this.$confirm("确认已收到商品，完成订单？", "确认收货", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      })
        .then(async () => {
          try {
            await confirmReceipt(row.id);
            this.$message.success("确认收货成功");
            this.loadData();
          } catch (e) {
            this.$message.error(e.message || "操作失败");
          }
        })
        .catch(() => {});
    },
    openShipDialog(row) {
      this.shipForm = {
        orderId: row.id,
        orderNo: row.orderNo || "",
        expressCompany: "",
        expressNo: "",
      };
      this.shipVisible = true;
    },
    async submitShip() {
      if (!this.shipForm.expressCompany || !this.shipForm.expressNo) {
        this.$message.warning("请填写快递公司和快递单号");
        return;
      }
      this.shipLoading = true;
      try {
        await shipOrder(
          this.shipForm.orderId,
          this.shipForm.expressCompany,
          this.shipForm.expressNo
        );
        this.$message.success("发货成功");
        this.shipVisible = false;
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "发货失败");
      } finally {
        this.shipLoading = false;
      }
    },
    openComplaintDialog(row) {
      this.complaintForm = {
        orderId: row.id,
        orderNo: row.orderNo || "",
        goodsId: row.goodsId || null,
        complaintContent: "",
      };
      this.complaintVisible = true;
    },
    async submitOrderComplaint() {
      const content = (this.complaintForm.complaintContent || "").trim();
      if (!this.complaintForm.orderId) {
        this.$message.warning("订单信息异常");
        return;
      }
      if (!content) {
        this.$message.warning("请填写投诉内容");
        return;
      }
      this.complaintLoading = true;
      try {
        await submitComplaint({
          complaintType: "order",
          targetId: this.complaintForm.orderId,
          relatedOrderId: this.complaintForm.orderId,
          relatedGoodsId: this.complaintForm.goodsId,
          complaintContent: content,
        });
        this.$message.success("投诉提交成功");
        this.complaintVisible = false;
      } catch (e) {
        this.$message.error(e.message || "投诉提交失败");
      } finally {
        this.complaintLoading = false;
      }
    },
  },
};
</script>

<style scoped>
.profile-order-page { padding: 20px; }
.filter-bar { margin-bottom: 20px; display: flex; align-items: center; }
.pagination-wrap { margin-top: 20px; }
.text-muted { color: #909399; font-size: 12px; }
</style>
