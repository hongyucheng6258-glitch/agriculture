<template>
  <div class="login-container">
    <!-- 动态背景 -->
    <div class="bg-decorations">
      <div class="decoration decoration-1"></div>
      <div class="decoration decoration-2"></div>
      <div class="decoration decoration-3"></div>
      <div class="decoration decoration-4"></div>
    </div>
    
    <div class="login-card">
      <div class="login-header">
        <i class="el-icon-s-opportunity login-logo"></i>
        <h2 class="login-title">智慧渔业管理系统</h2>
        <p class="login-subtitle">Smart Aquaculture Management System</p>
      </div>
      <el-form
        ref="loginForm"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter.native="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            prefix-icon="el-icon-user"
            placeholder="请输入用户名"
            clearable
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            prefix-icon="el-icon-lock"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            size="large"
          >
            <el-button
              slot="suffix"
              type="text"
              icon="el-icon-view"
              @click="showPassword = !showPassword"
            ></el-button>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <span class="forgot-password" style="float: right; cursor: pointer; color: #ff69b4;">忘记密码？</span>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loginLoading"
            class="login-btn"
            @click="handleLogin"
            size="large"
          >
            {{ loginLoading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>© 左鬼右告智慧渔业管理系统</span>
      </div>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/auth';

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ]
      },
      loginLoading: false,
      showPassword: false,
      rememberMe: false
    };
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (!valid) return;

        this.loginLoading = true;
        try {
          const res = await login(this.loginForm);
          const resultData = res.data; // 后端返回的 data 是包含 token 和 user 的对象
          const token = resultData?.token;
          if (!token) {
            this.$message.error('登录失败：未获取到令牌');
            return;
          }
          const user = resultData?.user || {};

          localStorage.setItem('token', token);
          localStorage.setItem('user', JSON.stringify(user));
          this.$store.commit('SET_USER', user);

          this.$message.success('登录成功');
          this.$router.push('/dashboard');
        } catch (error) {
          console.error('登录失败:', error);
          const errorMsg = error?.response?.data?.message || error?.message || '登录失败，请检查用户名和密码';
          this.$message.error(errorMsg);
        } finally {
          this.loginLoading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff5f8 0%, #ffe8f0 50%, #fffafc 100%);
  position: relative;
  overflow: hidden;
}

/* 装饰性背景元素 */
.bg-decorations {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.decoration {
  position: absolute;
  border-radius: 50%;
  opacity: 0.4;
  animation: float 6s ease-in-out infinite;
}

.decoration-1 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #ff69b4, #ff85c0);
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.decoration-2 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #ff85c0, #ffa8d1);
  bottom: 20%;
  left: -80px;
  animation-delay: 1.5s;
}

.decoration-3 {
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, #ffa8d1, #ffcde0);
  top: 30%;
  right: 15%;
  animation-delay: 3s;
}

.decoration-4 {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #ffcde0, #ffe8f0);
  bottom: 10%;
  right: 30%;
  animation-delay: 4.5s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-30px) scale(1.1);
    opacity: 0.6;
  }
}

/* 登录卡片 */
.login-card {
  width: 440px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 105, 180, 0.2);
  box-shadow: 0 20px 60px rgba(255, 105, 180, 0.2),
              0 8px 25px rgba(255, 105, 180, 0.1);
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 卡片顶部装饰线 */
.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 4px;
  background: linear-gradient(90deg, transparent, #ff69b4, #ff85c0, transparent);
  border-radius: 2px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-logo {
  font-size: 56px;
  color: #ff69b4;
  margin-bottom: 16px;
  display: block;
  filter: drop-shadow(0 4px 12px rgba(255, 105, 180, 0.3));
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.login-title {
  font-size: 26px;
  color: #2d3436;
  margin: 0 0 8px 0;
  font-weight: 800;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #ff69b4, #ff85c0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-subtitle {
  font-size: 13px;
  color: #b2bec3;
  margin: 0;
  letter-spacing: 1px;
  font-weight: 500;
}

.login-form {
  margin-top: 10px;
}

/* 输入框样式 */
.login-form >>> .el-form-item__error {
  color: #ff4757;
}

.login-form >>> .el-input__inner {
  height: 50px;
  font-size: 15px;
  background: #fffafc;
  border: 2px solid rgba(255, 105, 180, 0.15);
  border-radius: 14px;
  color: #2d3436;
  padding-left: 44px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.login-form >>> .el-input__inner::placeholder {
  color: #b2bec3;
}

.login-form >>> .el-input__inner:focus {
  border-color: #ff69b4;
  box-shadow: 0 0 0 4px rgba(255, 105, 180, 0.1);
  background: #ffffff;
}

.login-form >>> .el-input__prefix {
  font-size: 18px;
  color: #b2bec3;
  left: 14px;
  transition: all 0.3s ease;
}

.login-form >>> .el-input__inner:focus + .el-input__prefix,
.login-form >>> .el-input__prefix:hover {
  color: #ff69b4;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #ff69b4, #ff85c0) !important;
  border: none !important;
  border-radius: 14px !important;
  margin-top: 8px;
  letter-spacing: 4px;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(255, 105, 180, 0.35);
}

.login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(255, 105, 180, 0.45);
  filter: brightness(1.05);
}

.login-btn:active {
  transform: translateY(-1px);
}

.login-footer {
  text-align: center;
  margin-top: 32px;
  color: #b2bec3;
  font-size: 13px;
  font-weight: 500;
}
</style>
