<template>
  <div class="profile-page">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="个人信息" name="info">
        <div class="profile-content">
          <el-card shadow="never">
            <div slot="header">
              <span>个人信息</span>
            </div>
            <el-form
              :model="profileForm"
              :rules="profileRules"
              ref="profileForm"
              label-width="100px"
              style="max-width: 600px"
            >
              <el-form-item label="头像">
                <div class="avatar-upload">
                  <el-upload
                    class="avatar-uploader"
                    :action="uploadAction"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :on-error="handleAvatarError"
                    :before-upload="beforeAvatarUpload"
                    :disabled="avatarUploading"
                    :limit="1"
                    name="files"
                  >
                    <img
                      v-if="avatarUrl && !avatarUploading"
                      :src="avatarUrl"
                      class="avatar"
                      alt="头像"
                    />
                    <i v-else-if="!avatarUploading" class="el-icon-plus avatar-uploader-icon"></i>
                    <i v-else class="el-icon-loading avatar-uploader-icon"></i>
                  </el-upload>
                  <div class="avatar-tip">点击上传头像，支持JPG、PNG格式，大小不超过2MB</div>
                </div>
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="profileForm.userName" disabled></el-input>
              </el-form-item>
              <el-form-item label="昵称" prop="nickName">
                <el-input
                  v-model="profileForm.nickName"
                  placeholder="请输入昵称"
                  maxlength="30"
                ></el-input>
              </el-form-item>
              <el-form-item label="真实姓名" prop="realName">
                <el-input
                  v-model="profileForm.realName"
                  placeholder="请输入真实姓名"
                  maxlength="20"
                ></el-input>
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model="profileForm.phone"
                  placeholder="请输入手机号"
                  maxlength="11"
                ></el-input>
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input
                  v-model="profileForm.email"
                  placeholder="请输入邮箱"
                  maxlength="50"
                ></el-input>
              </el-form-item>
              <el-form-item label="性别" prop="sex">
                <el-radio-group v-model="profileForm.sex">
                  <el-radio label="0">男</el-radio>
                  <el-radio label="1">女</el-radio>
                  <el-radio label="2">未知</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  @click="handleSaveProfile"
                  :loading="profileLoading"
                  >保存</el-button
                >
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="修改密码" name="password">
        <div class="profile-content">
          <el-card shadow="never">
            <div slot="header">
              <span>修改密码</span>
            </div>
            <el-form
              :model="passwordForm"
              :rules="passwordRules"
              ref="passwordForm"
              label-width="100px"
              style="max-width: 600px"
            >
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input
                  type="password"
                  v-model="passwordForm.oldPassword"
                  placeholder="请输入旧密码"
                  show-password
                ></el-input>
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  type="password"
                  v-model="passwordForm.newPassword"
                  placeholder="请输入新密码（至少6位）"
                  show-password
                ></el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  type="password"
                  v-model="passwordForm.confirmPassword"
                  placeholder="请再次输入新密码"
                  show-password
                ></el-input>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  @click="handleChangePassword"
                  :loading="passwordLoading"
                  >修改密码</el-button
                >
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="订单管理" name="order">
        <router-view />
      </el-tab-pane>
      <el-tab-pane label="保证金管理" name="deposit">
        <router-view />
      </el-tab-pane>
      <el-tab-pane label="消息中心" name="message">
        <router-view />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { getCurrentUser } from "@/api/auth";
import { updateProfile, changePassword, updateAvatar } from "@/api/user";

export default {
  name: "Profile",
  data() {
    // 验证手机号
    const validatePhone = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入手机号"));
      } else if (!/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error("请输入正确的手机号"));
      } else {
        callback();
      }
    };
    // 验证邮箱
    const validateEmail = (rule, value, callback) => {
      if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        callback(new Error("请输入正确的邮箱地址"));
      } else {
        callback();
      }
    };
    // 验证确认密码
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请再次输入新密码"));
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };

    return {
      activeTab: "info",
      profileLoading: false,
      passwordLoading: false,
      uploadAction: "/api/OnlineAuction/auctionFile/upload?fileCategory=avatar",
      avatarUrl: "",
      avatarFileId: null,
      avatarUploading: false, // 头像上传状态
      profileForm: {
        userName: "",
        nickName: "",
        realName: "",
        phone: "",
        email: "",
        sex: "2",
      },
      profileRules: {
        nickName: [
          { max: 30, message: "昵称长度不能超过30个字符", trigger: "blur" },
        ],
        realName: [
          { max: 20, message: "真实姓名长度不能超过20个字符", trigger: "blur" },
        ],
        phone: [{ validator: validatePhone, trigger: "blur" }],
        email: [{ validator: validateEmail, trigger: "blur" }],
      },
      passwordForm: {
        oldPassword: "",
        newPassword: "",
        confirmPassword: "",
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: "请输入旧密码", trigger: "blur" },
        ],
        newPassword: [
          { required: true, message: "请输入新密码", trigger: "blur" },
          { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
        ],
        confirmPassword: [
          { validator: validateConfirmPassword, trigger: "blur" },
        ],
      },
    };
  },
  mounted() {
    this.updateActiveTab();
    this.loadUserInfo();
  },
  watch: {
    $route() {
      this.updateActiveTab();
    },
  },
  methods: {
    updateActiveTab() {
      const routeName = this.$route.name;
      if (routeName === "ProfileOrder") {
        this.activeTab = "order";
      } else if (routeName === "ProfileDeposit") {
        this.activeTab = "deposit";
      } else if (routeName === "ProfileMessage") {
        this.activeTab = "message";
      } else {
        this.activeTab = "info";
      }
    },
    handleTabClick(tab) {
      const routeMap = {
        order: "/profile/order",
        deposit: "/profile/deposit",
        message: "/profile/message",
      };
      const targetPath = routeMap[tab.name];
      if (targetPath && this.$route.path !== targetPath) {
        this.$router.push(targetPath).catch((err) => {
          // 忽略重复导航错误（如已在同一路由时再次点击）
          if (err.name !== "NavigationDuplicated") {
            return Promise.reject(err);
          }
        });
      }
    },
    // 加载用户信息
    async loadUserInfo() {
      try {
        const userInfo = await getCurrentUser();
        if (userInfo) {
          this.profileForm.userName = userInfo.userName || "";
          this.profileForm.nickName = userInfo.nickName || "";
          this.profileForm.realName = userInfo.realName || "";
          this.profileForm.phone = userInfo.phone || "";
          this.profileForm.email = userInfo.email || "";
          this.profileForm.sex = userInfo.sex || "2";
          
          // 加载头像
          if (userInfo.avatar) {
            this.avatarUrl = userInfo.avatar;
          }
          if (userInfo.avatarFileId) {
            this.avatarFileId = userInfo.avatarFileId;
          }
        }
      } catch (error) {
        console.error("加载用户信息失败:", error);
      }
    },
    // 头像上传前验证
    beforeAvatarUpload(file) {
      // 防止重复上传
      if (this.avatarUploading) {
        this.$message.warning("头像正在上传中，请稍候...");
        return false;
      }

      const isImage = file.type.startsWith("image/");
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        this.$message.error("只能上传图片文件!");
        return false;
      }
      if (!isLt2M) {
        this.$message.error("头像大小不能超过 2MB!");
        return false;
      }

      // 设置上传状态
      this.avatarUploading = true;
      return true;
    },
    // 头像上传成功
    async handleAvatarSuccess(response, file) {
      this.avatarUploading = false; // 重置上传状态
      
      if (response.code === 200 && response.data) {
        let fileId = null;
        let filePath = null;
        
        // response.data 可能是数组或单个对象
        if (Array.isArray(response.data) && response.data.length > 0) {
          // 只取第一个文件（头像只允许一个）
          fileId = response.data[0].id;
          filePath = response.data[0].filePath;
        } else if (response.data.id) {
          fileId = response.data.id;
          filePath = response.data.filePath;
        }

        if (fileId) {
          try {
            // 如果已有旧头像，先删除旧文件
            if (this.avatarFileId && this.avatarFileId !== fileId) {
              // 注意：这里不删除旧文件，因为可能被其他用户使用
              // 如果需要清理，可以在后端实现定期清理未使用的文件
            }

            // 更新用户头像
            await updateAvatar(fileId);
            this.avatarFileId = fileId;
            this.avatarUrl = filePath;
            this.$message.success("头像上传成功");
            
            // 更新localStorage中的用户信息
            const userInfo = JSON.parse(localStorage.getItem("userInfo") || "{}");
            userInfo.avatar = this.avatarUrl;
            userInfo.avatarFileId = fileId;
            localStorage.setItem("userInfo", JSON.stringify(userInfo));
          } catch (error) {
            console.error("更新头像失败:", error);
            this.$message.error("头像更新失败：" + (error.message || "未知错误"));
            // 上传失败，恢复原来的头像
            if (this.avatarFileId) {
              // 可以重新加载用户信息来恢复头像
            }
          }
        } else {
          this.$message.error("头像上传失败：未获取到文件ID");
        }
      } else {
        this.$message.error("头像上传失败：" + (response.message || "未知错误"));
      }
    },
    // 头像上传失败
    handleAvatarError(error, file) {
      this.avatarUploading = false; // 重置上传状态
      console.error("头像上传失败:", error);
      this.$message.error("头像上传失败，请重试");
    },
    // 保存个人信息
    handleSaveProfile() {
      this.$refs.profileForm.validate(async (valid) => {
        if (valid) {
          this.profileLoading = true;
          try {
            await updateProfile(this.profileForm);
            this.$message.success("保存成功");
            
            // 更新localStorage中的用户信息
            const userInfo = JSON.parse(localStorage.getItem("userInfo") || "{}");
            Object.assign(userInfo, this.profileForm);
            localStorage.setItem("userInfo", JSON.stringify(userInfo));
          } catch (error) {
            console.error("保存失败:", error);
            this.$message.error("保存失败：" + (error.message || "未知错误"));
          } finally {
            this.profileLoading = false;
          }
        }
      });
    },
    // 修改密码
    handleChangePassword() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (valid) {
          this.passwordLoading = true;
          try {
            await changePassword(
              this.passwordForm.oldPassword,
              this.passwordForm.newPassword
            );
            this.$message.success("密码修改成功，请重新登录");
            
            // 清空表单
            this.passwordForm = {
              oldPassword: "",
              newPassword: "",
              confirmPassword: "",
            };
            this.$refs.passwordForm.resetFields();
            
            // 延迟跳转到登录页
            setTimeout(() => {
              this.$router.push("/login");
            }, 1500);
          } catch (error) {
            console.error("修改密码失败:", error);
            this.$message.error("修改密码失败：" + (error.message || "未知错误"));
          } finally {
            this.passwordLoading = false;
          }
        }
      });
    },
  },
};
</script>

<style scoped>
.profile-page {
  padding: 20px;
}

.profile-content {
  padding: 20px 0;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
}

.avatar-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
