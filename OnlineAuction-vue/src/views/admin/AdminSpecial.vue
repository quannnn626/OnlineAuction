<template>
  <div class="admin-special-page">
    <div class="page-header">
      <h2>专场管理</h2>
      <el-button type="primary" size="small" icon="el-icon-plus" @click="openAdd">新增专场</el-button>
    </div>
    <el-table v-loading="loading" :data="specialList" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="specialName" label="专场名称" min-width="140" />
      <el-table-column prop="specialDesc" label="描述" min-width="160" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template slot-scope="scope">{{ scope.row.status === 1 ? '启用' : '禁用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="openGoods(scope.row)">商品管理</el-button>
          <el-button type="text" size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" size="small" @click="delSpecial(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="isAdd ? '新增专场' : '编辑专场'" :visible.sync="formVisible" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="专场名称" required>
          <el-input v-model="form.specialName" placeholder="请输入专场名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.specialDesc" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="'专场商品 - ' + (currentSpecial && currentSpecial.specialName)" :visible.sync="goodsVisible" width="640px">
      <div class="goods-toolbar">
        <el-input v-model="addGoodsId" placeholder="商品ID" style="width:120px; margin-right:8px" />
        <el-button type="primary" size="small" @click="addGoodsToSpecial">加入专场</el-button>
      </div>
      <el-table v-loading="goodsLoading" :data="specialGoodsList" stripe size="small">
        <el-table-column prop="goodsId" label="商品ID" width="90" />
        <el-table-column prop="goodsName" label="商品名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="removeGoods(scope.row)">移出</el-button>
          </template>
        </el-table-column>
      </el-table>
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

export default {
  name: "AdminSpecial",
  data() {
    return {
      loading: false,
      specialList: [],
      formVisible: false,
      isAdd: true,
      submitLoading: false,
      form: { id: null, specialName: "", specialDesc: "", sortOrder: 0, status: 1 },
      goodsVisible: false,
      goodsLoading: false,
      currentSpecial: null,
      specialGoodsList: [],
      addGoodsId: "",
    };
  },
  mounted() {
    this.loadList();
  },
  methods: {
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
      this.form = { id: null, specialName: "", specialDesc: "", sortOrder: 0, status: 1 };
      this.formVisible = true;
    },
    openEdit(row) {
      this.isAdd = false;
      this.form = {
        id: row.id,
        specialName: row.specialName,
        specialDesc: row.specialDesc || "",
        sortOrder: row.sortOrder,
        status: row.status,
      };
      this.formVisible = true;
    },
    async submitForm() {
      if (!this.form.specialName || !this.form.specialName.trim()) {
        this.$message.warning("请输入专场名称");
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
      this.addGoodsId = "";
      this.goodsVisible = true;
      this.loadSpecialGoods();
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
    async addGoodsToSpecial() {
      const goodsId = this.addGoodsId && this.addGoodsId.trim() ? parseInt(this.addGoodsId.trim(), 10) : null;
      if (!goodsId || !this.currentSpecial) {
        this.$message.warning("请输入商品ID");
        return;
      }
      try {
        await addSpecialGoods(this.currentSpecial.id, goodsId);
        this.$message.success("已加入专场");
        this.addGoodsId = "";
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
.goods-toolbar { margin-bottom: 12px; }
</style>
