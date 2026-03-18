<template>
  <div class="admin-goods-time-page">
    <div class="page-header">
      <h2>限时拍管理</h2>
    </div>
    <p class="tip">设置商品竞拍起止时间。仅管理员、超级管理员、运营可操作。</p>
    <el-table v-loading="loading" :data="tableData" stripe>
      <el-table-column prop="id" label="商品ID" width="80" />
      <el-table-column prop="goodsName" label="商品名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="开始时间" width="170">
        <template slot-scope="scope">{{ formatTime(scope.row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="170">
        <template slot-scope="scope">{{ formatTime(scope.row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="openEdit(scope.row)">设置时间</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination
        :current-page="pagination.current"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="(p) => { pagination.current = p; loadData(); }"
      />
    </div>
    <el-dialog title="设置竞拍时间" :visible.sync="dialogVisible" width="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="选择开始时间"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="选择结束时间"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTime" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getGoodsPage, setGoodsTime } from "@/api/goods";

export default {
  name: "AdminGoodsTime",
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: { current: 1, size: 10, total: 0 },
      dialogVisible: false,
      submitLoading: false,
      form: { id: null, startTime: "", endTime: "" },
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const res = await getGoodsPage({
          current: this.pagination.current,
          size: this.pagination.size,
        });
        this.tableData = (res && res.list) || [];
        this.pagination.total = (res && res.total) || 0;
      } catch (e) {
        this.$message.error("加载失败");
      } finally {
        this.loading = false;
      }
    },
    formatTime(v) {
      if (!v) return "-";
      const d = new Date(v);
      return isNaN(d.getTime()) ? v : d.toLocaleString("zh-CN", { hour12: false });
    },
    openEdit(row) {
      this.form.id = row.id;
      this.form.startTime = row.startTime ? this.formatTime(row.startTime) : "";
      this.form.endTime = row.endTime ? this.formatTime(row.endTime) : "";
      this.dialogVisible = true;
    },
    async submitTime() {
      if (!this.form.id) return;
      this.submitLoading = true;
      try {
        await setGoodsTime(this.form.id, {
          startTime: this.form.startTime,
          endTime: this.form.endTime,
        });
        this.$message.success("设置成功");
        this.dialogVisible = false;
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "设置失败");
      } finally {
        this.submitLoading = false;
      }
    },
  },
};
</script>

<style scoped>
.admin-goods-time-page { padding: 20px; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.tip { color: #909399; font-size: 13px; margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; text-align: right; }
</style>
