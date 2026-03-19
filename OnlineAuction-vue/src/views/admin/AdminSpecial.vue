<template>
  <div class="admin-special-page">
    <div class="page-header">
      <h2>专场管理</h2>
      <el-button type="primary" size="small" icon="el-icon-plus" @click="openAdd">新增专场</el-button>
    </div>
    <el-table v-loading="loading" :data="specialList" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="specialName" label="专场名称" min-width="140" />
      <el-table-column prop="specialDesc" label="描述" min-width="140" show-overflow-tooltip />
      <el-table-column label="商品分类" min-width="120">
        <template slot-scope="scope">
          {{ getCategoryName(scope.row.categoryId) || "—" }}
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="上架状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
            {{ scope.row.status === 1 ? "已上架" : "未上架" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="openGoods(scope.row)">商品管理</el-button>
          <el-button type="text" size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" size="small" @click="delSpecial(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="isAdd ? '新增专场' : '编辑专场'" :visible.sync="formVisible" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="专场名称" required>
          <el-input v-model="form.specialName" placeholder="请输入专场名称" />
        </el-form-item>
        <el-form-item label="商品分类" required>
          <el-popover
            placement="bottom-start"
            width="420"
            trigger="click"
            v-model="categoryTreeVisible"
          >
            <el-tree
              :data="categoryTreeData"
              :props="categoryTreeProps"
              node-key="id"
              highlight-current
              :current-node-key="form.categoryId"
              :expand-on-click-node="false"
              style="max-height: 300px; overflow-y: auto"
              @node-click="handleCategoryNodeClick"
            />
            <el-input
              slot="reference"
              v-model="selectedCategoryNames"
              placeholder="请选择商品分类（点击展开分层级选择）"
              readonly
              style="cursor: pointer"
            >
              <i
                slot="prefix"
                class="el-input__icon el-icon-arrow-down"
                :class="{ 'is-reverse': categoryTreeVisible }"
              ></i>
            </el-input>
          </el-popover>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.specialDesc" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="上架状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">已上架（用户可见）</el-radio>
            <el-radio :label="0">未上架（用户不可见）</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="'专场商品 - ' + (currentSpecial && currentSpecial.specialName)" :visible.sync="goodsVisible" width="900px">
      <div v-if="!currentSpecial || currentSpecial.categoryId == null" class="goods-tip">
        <el-alert type="warning" :closable="false" show-icon>
          请先在「编辑」中为该专场选择商品分类，再在此处添加该分类下的商品。
        </el-alert>
      </div>
      <template v-else>
        <div class="goods-section">
          <div class="section-title">可选商品（分类：{{ getCategoryName(currentSpecial.categoryId) }}）</div>
          <el-table v-loading="categoryGoodsLoading" :data="categoryGoodsList" stripe size="small" max-height="240">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="goodsName" label="商品名称" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button
                  type="text"
                  size="mini"
                  :disabled="isInSpecial(scope.row.id)"
                  @click="addGoodsToSpecial(scope.row.id)"
                >
                  {{ isInSpecial(scope.row.id) ? "已加入" : "加入专场" }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-if="categoryGoodsTotal > categoryGoodsPageSize"
            small
            :current-page="categoryGoodsPage"
            :page-size="categoryGoodsPageSize"
            :total="categoryGoodsTotal"
            layout="prev, pager, next"
            @current-change="onCategoryGoodsPageChange"
          />
        </div>
        <div class="goods-section">
          <div class="section-title">已加入专场的商品</div>
          <el-table v-loading="goodsLoading" :data="specialGoodsList" stripe size="small" max-height="220">
            <el-table-column prop="goodsId" label="商品ID" width="90" />
            <el-table-column prop="goodsName" label="商品名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button type="text" size="mini" @click="removeGoods(scope.row)">移出</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  getSpecialList,
  getSpecialGoods,
  addSpecial,
  updateSpecial,
  deleteSpecial,
  addSpecialGoods,
  removeSpecialGoods,
} from "@/api/special";
import { getCategoryTree } from "@/api/category";
import { getGoodsPage } from "@/api/goods";

export default {
  name: "AdminSpecial",
  data() {
    return {
      loading: false,
      specialList: [],
      categoryTreeData: [],
      categoryTreeVisible: false,
      categoryTreeProps: {
        children: "children",
        label: "categoryName",
      },
      categoryMap: {},
      selectedCategoryNames: "",
      formVisible: false,
      isAdd: true,
      submitLoading: false,
      form: { id: null, specialName: "", specialDesc: "", categoryId: null, sortOrder: 0, status: 0 },
      goodsVisible: false,
      goodsLoading: false,
      currentSpecial: null,
      specialGoodsList: [],
      categoryGoodsLoading: false,
      categoryGoodsList: [],
      categoryGoodsPage: 1,
      categoryGoodsPageSize: 10,
      categoryGoodsTotal: 0,
    };
  },
  mounted() {
    this.loadList();
    this.loadCategoryTreeData();
  },
  methods: {
    getCategoryName(id) {
      if (id == null) return "";
      return this.categoryMap[id] || String(id);
    },
    buildCategoryMap(treeData) {
      const map = {};
      const flatten = (nodes) => {
        if (!nodes || !Array.isArray(nodes)) return;
        nodes.forEach((node) => {
          map[node.id] = node.categoryName;
          if (node.children && node.children.length > 0) {
            flatten(node.children);
          }
        });
      };
      flatten(treeData);
      this.categoryMap = map;
    },
    async loadCategoryTreeData() {
      try {
        const list = await getCategoryTree(true); // 包含禁用
        this.categoryTreeData = Array.isArray(list) ? list : [];
        this.buildCategoryMap(this.categoryTreeData);
        if (this.formVisible && this.form.categoryId != null) {
          this.selectedCategoryNames = this.getCategoryName(this.form.categoryId);
        }
      } catch (e) {
        this.categoryTreeData = [];
        this.categoryMap = {};
      }
    },
    handleCategoryNodeClick(data) {
      if (!data || data.id == null) return;
      this.form.categoryId = data.id;
      this.selectedCategoryNames = data.categoryName || this.getCategoryName(data.id);
      // 单选：选择后关闭弹层
      this.categoryTreeVisible = false;
    },
    async loadList() {
      this.loading = true;
      try {
        this.specialList = await getSpecialList();
      } catch (e) {
        this.$message.error("加载失败");
      } finally {
        this.loading = false;
      }
    },
    openAdd() {
      this.isAdd = true;
      this.form = {
        id: null,
        specialName: "",
        specialDesc: "",
        categoryId: null,
        sortOrder: 0,
        status: 0,
      };
      this.selectedCategoryNames = "";
      this.formVisible = true;
    },
    openEdit(row) {
      this.isAdd = false;
      this.form = {
        id: row.id,
        specialName: row.specialName,
        specialDesc: row.specialDesc || "",
        categoryId: row.categoryId != null ? Number(row.categoryId) : null,
        sortOrder: row.sortOrder,
        status: row.status,
      };
      this.selectedCategoryNames = this.getCategoryName(this.form.categoryId);
      this.formVisible = true;
    },
    async submitForm() {
      if (!this.form.specialName || !this.form.specialName.trim()) {
        this.$message.warning("请输入专场名称");
        return;
      }
      if (this.form.categoryId == null || this.form.categoryId === "") {
        this.$message.warning("请选择商品分类");
        return;
      }
      this.submitLoading = true;
      try {
        if (this.isAdd) {
          await addSpecial(this.form);
          this.$message.success("新增成功");
        } else {
          await updateSpecial(this.form.id, this.form);
          this.$message.success("更新成功");
        }
        this.formVisible = false;
        this.loadList();
      } catch (e) {
        this.$message.error(e.message || "操作失败");
      } finally {
        this.submitLoading = false;
      }
    },
    async delSpecial(row) {
      try {
        await this.$confirm("确定删除该专场？", "提示", { type: "warning" });
        await deleteSpecial(row.id);
        this.$message.success("已删除");
        this.loadList();
      } catch (e) {
        if (e !== "cancel") this.$message.error(e.message || "删除失败");
      }
    },
    async openGoods(row) {
      this.currentSpecial = row;
      this.goodsVisible = true;
      this.loadSpecialGoods();
      if (row.categoryId != null) {
        this.categoryGoodsPage = 1;
        this.loadCategoryGoods();
      } else {
        this.categoryGoodsList = [];
        this.categoryGoodsTotal = 0;
      }
    },
    isInSpecial(goodsId) {
      return this.specialGoodsList.some((g) => g.goodsId === goodsId);
    },
    async loadCategoryGoods() {
      if (!this.currentSpecial || this.currentSpecial.categoryId == null) return;
      this.categoryGoodsLoading = true;
      try {
        const res = await getGoodsPage({
          current: this.categoryGoodsPage,
          size: this.categoryGoodsPageSize,
          categoryId: String(this.currentSpecial.categoryId),
        });
        this.categoryGoodsList = (res && res.list) || [];
        this.categoryGoodsTotal = (res && res.total) || 0;
      } catch (e) {
        this.categoryGoodsList = [];
        this.categoryGoodsTotal = 0;
      } finally {
        this.categoryGoodsLoading = false;
      }
    },
    onCategoryGoodsPageChange(p) {
      this.categoryGoodsPage = p;
      this.loadCategoryGoods();
    },
    async loadSpecialGoods() {
      if (!this.currentSpecial) return;
      this.goodsLoading = true;
      try {
        this.specialGoodsList = await getSpecialGoods(this.currentSpecial.id);
      } catch (e) {
        this.$message.error("加载专场商品失败");
      } finally {
        this.goodsLoading = false;
      }
    },
    async addGoodsToSpecial(goodsId) {
      if (!this.currentSpecial || !goodsId) return;
      try {
        await addSpecialGoods(this.currentSpecial.id, goodsId);
        this.$message.success("已加入专场");
        this.loadSpecialGoods();
      } catch (e) {
        this.$message.error(e.message || "添加失败");
      }
    },
    async removeGoods(row) {
      try {
        await this.$confirm("确定移出该商品？", "提示", { type: "warning" });
        await removeSpecialGoods(this.currentSpecial.id, row.goodsId);
        this.$message.success("已移出");
        this.loadSpecialGoods();
      } catch (e) {
        if (e !== "cancel") this.$message.error(e.message || "移出失败");
      }
    },
  },
};
</script>

<style scoped>
.admin-special-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.goods-tip { margin-bottom: 12px; }
.goods-section { margin-bottom: 16px; }
.section-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; color: #303133; }
.el-pagination { margin-top: 8px; }
</style>
