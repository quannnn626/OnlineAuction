<template>
  <div class="admin-category-page">
    <div class="page-header">
      <h2>拍卖商品类目管理</h2>
      <div class="header-actions">
        <el-button
          v-if="selectedRows.length > 0"
          type="danger"
          icon="el-icon-delete"
          @click="handleBatchDelete"
        >
          批量删除 ({{ selectedRows.length }})
        </el-button>
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          新增分类
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类名称">
          <el-input
            v-model="searchForm.categoryName"
            placeholder="请输入分类名称"
            clearable
            @keyup.enter.native="handleSearch"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch"
            >搜索</el-button
          >
          <el-button icon="el-icon-refresh" @click="handleReset"
            >重置</el-button
          >
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
          align="center"
        ></el-table-column>
        <el-table-column
          prop="id"
          label="ID"
          width="80"
          align="center"
        ></el-table-column>
        <el-table-column
          prop="categoryName"
          label="分类名称"
          min-width="150"
        ></el-table-column>
        <el-table-column
          prop="categorySort"
          label="排序值"
          width="100"
          align="center"
        >
          <template slot-scope="scope">
            <el-tag size="small">{{ scope.row.categorySort || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
          align="center"
        >
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="updateTime"
          label="更新时间"
          width="180"
          align="center"
        >
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)"
              >编辑</el-button
            >
            <el-button
              type="text"
              icon="el-icon-delete"
              style="color: #f56c6c"
              @click="handleDelete(scope.row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.current"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
        ></el-pagination>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        :model="formData"
        :rules="formRules"
        ref="categoryForm"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="categoryName">
          <el-input
            v-model="formData.categoryName"
            placeholder="请输入分类名称"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="排序值" prop="categorySort">
          <el-input-number
            v-model="formData.categorySort"
            :min="0"
            :max="9999"
            placeholder="数字越小越靠前"
            style="width: 100%"
          ></el-input-number>
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            提示：数字越小，排序越靠前
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading"
          >确定</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getCategoryPage,
  addCategory,
  updateCategory,
  deleteCategory,
  batchDeleteCategory,
} from "@/api/category";

export default {
  name: "AdminCategory",
  data() {
    return {
      loading: false,
      submitLoading: false,
      tableData: [],
      selectedRows: [],
      searchForm: {
        categoryName: "",
      },
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
      dialogVisible: false,
      dialogTitle: "新增分类",
      formData: {
        id: null,
        categoryName: "",
        categorySort: 0,
      },
      formRules: {
        categoryName: [
          { required: true, message: "请输入分类名称", trigger: "blur" },
          {
            min: 1,
            max: 50,
            message: "分类名称长度在 1 到 50 个字符",
            trigger: "blur",
          },
        ],
        categorySort: [
          { required: true, message: "请输入排序值", trigger: "blur" },
        ],
      },
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    // 加载数据
    async loadData() {
      this.loading = true;
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          categoryName: this.searchForm.categoryName || undefined,
        };
        const result = await getCategoryPage(params);
        console.log("分页数据:", result); // 调试信息
        console.log("分页数据所有字段:", Object.keys(result || {})); // 打印所有字段名
        // MyBatis Plus的Page对象结构：{ records: [], total: 0, current: 1, size: 10, pages: 0 }
        if (result && typeof result === "object") {
          // 处理不同的字段名可能（total, totalCount, totalElements等）
          const total =
            result.total || result.totalCount || result.totalElements || 0;
          const records = result.records || result.list || result.data || [];

          this.tableData = records;
          this.pagination.total = total;

          console.log(
            "解析后的数据 - total:",
            total,
            "records数量:",
            records.length
          );

          // 同步当前页和每页大小（防止后端返回的数据与前端不一致）
          if (result.current !== undefined) {
            this.pagination.current = result.current;
          }
          if (result.size !== undefined) {
            this.pagination.size = result.size;
          }
        } else {
          // 如果返回的不是Page对象，可能是数组或其他格式
          console.warn("返回数据格式异常:", result);
          this.tableData = Array.isArray(result) ? result : [];
          this.pagination.total = Array.isArray(result) ? result.length : 0;
        }
      } catch (error) {
        console.error("加载数据失败:", error);
        this.$message.error("加载数据失败");
      } finally {
        this.loading = false;
      }
    },
    // 搜索
    handleSearch() {
      this.pagination.current = 1;
      this.loadData();
    },
    // 重置
    handleReset() {
      this.searchForm.categoryName = "";
      this.pagination.current = 1;
      this.loadData();
    },
    // 新增
    handleAdd() {
      this.dialogTitle = "新增分类";
      this.formData = {
        id: null,
        categoryName: "",
        categorySort: 0,
      };
      this.dialogVisible = true;
    },
    // 编辑
    handleEdit(row) {
      this.dialogTitle = "编辑分类";
      this.formData = {
        id: row.id,
        categoryName: row.categoryName,
        categorySort: row.categorySort || 0,
      };
      this.dialogVisible = true;
    },
    // 删除
    handleDelete(row) {
      this.$confirm(`确定要删除分类"${row.categoryName}"吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            await deleteCategory(row.id);
            this.$message.success("删除成功");
            this.loadData();
          } catch (error) {
            console.error("删除失败:", error);
            this.$message.error("删除失败");
          }
        })
        .catch(() => {});
    },
    // 批量删除
    handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning("请选择要删除的分类");
        return;
      }
      this.$confirm(
        `确定要删除选中的 ${this.selectedRows.length} 个分类吗？`,
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }
      )
        .then(async () => {
          try {
            const ids = this.selectedRows.map((row) => row.id);
            await batchDeleteCategory(ids);
            this.$message.success("批量删除成功");
            this.selectedRows = [];
            this.loadData();
          } catch (error) {
            console.error("批量删除失败:", error);
            this.$message.error("批量删除失败");
          }
        })
        .catch(() => {});
    },
    // 提交表单
    handleSubmit() {
      this.$refs.categoryForm.validate(async (valid) => {
        if (!valid) {
          return false;
        }
        this.submitLoading = true;
        try {
          if (this.formData.id) {
            // 更新
            await updateCategory(this.formData.id, this.formData);
            this.$message.success("更新成功");
          } else {
            // 新增
            await addCategory(this.formData);
            this.$message.success("新增成功");
          }
          this.dialogVisible = false;
          this.loadData();
        } catch (error) {
          console.error("操作失败:", error);
          this.$message.error(error.message || "操作失败");
        } finally {
          this.submitLoading = false;
        }
      });
    },
    // 对话框关闭
    handleDialogClose() {
      this.$refs.categoryForm && this.$refs.categoryForm.resetFields();
    },
    // 选择变化
    handleSelectionChange(selection) {
      this.selectedRows = selection;
    },
    // 分页大小变化
    handleSizeChange(size) {
      console.log("分页大小变化:", size, "当前值:", this.pagination.size);
      this.pagination.size = size;
      this.pagination.current = 1;
      this.loadData();
    },
    // 当前页变化
    handleCurrentChange(current) {
      console.log("当前页变化:", current, "当前值:", this.pagination.current);
      this.pagination.current = current;
      this.loadData();
    },
    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return "-";
      const date = new Date(dateTime);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      const seconds = String(date.getSeconds()).padStart(2, "0");
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
  },
};
</script>

<style scoped>
.admin-category-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px; /* 按钮间距 */
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.search-section {
  background: #fff;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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

.batch-actions {
  margin-top: 20px;
  padding: 10px 0;
}

.pagination-section {
  margin-top: 20px;
  text-align: right;
}
</style>
