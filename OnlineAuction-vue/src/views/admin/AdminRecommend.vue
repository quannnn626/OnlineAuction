<template>
  <div class="admin-recommend-page">
    <div class="page-header">
      <h2>推荐位管理</h2>
    </div>
    <el-tabs v-model="activeTab" @tab-click="onTabClick">
      <el-tab-pane label="首页推荐" name="home">
        <div class="toolbar">
          <el-input v-model="addGoodsId" placeholder="商品ID" style="width:120px; margin-right:8px" />
          <el-input-number v-model="addSortOrder" :min="0" style="width:100px; margin-right:8px" placeholder="排序" />
          <el-button type="primary" size="small" @click="addRecommend">添加</el-button>
        </div>
        <el-table v-loading="loading" :data="recommendList" stripe size="small">
          <el-table-column prop="goodsId" label="商品ID" width="100" />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="openSort(scope.row)">改排序</el-button>
              <el-button type="text" size="mini" @click="removeRec(scope.row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="分类推荐" name="category">
        <div class="toolbar">
          <el-select v-model="categoryId" placeholder="选择分类" clearable filterable style="width:200px; margin-right:8px" @change="loadCategoryRecommend">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
          <template v-if="categoryId">
            <el-input v-model="addGoodsId" placeholder="商品ID" style="width:120px; margin-right:8px" />
            <el-input-number v-model="addSortOrder" :min="0" style="width:100px; margin-right:8px" />
            <el-button type="primary" size="small" @click="addRecommend">添加</el-button>
          </template>
        </div>
        <el-table v-loading="loading" :data="recommendList" stripe size="small">
          <el-table-column prop="goodsId" label="商品ID" width="100" />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="openSort(scope.row)">改排序</el-button>
              <el-button type="text" size="mini" @click="removeRec(scope.row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <el-dialog title="修改排序" :visible.sync="sortVisible" width="360px">
      <el-form-item label="排序值">
        <el-input-number v-model="editSortOrder" :min="0" />
      </el-form-item>
      <span slot="footer">
        <el-button @click="sortVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSort">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getRecommendList, addRecommend as addRecommendApi, updateRecommendSort, removeRecommend } from "@/api/recommend";
import { getCategoryList } from "@/api/category";

export default {
  name: "AdminRecommend",
  data() {
    return {
      activeTab: "home",
      loading: false,
      recommendList: [],
      categoryOptions: [],
      categoryId: null,
      addGoodsId: "",
      addSortOrder: 0,
      sortVisible: false,
      editRow: null,
      editSortOrder: 0,
    };
  },
  mounted() {
    this.loadCategoryOptions();
    this.loadList();
  },
  methods: {
    onTabClick() {
      this.recommendList = [];
      if (this.activeTab === "home") this.loadList();
      else if (this.categoryId) this.loadCategoryRecommend();
    },
    async loadCategoryOptions() {
      try {
        const list = await getCategoryList();
        this.categoryOptions = list || [];
      } catch (e) {
        this.categoryOptions = [];
      }
    },
    async loadList() {
      this.loading = true;
      try {
        this.recommendList = await getRecommendList("home", 0);
      } catch (e) {
        this.$message.error("加载失败");
      } finally {
        this.loading = false;
      }
    },
    async loadCategoryRecommend() {
      if (!this.categoryId) {
        this.recommendList = [];
        return;
      }
      this.loading = true;
      try {
        this.recommendList = await getRecommendList("category", this.categoryId);
      } catch (e) {
        this.$message.error("加载失败");
      } finally {
        this.loading = false;
      }
    },
    async addRecommend() {
      const goodsId = this.addGoodsId && this.addGoodsId.trim() ? parseInt(this.addGoodsId.trim(), 10) : null;
      if (!goodsId) {
        this.$message.warning("请输入商品ID");
        return;
      }
      const data = {
        recommendType: this.activeTab,
        goodsId,
        sortOrder: this.addSortOrder,
      };
      if (this.activeTab === "category") {
        if (!this.categoryId) {
          this.$message.warning("请先选择分类");
          return;
        }
        data.targetId = this.categoryId;
      } else {
        data.targetId = 0;
      }
      try {
        await addRecommendApi(data);
        this.$message.success("添加成功");
        this.addGoodsId = "";
        if (this.activeTab === "home") this.loadList();
        else this.loadCategoryRecommend();
      } catch (e) {
        this.$message.error(e.message || "添加失败");
      }
    },
    openSort(row) {
      this.editRow = row;
      this.editSortOrder = row.sortOrder;
      this.sortVisible = true;
    },
    async submitSort() {
      if (!this.editRow) return;
      try {
        await updateRecommendSort(this.editRow.id, this.editSortOrder);
        this.$message.success("已更新");
        this.sortVisible = false;
        if (this.activeTab === "home") this.loadList();
        else this.loadCategoryRecommend();
      } catch (e) {
        this.$message.error(e.message || "更新失败");
      }
    },
    async removeRec(row) {
      try {
        await this.$confirm("确定移除该推荐？", "提示", { type: "warning" });
        await removeRecommend(row.id);
        this.$message.success("已移除");
        if (this.activeTab === "home") this.loadList();
        else this.loadCategoryRecommend();
      } catch (e) {
        if (e !== "cancel") this.$message.error(e.message || "移除失败");
      }
    },
  },
};
</script>

<style scoped>
.admin-recommend-page { padding: 20px; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.toolbar { margin-bottom: 16px; }
</style>
