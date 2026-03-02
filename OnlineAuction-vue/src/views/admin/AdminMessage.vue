<template>
  <div class="admin-message-page">
    <div class="page-header">
      <h2>留言板管理</h2>
    </div>

    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            clearable
            placeholder="搜索留言内容"
            style="width: 220px"
            @keyup.enter.native="handleSearch"
          ></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.messageStatus"
            clearable
            placeholder="全部"
            style="width: 120px"
          >
            <el-option label="未回复" :value="0"></el-option>
            <el-option label="已回复" :value="1"></el-option>
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
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="userName" label="留言用户" width="140"></el-table-column>
        <el-table-column prop="messageContent" label="留言内容" min-width="260" show-overflow-tooltip></el-table-column>
        <el-table-column prop="messageStatus" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.messageStatus === 1 ? 'success' : 'warning'">
              {{ scope.row.messageStatus === 1 ? "已回复" : "未回复" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="replyContent" label="回复内容" min-width="220" show-overflow-tooltip>
          <template slot-scope="scope">
            <span>{{ scope.row.replyContent || "-" }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="留言时间" width="170">
          <template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="replyTime" label="回复时间" width="170">
          <template slot-scope="scope">{{ formatDateTime(scope.row.replyTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" icon="el-icon-chat-line-square" @click="openReplyDialog(scope.row)">
              回复
            </el-button>
            <el-button size="mini" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)">
              删除
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
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        background
      >
      </el-pagination>
    </div>

    <el-dialog title="回复留言" :visible.sync="replyDialogVisible" width="560px" @close="handleReplyDialogClose">
      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="90px">
        <el-form-item label="留言内容">
          <div class="message-preview">{{ replyForm.messageContent || "-" }}</div>
        </el-form-item>
        <el-form-item label="回复内容" prop="replyContent">
          <el-input
            v-model="replyForm.replyContent"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请输入回复内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="submitReply">提交回复</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminMessagePage,
  replyMessage,
  deleteAdminMessage,
} from "@/api/message";

export default {
  name: "AdminMessage",
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: {
        keyword: "",
        messageStatus: undefined,
      },
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
      replyDialogVisible: false,
      replyLoading: false,
      replyForm: {
        id: null,
        messageContent: "",
        replyContent: "",
      },
      replyRules: {
        replyContent: [
          { required: true, message: "请输入回复内容", trigger: "blur" },
          { min: 1, max: 500, message: "回复内容长度为1-500个字符", trigger: "blur" },
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
        const res = await getAdminMessagePage({
          current: this.pagination.current,
          size: this.pagination.size,
          keyword: this.searchForm.keyword || undefined,
          messageStatus: this.searchForm.messageStatus,
        });
        this.tableData = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
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
      this.searchForm = {
        keyword: "",
        messageStatus: undefined,
      };
      this.pagination.current = 1;
      this.loadData();
    },
    handleSizeChange(size) {
      this.pagination.size = size;
      this.pagination.current = 1;
      this.loadData();
    },
    handleCurrentChange(current) {
      this.pagination.current = current;
      this.loadData();
    },
    openReplyDialog(row) {
      this.replyForm = {
        id: row.id,
        messageContent: row.messageContent || "",
        replyContent: row.replyContent || "",
      };
      this.replyDialogVisible = true;
    },
    handleReplyDialogClose() {
      if (this.$refs.replyFormRef) {
        this.$refs.replyFormRef.resetFields();
      }
      this.replyForm = {
        id: null,
        messageContent: "",
        replyContent: "",
      };
    },
    submitReply() {
      this.$refs.replyFormRef.validate(async (valid) => {
        if (!valid) return;
        this.replyLoading = true;
        try {
          await replyMessage(this.replyForm.id, { replyContent: this.replyForm.replyContent });
          this.$message.success("回复成功");
          this.replyDialogVisible = false;
          this.loadData();
        } catch (e) {
          // 错误提示由请求拦截器处理
        } finally {
          this.replyLoading = false;
        }
      });
    },
    handleDelete(row) {
      this.$confirm("确定要删除这条留言吗？删除后不可恢复。", "删除确认", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          await deleteAdminMessage(row.id);
          this.$message.success("删除成功");
          this.loadData();
        })
        .catch(() => {});
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
.page-header {
  margin-bottom: 16px;
}

.filter-section,
.table-section,
.pagination-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.pagination-section {
  display: flex;
  justify-content: center;
}

.message-preview {
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px 12px;
  color: #606266;
  line-height: 1.5;
  max-height: 160px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>

