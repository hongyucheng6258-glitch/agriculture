<template>
  <div class="user-info">
    <el-card shadow="hover">
      <div slot="header" class="card-header">
        <span>用户信息</span>
      </div>

      <div class="info-container" v-loading="loading">
        <div class="info-section">
          <h3 class="section-title">基本信息</h3>
          <el-form ref="infoForm" :model="infoForm" :rules="infoRules" label-width="100px" style="max-width: 500px;">
            <el-form-item label="用户名">
              <el-input :value="infoForm.username" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag :type="infoForm.role === 'admin' ? 'danger' : 'info'">
                {{ infoForm.role === 'admin' ? '管理员' : '操作员' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="infoForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="创建时间">
              <el-input :value="infoForm.createTime" disabled />
            </el-form-item>
            <el-form-item label="最后登录">
              <el-input :value="infoForm.lastLoginTime" disabled />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="info-section">
          <h3 class="section-title">修改密码</h3>
          <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 500px;">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :loading="pwdLoading" @click="handlePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getUserInfo, updateUserInfo, changePassword } from '@/api/auth';

export default {
  name: 'UserInfo',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.pwdForm.newPassword) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };
    return {
      loading: false,
      submitLoading: false,
      pwdLoading: false,
      infoForm: {
        username: '',
        realName: '',
        role: '',
        createTime: '',
        lastLoginTime: ''
      },
      infoRules: {
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
      },
      pwdForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      pwdRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 4, message: '密码长度不能少于4位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      }
    };
  },
  created() {
    this.loadUserInfo();
  },
  methods: {
    async loadUserInfo() {
      this.loading = true;
      try {
        const res = await getUserInfo();
        const user = res.data || res;
        this.infoForm = {
          username: user.username || '',
          realName: user.realName || '',
          role: user.role || '',
          createTime: user.createTime || '',
          lastLoginTime: user.lastLoginTime || ''
        };
      } catch (error) {
        console.error('获取用户信息失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async handleSubmit() {
      this.$refs.infoForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          await updateUserInfo({ realName: this.infoForm.realName });
          this.$message.success('信息修改成功');
          // 更新 Vuex 中的用户信息
          this.$store.commit('SET_USER', {
            ...this.$store.getters.user,
            realName: this.infoForm.realName
          });
        } catch (error) {
          console.error('修改信息失败:', error);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    async handlePassword() {
      this.$refs.pwdForm.validate(async (valid) => {
        if (!valid) return;
        this.pwdLoading = true;
        try {
          await changePassword({
            oldPassword: this.pwdForm.oldPassword,
            newPassword: this.pwdForm.newPassword
          });
          this.$message.success('密码修改成功，请重新登录');
          this.pwdForm = { oldPassword: '', newPassword: '', confirmPassword: '' };
          this.$refs.pwdForm.resetFields();
          // 退出登录
          setTimeout(() => {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            this.$store.commit('SET_USER', {});
            this.$router.push('/login').catch(() => {});
          }, 1500);
        } catch (error) {
          console.error('修改密码失败:', error);
        } finally {
          this.pwdLoading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
.user-info {
  padding: 0;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.info-container {
  display: flex;
  gap: 40px;
  flex-wrap: wrap;
}

.info-section {
  flex: 1;
  min-width: 300px;
}

.section-title {
  font-size: 15px;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}
</style>
