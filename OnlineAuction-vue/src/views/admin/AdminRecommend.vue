<template>
  <div class="admin-recommend-page">
    <div class="page-header">
      <h2>推荐位管理</h2>
    </div>
    <el-tabs v-model="activeTab" @tab-click="onTabClick">
      <el-tab-pane label="首页推荐" name="home">
        <div class="toolbar">
          <el-input
            v-model="homeKeyword"
            placeholder="按商品名搜索"
            style="width:240px; margin-right:8px"
            clearable
            @keyup.enter.native="openHomeGoodsPicker"
          />
          <el-select
            v-model="homeCategoryId"
            placeholder="选择商品分类（自动弹出）"
            clearable
            filterable
            style="width:220px; margin-right:8px"
            @change="handleHomeCategoryChange"
          >
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
          <el-input-number v-model="addSortOrder" :min="0" style="width:100px; margin-right:8px" placeholder="排序" />
          <el-button type="primary" size="small" @click="openHomeGoodsPicker">
            搜索并选择
          </el-button>
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
            <el-input
              v-model="categoryKeyword"
              placeholder="按商品名搜索"
              style="width:200px; margin-right:8px"
              clearable
              @keyup.enter.native="openCategoryGoodsPicker"
            />
            <el-input-number v-model="addSortOrder" :min="0" style="width:100px; margin-right:8px" />
            <el-button type="primary" size="small" @click="openCategoryGoodsPicker">搜索并选择</el-button>
            <span style="margin-left:12px; color:#909399; font-size:12px">或</span>
            <el-input v-model="addGoodsId" placeholder="商品ID" style="width:120px; margin-left:8px; margin-right:8px" />
            <el-button size="small" @click="addRecommend">按ID添加</el-button>
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

    <el-dialog title="选择商品" :visible.sync="homeGoodsPickerVisible" width="900px" @close="onHomePickerClose">
      <el-table
        v-loading="homeGoodsPickerLoading"
        :data="homeGoodsPickerList"
        stripe
        size="small"
        style="width:100%"
      >
        <el-table-column prop="id" label="商品ID" width="90" />
        <el-table-column prop="goodsName" label="商品名称" min-width="240" show-overflow-tooltip />
        <el-table-column prop="basePrice" label="起拍价" width="120" />
        <el-table-column prop="goodsStatus" label="状态" width="120">
          <template slot-scope="scope">
            {{ getGoodsStatusText(scope.row.goodsStatus) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="selectHomeGoods(scope.row)">
              选择并添加
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          :current-page="homeGoodsPickerPagination.current"
          :page-size="homeGoodsPickerPagination.size"
          :total="homeGoodsPickerPagination.total"
          layout="prev, pager, next"
          small
          @current-change="onHomePickerPageChange"
        />
      </div>
    </el-dialog>

    <el-dialog title="选择商品（当前分类下搜索）" :visible.sync="categoryGoodsPickerVisible" width="900px" @open="loadCategoryGoodsPicker">
      <el-table
        v-loading="categoryGoodsPickerLoading"
        :data="categoryGoodsPickerList"
        stripe
        size="small"
        style="width:100%"
      >
        <el-table-column prop="id" label="商品ID" width="90" />
        <el-table-column prop="goodsName" label="商品名称" min-width="240" show-overflow-tooltip />
        <el-table-column prop="basePrice" label="起拍价" width="120" />
        <el-table-column prop="goodsStatus" label="状态" width="120">
          <template slot-scope="scope">
            {{ getGoodsStatusText(scope.row.goodsStatus) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="selectCategoryGoods(scope.row)">
              选择并添加
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          :current-page="categoryGoodsPickerPagination.current"
          :page-size="categoryGoodsPickerPagination.size"
          :total="categoryGoodsPickerPagination.total"
          layout="prev, pager, next"
          small
          @current-change="onCategoryPickerPageChange"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getRecommendList, addRecommend as addRecommendApi, updateRecommendSort, removeRecommend } from "@/api/recommend";
import { getCategoryList } from "@/api/category";
import { getGoodsList } from "@/api/goods";

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

      // 首页推荐：搜索/选品
      homeKeyword: "",
      homeCategoryId: null,
      homeGoodsPickerVisible: false,
      homeGoodsPickerLoading: false,
      homeGoodsPickerList: [],
      homeGoodsPickerPagination: { current: 1, size: 10, total: 0 },

      // 分类推荐：选择分类后按关键词搜索选品
      categoryKeyword: "",
      categoryGoodsPickerVisible: false,
      categoryGoodsPickerLoading: false,
      categoryGoodsPickerList: [],
      categoryGoodsPickerPagination: { current: 1, size: 10, total: 0 },
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

    // 首页推荐：分类改变后自动弹出商品列表
    handleHomeCategoryChange() {
      // 有选择分类就自动弹出；清空分类则不强制弹出
      if (this.homeCategoryId) {
        this.openHomeGoodsPicker();
      }
    },

    async openHomeGoodsPicker() {
      this.homeGoodsPickerVisible = true;
      this.homeGoodsPickerPagination.current = 1;
      await this.loadHomeGoodsPicker();
    },

    onHomePickerClose() {
      // 关闭后不强制清空条件，方便用户继续操作
    },

    async onHomePickerPageChange(p) {
      this.homeGoodsPickerPagination.current = p;
      await this.loadHomeGoodsPicker();
    },

    async loadHomeGoodsPicker() {
      this.homeGoodsPickerLoading = true;
      try {
        const params = {
          current: this.homeGoodsPickerPagination.current,
          size: this.homeGoodsPickerPagination.size,
          keyword: this.homeKeyword || undefined,
          categoryId: this.homeCategoryId || undefined,
        };
        const res = await getGoodsList(params);
        this.homeGoodsPickerList = (res && (res.list || res.records)) || [];
        this.homeGoodsPickerPagination.total = (res && res.total) || 0;
      } catch (e) {
        this.homeGoodsPickerList = [];
        this.homeGoodsPickerPagination.total = 0;
      } finally {
        this.homeGoodsPickerLoading = false;
      }
    },

    async selectHomeGoods(row) {
      if (!row || !row.id) return;
      try {
        const data = {
          recommendType: "home",
          targetId: 0,
          goodsId: row.id,
          sortOrder: this.addSortOrder,
        };
        await addRecommendApi(data);
        this.$message.success("添加成功");
        this.homeGoodsPickerVisible = false;
        this.loadList();
      } catch (e) {
        this.$message.error(e.message || "添加失败");
      }
    },

    // 分类推荐：选择分类后搜索并选品
    openCategoryGoodsPicker() {
      if (!this.categoryId) {
        this.$message.warning("请先选择分类");
        return;
      }
      this.categoryGoodsPickerVisible = true;
      this.categoryGoodsPickerPagination.current = 1;
    },

    async loadCategoryGoodsPicker() {
      if (!this.categoryId) return;
      this.categoryGoodsPickerLoading = true;
      try {
        const params = {
          current: this.categoryGoodsPickerPagination.current,
          size: this.categoryGoodsPickerPagination.size,
          keyword: this.categoryKeyword || undefined,
          categoryId: String(this.categoryId),
        };
        const res = await getGoodsList(params);
        this.categoryGoodsPickerList = (res && (res.list || res.records)) || [];
        this.categoryGoodsPickerPagination.total = (res && res.total) || 0;
      } catch (e) {
        this.categoryGoodsPickerList = [];
        this.categoryGoodsPickerPagination.total = 0;
      } finally {
        this.categoryGoodsPickerLoading = false;
      }
    },

    onCategoryPickerPageChange(p) {
      this.categoryGoodsPickerPagination.current = p;
      this.loadCategoryGoodsPicker();
    },

    async selectCategoryGoods(row) {
      if (!row || !row.id) return;
      try {
        const data = {
          recommendType: "category",
          targetId: this.categoryId,
          goodsId: row.id,
          sortOrder: this.addSortOrder,
        };
        await addRecommendApi(data);
        this.$message.success("添加成功");
        await this.loadCategoryRecommend();
        await this.loadCategoryGoodsPicker();
      } catch (e) {
        this.$message.error(e.message || "添加失败");
      }
    },

    getGoodsStatusText(status) {
      const m = { 0: "未开始", 1: "竞拍中", 2: "已成交", 3: "已流拍" };
      return m[status] || "未知";
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
.pagination-wrap { margin-top: 14px; text-align: right; }
</style>
