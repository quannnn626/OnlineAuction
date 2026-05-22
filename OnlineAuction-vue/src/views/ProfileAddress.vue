<template>
  <div class="address-page">
    <el-card shadow="never">
      <div slot="header">
        <span>地址管理</span>
        <el-button type="primary" size="small" style="float: right" @click="showAddDialog">
          新增地址
        </el-button>
      </div>

      <el-table :data="addressList" style="width: 100%" v-loading="loading">
        <el-table-column prop="receiverName" label="收货人" width="120"></el-table-column>
        <el-table-column prop="receiverPhone" label="联系电话" width="140"></el-table-column>
        <el-table-column label="所在地区" min-width="200">
          <template slot-scope="scope">
            {{ (scope.row.province || "") + (scope.row.city || "") + (scope.row.district || "") }}
          </template>
        </el-table-column>
        <el-table-column prop="detailAddress" label="详细地址" min-width="200"></el-table-column>
        <el-table-column label="默认" width="70">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isDefault === 1" type="success" size="small">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button type="text" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button
              v-if="scope.row.isDefault !== 1"
              type="text"
              @click="handleSetDefault(scope.row.id)"
            >
              设为默认
            </el-button>
            <el-button type="text" style="color: #f56c6c" @click="handleDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && addressList.length === 0" description="暂无地址，请新增"></el-empty>
    </el-card>

    <el-dialog
      :title="isEdit ? '编辑地址' : '新增地址'"
      :visible.sync="dialogVisible"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="addressForm"
        :rules="addressRules"
        ref="addressForm"
        label-width="100px"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" maxlength="20"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入联系电话" maxlength="20"></el-input>
        </el-form-item>
        <el-form-item label="所在地区">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-input v-model="addressForm.province" placeholder="省"></el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="addressForm.city" placeholder="市"></el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="addressForm.district" placeholder="区"></el-input>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input
            v-model="addressForm.detailAddress"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址"
            maxlength="200"
          ></el-input>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAddressList, saveAddress, deleteAddress, setDefaultAddress } from "@/api/address";

export default {
  name: "ProfileAddress",
  data() {
    return {
      addressList: [],
      loading: false,
      saving: false,
      dialogVisible: false,
      isEdit: false,
      editingId: null,
      addressForm: {
        receiverName: "",
        receiverPhone: "",
        province: "",
        city: "",
        district: "",
        detailAddress: "",
        isDefault: 0,
      },
      addressRules: {
        receiverName: [
          { required: true, message: "请输入收货人姓名", trigger: "blur" },
        ],
        receiverPhone: [
          { required: true, message: "请输入联系电话", trigger: "blur" },
        ],
        detailAddress: [
          { required: true, message: "请输入详细地址", trigger: "blur" },
        ],
      },
    };
  },
  mounted() {
    this.loadAddressList();
  },
  methods: {
    async loadAddressList() {
      this.loading = true;
      try {
        const data = await getAddressList();
        this.addressList = data || [];
      } catch (e) {
      } finally {
        this.loading = false;
      }
    },
    resetForm() {
      this.addressForm = {
        receiverName: "",
        receiverPhone: "",
        province: "",
        city: "",
        district: "",
        detailAddress: "",
        isDefault: 0,
      };
      this.editingId = null;
      this.isEdit = false;
    },
    showAddDialog() {
      this.resetForm();
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.addressForm) {
          this.$refs.addressForm.clearValidate();
        }
      });
    },
    showEditDialog(row) {
      this.isEdit = true;
      this.editingId = row.id;
      this.addressForm = {
        receiverName: row.receiverName || "",
        receiverPhone: row.receiverPhone || "",
        province: row.province || "",
        city: row.city || "",
        district: row.district || "",
        detailAddress: row.detailAddress || "",
        isDefault: row.isDefault || 0,
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.addressForm) {
          this.$refs.addressForm.clearValidate();
        }
      });
    },
    handleSave() {
      this.$refs.addressForm.validate(async (valid) => {
        if (!valid) return;
        this.saving = true;
        try {
          const params = { ...this.addressForm };
          if (this.isEdit) {
            params.id = this.editingId;
          }
          await saveAddress(params);
          this.$message.success(this.isEdit ? "地址修改成功" : "地址添加成功");
          this.dialogVisible = false;
          this.loadAddressList();
        } catch (e) {
          this.$message.error(e.message || "保存失败");
        } finally {
          this.saving = false;
        }
      });
    },
    handleDelete(id) {
      this.$confirm("确定删除该地址吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            await deleteAddress(id);
            this.$message.success("删除成功");
            this.loadAddressList();
          } catch (e) {
            this.$message.error(e.message || "删除失败");
          }
        })
        .catch(() => {});
    },
    async handleSetDefault(id) {
      try {
        await setDefaultAddress(id);
        this.$message.success("已设为默认地址");
        this.loadAddressList();
      } catch (e) {
        this.$message.error(e.message || "设置失败");
      }
    },
  },
};
</script>

<style scoped>
.address-page {
  padding: 0;
}
</style>
