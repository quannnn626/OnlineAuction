<template>
  <div class="admin-category-page">
    <div class="page-header">
      <h2>拍卖商品类目管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          新增一级分类
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

    <!-- 树形控件区域 -->
    <div class="tree-section">
      <el-tree
        ref="categoryTree"
        :key="treeKey"
        :data="displayTreeData"
        :props="treeProps"
        node-key="id"
        :expand-on-click-node="false"
        :default-expanded-keys="expandedKeys"
        v-loading="loading"
        class="category-tree"
      >
        <span class="custom-tree-node" slot-scope="{ node, data }">
          <span class="node-label">
            <el-tag
              :type="getLevelTagType(data.level)"
              size="mini"
              style="margin-right: 8px"
            >
              {{ getLevelText(data.level) }}
            </el-tag>
            <span class="category-name">{{ data.categoryName }}</span>
            <el-tag
              v-if="data.categoryStatus === 0"
              type="info"
              size="mini"
              style="margin-left: 8px"
            >
              已禁用
            </el-tag>
            <span class="category-sort" style="margin-left: 8px; color: #909399">
              排序: {{ data.categorySort || 0 }}
            </span>
          </span>
          <span class="node-actions">
            <el-button
              type="text"
              size="mini"
              icon="el-icon-plus"
              @click="handleAddChild(data)"
              :disabled="data.level >= 3"
              :title="data.level >= 3 ? '最多只能有三级分类' : '添加子分类'"
            >
              添加
            </el-button>
            <el-button
              type="text"
              size="mini"
              icon="el-icon-edit"
              @click="handleEdit(data)"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              size="mini"
              icon="el-icon-delete"
              style="color: #f56c6c"
              @click="handleDelete(data)"
            >
              删除
            </el-button>
          </span>
        </span>
      </el-tree>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        :model="formData"
        :rules="formRules"
        ref="categoryForm"
        label-width="100px"
      >
        <el-form-item label="父分类" prop="parentId" v-if="formData.level > 1">
          <el-cascader
            v-model="parentIdPath"
            :options="categoryTreeOptions"
            :props="cascaderProps"
            clearable
            :disabled="isEdit && formData.level === 3"
            placeholder="请选择父分类（不选则为一级分类）"
            style="width: 100%"
            @change="handleParentChange"
          ></el-cascader>
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            提示：最多只能有三级分类，三级分类不能再添加子分类
            <span v-if="isEdit && formData.level === 3" style="color: #f56c6c">
              （三级分类不能修改父分类）
            </span>
          </div>
        </el-form-item>
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
        <el-form-item label="状态" prop="categoryStatus">
          <el-switch
            v-model="formData.categoryStatus"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          ></el-switch>
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
  getCategoryTree,
  addCategory,
  updateCategory,
  deleteCategory,
} from "@/api/category";

export default {
  name: "AdminCategory",
  data() {
    return {
      loading: false,
      submitLoading: false,
      treeData: [],
      displayTreeData: [], // 展示的树数据（搜索时可能过滤）
      expandedKeys: [], // 搜索后需展开的节点 ID 列表
      treeKey: 0,
      treeProps: {
        children: "children",
        label: "categoryName",
      },
      searchForm: {
        categoryName: "",
      },
      dialogVisible: false,
      dialogTitle: "新增分类",
      isEdit: false,
      formData: {
        id: null,
        parentId: 0,
        level: 1,
        categoryName: "",
        categorySort: 0,
        categoryStatus: 1,
      },
      parentIdPath: [], // 级联选择器的路径
      categoryTreeOptions: [], // 用于级联选择器的树形数据
      cascaderProps: {
        value: "id",
        label: "categoryName",
        children: "children",
        checkStrictly: true, // 允许选择任意一级
        emitPath: true, // 返回完整路径
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
        const result = await getCategoryTree(true);
        if (result && Array.isArray(result)) {
          this.treeData = result;
          await this.loadCategoryTreeOptions();
          this.applySearchFilter();
        } else {
          this.treeData = [];
          this.displayTreeData = [];
        }
      } catch (error) {
        this.$message.error("加载数据失败");
        this.treeData = [];
        this.displayTreeData = [];
      } finally {
        this.loading = false;
      }
    },
    // 根据搜索关键词过滤树并计算需展开的节点
    applySearchFilter() {
      const keyword = (this.searchForm.categoryName || "").trim();
      if (!keyword) {
        this.displayTreeData = this.treeData;
        this.expandedKeys = [];
        this.treeKey += 1;
        return;
      }
      const kw = keyword.toLowerCase();
      const matchIds = [];
      const findMatches = (nodes) => {
        if (!nodes || !nodes.length) return;
        for (const node of nodes) {
          if (node.categoryName && node.categoryName.toLowerCase().includes(kw)) {
            matchIds.push(node.id);
          }
          findMatches(node.children);
        }
      };
      findMatches(this.treeData);

      const ancestorIds = new Set();
      const getAncestors = (nodes, targetId, path = []) => {
        if (!nodes || !nodes.length) return false;
        for (const node of nodes) {
          if (node.id === targetId) {
            path.forEach((id) => ancestorIds.add(id));
            return true;
          }
          if (getAncestors(node.children || [], targetId, [...path, node.id])) {
            return true;
          }
        }
        return false;
      };
      matchIds.forEach((id) => getAncestors(this.treeData, id));
      matchIds.forEach((id) => ancestorIds.add(id));

      const filterByKeyword = (nodes) => {
        if (!nodes || !nodes.length) return [];
        return nodes
          .map((node) => {
            const isMatch = node.categoryName && node.categoryName.toLowerCase().includes(kw);
            const filteredChildren = filterByKeyword(node.children);
            const hasMatchInChildren = filteredChildren.length > 0;
            if (!isMatch && !hasMatchInChildren) return null;
            const newNode = { ...node };
            newNode.children = isMatch ? (node.children || []) : filteredChildren;
            return newNode;
          })
          .filter(Boolean);
      };

      const filtered = filterByKeyword(this.treeData);
      this.displayTreeData = filtered.length > 0 ? filtered : this.treeData;
      this.expandedKeys = [...ancestorIds];
      this.treeKey += 1;
    },
    // 加载用于级联选择器的分类树（包含所有分类，用于选择父分类）
    async loadCategoryTreeOptions() {
      try {
        // 获取所有分类（包括禁用的），用于选择父分类
        // request.js 的响应拦截器已经返回了 res.data，所以这里 result 就是树形数据数组
        const result = await getCategoryTree(true);
        if (result && Array.isArray(result)) {
          // 过滤掉三级分类，因为三级分类不能再有子分类
          this.categoryTreeOptions = this.filterTreeForCascader(result);
        }
      } catch (error) {
        // 忽略
      }
    },
    // 过滤树形数据，移除三级分类（因为三级分类不能再有子分类）
    filterTreeForCascader(tree) {
      return tree
        .filter((item) => item.level < 3)
        .map((item) => {
          const newNode = { ...item };
          if (newNode.children && newNode.children.length > 0) {
            newNode.children = this.filterTreeForCascader(newNode.children);
          }
          return newNode;
        });
    },
    // 搜索
    handleSearch() {
      // 树形结构搜索，可以展开匹配的节点
      this.loadData();
    },
    // 重置
    handleReset() {
      this.searchForm.categoryName = "";
      this.loadData();
    },
    // 新增一级分类
    handleAdd() {
      this.dialogTitle = "新增一级分类";
      this.isEdit = false;
      this.formData = {
        id: null,
        parentId: 0,
        level: 1,
        categoryName: "",
        categorySort: 0,
        categoryStatus: 1,
      };
      this.parentIdPath = [];
      this.dialogVisible = true;
    },
    // 添加子分类
    handleAddChild(parentData) {
      if (parentData.level >= 3) {
        this.$message.warning("最多只能有三级分类，无法继续添加");
        return;
      }
      this.dialogTitle = `新增${this.getLevelText(parentData.level + 1)}分类`;
      this.isEdit = false;
      this.formData = {
        id: null,
        parentId: parentData.id,
        level: parentData.level + 1,
        categoryName: "",
        categorySort: 0,
        categoryStatus: 1,
      };
      // 设置级联选择器的路径
      this.parentIdPath = this.getParentPath(parentData.id);
      this.dialogVisible = true;
    },
    // 编辑
    handleEdit(data) {
      this.dialogTitle = `编辑${this.getLevelText(data.level)}分类`;
      this.isEdit = true;
      this.formData = {
        id: data.id,
        parentId: data.parentId || 0,
        level: data.level,
        categoryName: data.categoryName,
        categorySort: data.categorySort || 0,
        categoryStatus: data.categoryStatus !== undefined ? data.categoryStatus : 1,
      };
      // 设置级联选择器的路径（三级分类也显示，但禁用）
      this.parentIdPath = this.getParentPath(data.parentId || 0);
      this.dialogVisible = true;
    },
    // 获取父分类的路径（用于级联选择器）
    getParentPath(parentId) {
      if (!parentId || parentId === 0) {
        return [];
      }
      // 递归查找父分类路径
      const findPath = (nodes, targetId, path = []) => {
        for (const node of nodes) {
          if (node.id === targetId) {
            return [...path, node.id];
          }
          if (node.children && node.children.length > 0) {
            const result = findPath(node.children, targetId, [...path, node.id]);
            if (result) {
              return result;
            }
          }
        }
        return null;
      };
      return findPath(this.categoryTreeOptions, parentId) || [];
    },
    // 父分类变化处理
    handleParentChange(value) {
      if (value && value.length > 0) {
        // 取最后一个ID作为父分类ID
        this.formData.parentId = value[value.length - 1];
        // 计算层级
        this.formData.level = value.length + 1;
        // 限制最多三级
        if (this.formData.level > 3) {
          this.$message.warning("最多只能有三级分类");
          this.formData.parentId = 0;
          this.formData.level = 1;
          this.parentIdPath = [];
        }
      } else {
        this.formData.parentId = 0;
        this.formData.level = 1;
      }
    },
    // 删除
    handleDelete(data) {
      this.$confirm(
        `确定要删除分类"${data.categoryName}"吗？${data.children && data.children.length > 0 ? '删除后其子分类也将被删除！' : ''}`,
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }
      )
        .then(async () => {
          try {
            await deleteCategory(data.id);
            this.$message.success("删除成功");
            this.loadData();
          } catch (error) {
            this.$message.error(error.response?.data?.message || "删除失败");
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
        
        // 验证层级限制
        if (this.formData.level > 3) {
          this.$message.error("分类层级最多为三级");
          return;
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
          this.$message.error(error.response?.data?.message || "操作失败");
        } finally {
          this.submitLoading = false;
        }
      });
    },
    // 对话框关闭
    handleDialogClose() {
      this.$refs.categoryForm && this.$refs.categoryForm.resetFields();
      this.parentIdPath = [];
    },
    // 获取层级标签类型
    getLevelTagType(level) {
      const types = {
        1: "primary",
        2: "success",
        3: "warning",
      };
      return types[level] || "info";
    },
    // 获取层级文本
    getLevelText(level) {
      const texts = {
        1: "一级",
        2: "二级",
        3: "三级",
      };
      return texts[level] || "未知";
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
  gap: 10px;
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

.tree-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-height: 400px;
}

.category-tree {
  font-size: 14px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.node-label {
  display: flex;
  align-items: center;
  flex: 1;
}

.category-name {
  font-weight: 500;
  color: #303133;
}

.category-sort {
  font-size: 12px;
}

.node-actions {
  display: flex;
  gap: 8px;
}

.node-actions .el-button {
  padding: 0 5px;
}

.node-actions .el-button.is-disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

/* 树节点样式优化 */
.category-tree ::v-deep .el-tree-node__content {
  height: 40px;
  line-height: 40px;
}

.category-tree ::v-deep .el-tree-node__content:hover {
  background-color: #f5f7fa;
}

.category-tree ::v-deep .el-tree-node__expand-icon {
  color: #606266;
}

.category-tree ::v-deep .el-tree-node__expand-icon.is-leaf {
  color: transparent;
  cursor: default;
}
</style>
