<template>
  <div class="admin-profile-page">
    <el-tabs v-model="activeTab">
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
                  <div class="avatar-preview">
                    <img
                      v-if="avatarUrl"
                      :src="avatarUrl"
                      class="avatar"
                      alt="头像"
                    />
                    <i v-else class="el-icon-user-solid avatar-placeholder"></i>
                  </div>
                  <el-button
                    type="primary"
                    size="small"
                    @click="showCropDialog = true"
                    :loading="avatarUploading"
                    >{{ avatarUrl ? "更换头像" : "上传头像" }}</el-button
                  >
                  <div class="avatar-tip">支持JPG、PNG格式，大小不超过5MB</div>
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
    </el-tabs>

    <!-- 头像裁剪对话框 -->
    <el-dialog
      title="裁剪头像"
      :visible.sync="showCropDialog"
      width="600px"
      :close-on-click-modal="false"
      @close="handleCropDialogClose"
    >
      <div class="crop-container">
        <el-upload
          class="avatar-uploader-crop"
          :action="uploadAction"
          :show-file-list="false"
          :before-upload="beforeAvatarUploadForCrop"
          :auto-upload="false"
          :on-change="handleFileChange"
          name="files"
          accept="image/*"
        >
          <el-button type="primary" size="small">选择图片</el-button>
        </el-upload>
        <div v-if="cropImageUrl" class="crop-wrapper">
          <div class="crop-area">
              <img
                v-if="cropImageUrl"
                ref="cropImage"
                :src="cropImageUrl"
                class="crop-image"
                @load="initCrop"
                @error="handleImageError"
              />
            <div
              class="crop-box"
              ref="cropBox"
              @mousedown="startDrag"
            >
              <div class="crop-handle"></div>
            </div>
          </div>
          <div class="crop-preview">
            <canvas ref="previewCanvas" width="200" height="200"></canvas>
            <div class="preview-label">预览</div>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showCropDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleCropAvatar"
          :loading="avatarUploading"
          :disabled="!cropImageUrl"
          >确定</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCurrentUser } from "@/api/auth";
import { updateProfile, changePassword, updateAvatar } from "@/api/user";
import { logout } from "@/api/auth";
import request from "@/utils/request";

export default {
  name: "AdminProfile",
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
      avatarUploading: false,
      uploadAction: "/api/OnlineAuction/auctionFile/upload?fileCategory=profile",
      avatarUrl: "",
      avatarFileId: null,
      showCropDialog: false,
      cropImageUrl: "", // 用于裁剪的图片URL
      cropData: {
        x: 0,
        y: 0,
        width: 200,
        height: 200,
      },
      isDragging: false,
      dragStart: { x: 0, y: 0 },
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
    this.loadUserInfo();
  },
  methods: {
    // 加载用户信息
    async loadUserInfo() {
      try {
        const userInfo = await getCurrentUser();
        if (userInfo) {
          // 回显表单数据
          this.profileForm.userName = userInfo.userName || "";
          this.profileForm.nickName = userInfo.nickName || "";
          this.profileForm.realName = userInfo.realName || "";
          this.profileForm.phone = userInfo.phone || "";
          this.profileForm.email = userInfo.email || "";
          this.profileForm.sex = userInfo.sex || "2";

          // 加载头像
          if (userInfo.avatar) {
            // avatar 已经是相对路径（如 /upload/profile/202601/xxx.png）
            // 直接使用，前端代理会自动处理
            this.avatarUrl = userInfo.avatar;
          } else {
            this.avatarUrl = "";
          }
          
          if (userInfo.avatarFileId) {
            this.avatarFileId = userInfo.avatarFileId;
          } else {
            this.avatarFileId = null;
          }
        }
      } catch (error) {
        this.$message.error("加载用户信息失败：" + (error.message || "未知错误"));
      }
    },
    // 头像上传前验证
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith("image/");
      const isLt5M = file.size / 1024 / 1024 < 5;

      if (!isImage) {
        this.$message.error("只能上传图片文件!");
        return false;
      }
      if (!isLt5M) {
        this.$message.error("头像大小不能超过 5MB!");
        return false;
      }
      return true;
    },
    // 文件选择（用于裁剪）- 使用FileReader读取本地文件
    handleFileChange(file, fileList) {
      // 获取实际的文件对象
      const rawFile = file.raw || file;
      
      if (!rawFile) {
        this.$message.error("文件选择失败");
        return;
      }

      // 验证文件类型和大小
      const isImage = rawFile.type && rawFile.type.startsWith("image/");
      const isLt5M = rawFile.size / 1024 / 1024 < 5;

      if (!isImage) {
        this.$message.error("只能上传图片文件!");
        return;
      }
      if (!isLt5M) {
        this.$message.error("头像大小不能超过 5MB!");
        return;
      }

      // 使用FileReader读取文件
      const reader = new FileReader();
      reader.onload = (e) => {
        this.cropImageUrl = e.target.result;
        this.$nextTick(() => {
          const img = this.$refs.cropImage;
          if (img) {
            // 如果图片已经加载完成
            if (img.complete && img.naturalWidth > 0) {
              this.initCrop();
            } else {
              // 等待图片加载完成
              img.onload = () => {
                this.initCrop();
              };
              img.onerror = () => {
                this.$message.error("图片加载失败，请重试");
                this.cropImageUrl = "";
              };
            }
          }
        });
      };
      reader.onerror = () => {
        this.$message.error("文件读取失败，请重试");
      };
      reader.readAsDataURL(rawFile);
    },
    // 文件上传前处理（用于裁剪）- 阻止自动上传
    beforeAvatarUploadForCrop(file) {
      // 阻止自动上传，文件处理在handleFileChange中进行
      return false;
    },
    // 初始化裁剪区域
    initCrop() {
      if (!this.$refs.cropImage || !this.$refs.cropBox) return;
      const img = this.$refs.cropImage;
      
      // 确保图片已加载完成
      if (!img.complete || img.naturalWidth === 0) {
        img.onload = () => {
          this.initCrop();
        };
        return;
      }
      
      const box = this.$refs.cropBox;
      
      // 设置裁剪框初始位置和大小
      const imgRect = img.getBoundingClientRect();
      const boxSize = Math.min(200, Math.min(imgRect.width, imgRect.height) * 0.8);
      this.cropData.x = (imgRect.width - boxSize) / 2;
      this.cropData.y = (imgRect.height - boxSize) / 2;
      this.cropData.width = boxSize;
      this.cropData.height = boxSize;
      
      this.updateCropBox();
      this.updatePreview();
    },
    // 更新裁剪框位置
    updateCropBox() {
      if (!this.$refs.cropBox) return;
      const box = this.$refs.cropBox;
      box.style.left = this.cropData.x + "px";
      box.style.top = this.cropData.y + "px";
      box.style.width = this.cropData.width + "px";
      box.style.height = this.cropData.height + "px";
    },
    // 更新预览
    updatePreview() {
      if (!this.$refs.cropImage || !this.$refs.previewCanvas) return;
      const img = this.$refs.cropImage;
      const canvas = this.$refs.previewCanvas;
      
      // 确保图片已加载完成
      if (!img.complete || img.naturalWidth === 0) {
        return;
      }
      
      const ctx = canvas.getContext("2d");
      
      const imgRect = img.getBoundingClientRect();
      const scaleX = img.naturalWidth / imgRect.width;
      const scaleY = img.naturalHeight / imgRect.height;
      
      const sx = this.cropData.x * scaleX;
      const sy = this.cropData.y * scaleY;
      const sw = this.cropData.width * scaleX;
      const sh = this.cropData.height * scaleY;
      
      ctx.clearRect(0, 0, 200, 200);
      
      try {
        ctx.drawImage(img, sx, sy, sw, sh, 0, 0, 200, 200);
      } catch (error) {
        // 忽略
      }
    },
    // 开始拖拽
    startDrag(e) {
      this.isDragging = true;
      const imgRect = this.$refs.cropImage.getBoundingClientRect();
      this.dragStart.x = e.clientX - imgRect.left - this.cropData.x;
      this.dragStart.y = e.clientY - imgRect.top - this.cropData.y;
      document.addEventListener("mousemove", this.onDrag);
      document.addEventListener("mouseup", this.stopDrag);
    },
    // 拖拽中
    onDrag(e) {
      if (!this.isDragging || !this.$refs.cropImage) return;
      const imgRect = this.$refs.cropImage.getBoundingClientRect();
      const x = e.clientX - imgRect.left - this.dragStart.x;
      const y = e.clientY - imgRect.top - this.dragStart.y;
      
      // 限制在图片范围内
      this.cropData.x = Math.max(0, Math.min(x, imgRect.width - this.cropData.width));
      this.cropData.y = Math.max(0, Math.min(y, imgRect.height - this.cropData.height));
      
      this.updateCropBox();
      this.updatePreview();
    },
    // 停止拖拽
    stopDrag() {
      this.isDragging = false;
      document.removeEventListener("mousemove", this.onDrag);
      document.removeEventListener("mouseup", this.stopDrag);
    },
    // 裁剪并上传头像
    async handleCropAvatar() {
      if (!this.$refs.cropImage) {
        this.$message.error("请先选择图片");
        return;
      }

      const img = this.$refs.cropImage;
      
      // 确保图片已加载完成
      if (!img.complete || img.naturalWidth === 0) {
        this.$message.error("图片尚未加载完成，请稍候");
        return;
      }

      this.avatarUploading = true;
      try {
        const canvas = document.createElement("canvas");
        canvas.width = 200;
        canvas.height = 200;
        const ctx = canvas.getContext("2d");
        
        const imgRect = img.getBoundingClientRect();
        const scaleX = img.naturalWidth / imgRect.width;
        const scaleY = img.naturalHeight / imgRect.height;
        
        const sx = this.cropData.x * scaleX;
        const sy = this.cropData.y * scaleY;
        const sw = this.cropData.width * scaleX;
        const sh = this.cropData.height * scaleY;
        
        try {
          ctx.drawImage(img, sx, sy, sw, sh, 0, 0, 200, 200);
        } catch (error) {
          throw new Error("图片裁剪失败：" + error.message);
        }
        
        // 将canvas转换为blob
        canvas.toBlob(async (blob) => {
          try {
            // 创建FormData
            const formData = new FormData();
            formData.append("files", blob, "avatar.png");

            // 上传裁剪后的图片 - 使用this.$http
            // 注意：不要手动设置Content-Type，让浏览器自动设置（包含boundary）
            const response = await this.$http.post(
              "/OnlineAuction/auctionFile/upload?fileCategory=profile",
              formData
            );

            // this.$http 的响应拦截器返回的是 res（包含 code, message, data）
            // 如果 code !== 200，拦截器会 reject，所以这里 response 一定是成功的
            if (response && response.data) {
              let fileId = null;
              let filePath = null;

              // response.data 可能是数组或单个对象
              if (Array.isArray(response.data) && response.data.length > 0) {
                fileId = response.data[0].id;
                filePath = response.data[0].filePath;
              } else if (response.data && response.data.id) {
                fileId = response.data.id;
                filePath = response.data.filePath;
              }

              if (fileId) {
                // 更新用户头像
                await updateAvatar(fileId);
                this.avatarFileId = fileId;
                this.avatarUrl = filePath;
                this.$message.success("头像上传成功");
                this.showCropDialog = false;
                this.cropImageUrl = "";

                // 更新localStorage中的用户信息
                const userInfo = JSON.parse(localStorage.getItem("userInfo") || "{}");
                userInfo.avatar = this.avatarUrl;
                userInfo.avatarFileId = fileId;
                localStorage.setItem("userInfo", JSON.stringify(userInfo));
              } else {
                this.$message.error("头像上传失败：未获取到文件ID，响应数据：" + JSON.stringify(response));
              }
            } else {
              this.$message.error("头像上传失败：服务器返回数据格式错误");
            }
          } catch (error) {
            // 详细错误信息
            let errorMsg = "未知错误";
            if (error.response) {
              // 服务器返回了错误响应
              errorMsg = error.response.data?.message || error.response.statusText || `HTTP ${error.response.status}`;
            } else if (error.message) {
              errorMsg = error.message;
            }
            
            this.$message.error("头像上传失败：" + errorMsg);
          } finally {
            this.avatarUploading = false;
          }
        }, "image/png");
      } catch (error) {
        this.$message.error("裁剪失败：" + (error.message || "未知错误"));
        this.avatarUploading = false;
      }
    },
    // 图片加载错误处理
    handleImageError() {
      this.$message.error("图片加载失败，请重新选择");
      this.cropImageUrl = "";
    },
    // 关闭裁剪对话框
    handleCropDialogClose() {
      this.cropImageUrl = "";
      this.cropData = {
        x: 0,
        y: 0,
        width: 200,
        height: 200,
      };
      // 清理事件监听
      if (this.$refs.cropImage) {
        this.$refs.cropImage.onload = null;
        this.$refs.cropImage.onerror = null;
      }
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
              this.handleLogout();
            }, 1500);
          } catch (error) {
            this.$message.error("修改密码失败：" + (error.message || "未知错误"));
          } finally {
            this.passwordLoading = false;
          }
        }
      });
    },
    // 退出登录
    handleLogout() {
      this.$confirm("确定要退出登录吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          logout()
            .then(() => {
              // 清除本地存储
              localStorage.removeItem("userInfo");
              localStorage.removeItem("userId");
              localStorage.removeItem("userName");
              localStorage.removeItem("userRole");
              localStorage.removeItem("isAdmin");
              localStorage.removeItem("isSuperAdmin");
              localStorage.removeItem("isBuyer");
              localStorage.removeItem("isSeller");

              this.$message.success("退出登录成功");
              this.$router.push("/admin/login");
            })
            .catch(() => {
              localStorage.clear();
              this.$router.push("/admin/login");
            });
        })
        .catch(() => {
          // 用户取消
        });
    },
  },
};
</script>

<style scoped>
.admin-profile-page {
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

.avatar-preview {
  margin-bottom: 10px;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #dcdfe6;
}

.avatar-placeholder {
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  font-size: 60px;
  color: #c0c4cc;
  border: 2px dashed #dcdfe6;
  border-radius: 50%;
  display: inline-block;
}

.avatar-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.crop-container {
  min-height: 400px;
}

.avatar-uploader-crop {
  margin-bottom: 20px;
}

.crop-wrapper {
  display: flex;
  gap: 20px;
}

.crop-area {
  position: relative;
  flex: 1;
  max-width: 400px;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.crop-image {
  max-width: 100%;
  max-height: 400px;
  display: block;
  width: auto;
  height: auto;
}

.crop-box {
  position: absolute;
  border: 2px solid #409eff;
  cursor: move;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.5);
}

.crop-handle {
  width: 100%;
  height: 100%;
  background: transparent;
}

.crop-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.crop-preview canvas {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.preview-label {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
