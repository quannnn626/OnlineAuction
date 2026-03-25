<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>注册账号</h2>
        <p>注册为买方用户，可参与竞拍与申请成为卖家</p>
      </div>
      <el-form
        ref="registerForm"
        :model="form"
        :rules="rules"
        class="register-form"
        label-width="0"
      >
        <el-form-item prop="userName">
          <el-input
            v-model="form.userName"
            placeholder="用户名（3～32位，字母数字下划线）"
            prefix-icon="el-icon-user"
            maxlength="32"
          />
        </el-form-item>
        <el-form-item prop="nickName">
          <el-input
            v-model="form.nickName"
            placeholder="昵称（可选，默认与用户名相同）"
            prefix-icon="el-icon-s-custom"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="手机号"
            prefix-icon="el-icon-mobile-phone"
            maxlength="11"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码（至少6位）"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            prefix-icon="el-icon-lock"
            show-password
            @keyup.enter.native="submit"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="submit-btn"
            @click="submit"
          >
            {{ loading ? "提交中..." : "注册" }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <router-link to="/login">已有账号？去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { register } from "@/api/auth";

export default {
  name: "Register",
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };
    return {
      loading: false,
      form: {
        userName: "",
        nickName: "",
        phone: "",
        password: "",
        confirmPassword: "",
      },
      rules: {
        userName: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          {
            min: 3,
            max: 32,
            message: "用户名长度为 3～32 位",
            trigger: "blur",
          },
          {
            pattern: /^[a-zA-Z0-9_]+$/,
            message: "仅允许字母、数字、下划线",
            trigger: "blur",
          },
        ],
        phone: [
          { required: true, message: "请输入手机号", trigger: "blur" },
          {
            pattern: /^1\d{10}$/,
            message: "请输入11位手机号",
            trigger: "blur",
          },
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { min: 6, message: "密码至少6位", trigger: "blur" },
        ],
        confirmPassword: [
          { required: true, message: "请再次输入密码", trigger: "blur" },
          { validator: validateConfirm, trigger: "blur" },
        ],
      },
    };
  },
  methods: {
    submit() {
      this.$refs.registerForm.validate((valid) => {
        if (!valid) return;
        this.loading = true;
        register({
          userName: this.form.userName.trim(),
          password: this.form.password,
          phone: this.form.phone.trim(),
          nickName: this.form.nickName.trim() || undefined,
        })
          .then(() => {
            this.$message.success("注册成功，请登录");
            this.$router.push("/login");
          })
          .catch(() => {})
          .finally(() => {
            this.loading = false;
          });
      });
    },
  },
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.register-header h2 {
  color: #333;
  margin-bottom: 8px;
  font-size: 26px;
}

.register-header p {
  color: #999;
  font-size: 13px;
  line-height: 1.5;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
}

.register-footer a {
  color: #667eea;
  text-decoration: none;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>
