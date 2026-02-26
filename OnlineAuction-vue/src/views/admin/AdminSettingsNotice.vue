<template>
  <div class="admin-settings-notice-page">
    <div class="page-header">
      <h2>竞拍公告管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          新增公告
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索标题或内容"
            clearable
            @keyup.enter.native="handleSearch"
            style="width: 220px"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.noticeStatus"
            placeholder="状态"
            clearable
            style="width: 120px"
          >
            <el-option label="全部" :value="undefined"></el-option>
            <el-option label="未发布" :value="0"></el-option>
            <el-option label="已发布" :value="1"></el-option>
            <el-option label="已下架" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">
            搜索
          </el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <div class="table-section">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="noticeTitle" label="标题" min-width="200" show-overflow-tooltip>
        </el-table-column>
        <el-table-column prop="isTop" label="置顶" width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isTop === 1" type="danger" size="mini">
              置顶
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="noticeStatus" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.noticeStatus)">
              {{ getStatusText(scope.row.noticeStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.publishTime || scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="scope.row.noticeStatus !== 1"
              size="mini"
              type="success"
              icon="el-icon-upload2"
              @click="handlePublish(scope.row)"
            >
              发布
            </el-button>
            <el-button
              v-else
              size="mini"
              type="warning"
              icon="el-icon-download"
              @click="handleUnpublish(scope.row)"
            >
              下架
            </el-button>
            <el-button
              size="mini"
              :type="scope.row.isTop === 1 ? 'info' : 'success'"
              icon="el-icon-top"
              @click="handleToggleTop(scope.row)"
            >
              {{ scope.row.isTop === 1 ? "取消置顶" : "置顶" }}
            </el-button>
            <el-button
              size="mini"
              type="danger"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        background
      >
      </el-pagination>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="640px"
      @close="handleDialogClose"
    >
      <el-form
        :model="formData"
        :rules="formRules"
        ref="noticeForm"
        label-width="100px"
      >
        <el-form-item label="公告标题" prop="noticeTitle">
          <el-input
            v-model="formData.noticeTitle"
            placeholder="请输入公告标题"
            maxlength="100"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="公告内容" prop="noticeContent">
          <el-input
            v-model="formData.noticeContent"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容"
          ></el-input>
        </el-form-item>
        <el-form-item label="是否置顶" prop="isTop">
          <el-switch
            v-model="formData.isTop"
            :active-value="1"
            :inactive-value="0"
            active-text="置顶"
            inactive-text="不置顶"
          ></el-switch>
        </el-form-item>
        <el-form-item label="状态" prop="noticeStatus">
          <el-radio-group v-model="formData.noticeStatus">
            <el-radio :label="0">未发布</el-radio>
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="2">已下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminNoticePage,
  getAdminNoticeById,
  addNotice,
  updateNotice,
  deleteNotice,
} from "@/api/notice";

export default {
  name: "AdminSettingsNotice",
  data() {
    return {
      loading: false,
      submitLoading: false,
      tableData: [],
      searchForm: {
        keyword: "",
        noticeStatus: undefined,
      },
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
      dialogVisible: false,
      dialogTitle: "新增公告",
      isEdit: false,
      formData: {
        id: null,
        noticeTitle: "",
        noticeContent: "",
        isTop: 0,
        noticeStatus: 0,
      },
      formRules: {
        noticeTitle: [
          { required: true, message: "请输入公告标题", trigger: "blur" },
          { min: 1, max: 100, message: "标题长度1-100字符", trigger: "blur" },
        ],
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
        const res = await getAdminNoticePage({
          current: this.pagination.current,
          size: this.pagination.size,
          keyword: this.searchForm.keyword || undefined,
          noticeStatus: this.searchForm.noticeStatus,
        });
        if (res && res.list) {
          this.tableData = res.list;
          this.pagination.total = res.total || 0;
        } else {
          this.tableData = [];
          this.pagination.total = 0;
        }
      } catch (error) {
        this.$message.error(error.message || "加载失败");
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
      this.searchForm.keyword = "";
      this.searchForm.noticeStatus = undefined;
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
    handleAdd() {
      this.dialogTitle = "新增公告";
      this.isEdit = false;
      this.formData = {
        id: null,
        noticeTitle: "",
        noticeContent: "",
        isTop: 0,
        noticeStatus: 0,
      };
      this.dialogVisible = true;
    },
    async handleEdit(row) {
      try {
        const res = await getAdminNoticeById(row.id);
        this.dialogTitle = "编辑公告";
        this.isEdit = true;
        this.formData = {
          id: res.id,
          noticeTitle: res.noticeTitle,
          noticeContent: res.noticeContent || "",
          isTop: res.isTop !== undefined ? res.isTop : 0,
          noticeStatus: res.noticeStatus !== undefined ? res.noticeStatus : 0,
        };
        this.dialogVisible = true;
      } catch (error) {
        this.$message.error(error.message || "获取详情失败");
      }
    },
    handleSubmit() {
      this.$refs.noticeForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          if (this.formData.id) {
            await updateNotice(this.formData.id, this.formData);
            this.$message.success("更新成功");
          } else {
            await addNotice(this.formData);
            this.$message.success("新增成功");
          }
          this.dialogVisible = false;
          this.loadData();
        } catch (error) {
          this.$message.error(error.message || "操作失败");
        } finally {
          this.submitLoading = false;
        }
      });
    },
    handleDialogClose() {
      this.$refs.noticeForm && this.$refs.noticeForm.resetFields();
    },
    handlePublish(row) {
      this.$confirm("确定要发布该公告吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      })
        .then(async () => {
          try {
            await updateNotice(row.id, { ...row, noticeStatus: 1 });
            this.$message.success("发布成功");
            this.loadData();
          } catch (error) {
            this.$message.error(error.message || "发布失败");
          }
        })
        .catch(() => {});
    },
    handleUnpublish(row) {
      this.$confirm("确定要下架该公告吗？下架后前台将不可见。", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            await updateNotice(row.id, { ...row, noticeStatus: 2 });
            this.$message.success("下架成功");
            this.loadData();
          } catch (error) {
            this.$message.error(error.message || "下架失败");
          }
        })
        .catch(() => {});
    },
    handleToggleTop(row) {
      const newTop = row.isTop === 1 ? 0 : 1;
      const action = newTop === 1 ? "置顶" : "取消置顶";
      this.$confirm(`确定要${action}该公告吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      })
        .then(async () => {
          try {
            await updateNotice(row.id, { ...row, isTop: newTop });
            this.$message.success(`${action}成功`);
            this.loadData();
          } catch (error) {
            this.$message.error(error.message || `${action}失败`);
          }
        })
        .catch(() => {});
    },
    handleDelete(row) {
      this.$confirm("确定要删除该公告吗？删除后无法恢复。", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            await deleteNotice(row.id);
            this.$message.success("删除成功");
            this.loadData();
          } catch (error) {
            this.$message.error(error.message || "删除失败");
          }
        })
        .catch(() => {});
    },
    getStatusText(status) {
      const map = { 0: "未发布", 1: "已发布", 2: "已下架" };
      return map[status] || "未知";
    },
    getStatusTagType(status) {
      const map = { 0: "info", 1: "success", 2: "warning" };
      return map[status] || "info";
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
.admin-settings-notice-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.filter-section {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination-section {
  margin-top: 20px;
  text-align: right;
}
</style>
