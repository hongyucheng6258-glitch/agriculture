<template>
  <div class="user-manage">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入用户名或真实姓名搜索"
        clearable
        prefix-icon="el-icon-search"
        style="width: 300px"
        @keyup.enter.native="handleSearch"
      />
      <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
      <el-button type="success" icon="el-icon-plus" @click="handleAdd">添加用户</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="真实姓名" width="140" />
        <el-table-column prop="role" label="角色" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'primary'">
              {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'info'">
              {{ scope.row.isEnabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="280">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="warning" @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button
              size="mini"
              :type="scope.row.isEnabled === 1 ? 'info' : 'success'"
              @click="handleToggleEnabled(scope.row)"
            >
              {{ scope.row.isEnabled === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        background
        :current-page="pagination.pageNum"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" append-to-body>
      <el-form ref="userForm" :model="userForm" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="operator" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog title="重置密码" :visible.sync="passwordDialogVisible" width="400px" append-to-body>
      <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="passwordDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="handleSubmitPassword">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getUserList,
  addUser,
  updateUser,
  deleteUser,
  toggleUserEnabled,
  resetUserPassword
} from '@/api/user';

export default {
  name: 'UserManage',
  data() {
    return {
      searchKeyword: '',
      loading: false,
      tableData: [],
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      submitLoading: false,
      userForm: {
        id: null,
        username: '',
        password: '',
        realName: '',
        role: 'operator'
      },
      userRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
      },
      passwordDialogVisible: false,
      passwordLoading: false,
      passwordForm: {
        userId: null,
        newPassword: ''
      },
      passwordRules: {
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ]
      }
    };
  },
  created() {
    this.fetchData();
  },
  methods: {
    async fetchData() {
      this.loading = true;
      try {
        const res = await getUserList({
          pageNum: this.pagination.pageNum,
          pageSize: this.pagination.pageSize,
          keyword: this.searchKeyword
        });
        this.tableData = res.data?.records || [];
        this.pagination.total = res.data?.total || 0;
      } catch (error) {
        console.error('获取用户列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.pagination.pageNum = 1;
      this.fetchData();
    },
    handlePageChange(pageNum) {
      this.pagination.pageNum = pageNum;
      this.fetchData();
    },
    handleAdd() {
      this.isEdit = false;
      this.dialogTitle = '添加用户';
      this.userForm = {
        id: null,
        username: '',
        password: '',
        realName: '',
        role: 'operator'
      };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.isEdit = true;
      this.dialogTitle = '编辑用户';
      this.userForm = {
        id: row.id,
        username: row.username,
        password: '',
        realName: row.realName,
        role: row.role
      };
      this.dialogVisible = true;
    },
    async handleSubmit() {
      this.$refs.userForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          if (this.isEdit) {
            await updateUser(this.userForm.id, this.userForm);
            this.$message.success('更新成功');
          } else {
            await addUser(this.userForm);
            this.$message.success('添加成功');
          }
          this.dialogVisible = false;
          this.fetchData();
        } catch (error) {
          console.error('操作失败:', error);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除用户【${row.realName}】吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        await deleteUser(row.id);
        this.$message.success('删除成功');
        this.fetchData();
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error);
        }
      }
    },
    async handleToggleEnabled(row) {
      try {
        await toggleUserEnabled(row.id);
        this.$message.success('操作成功');
        this.fetchData();
      } catch (error) {
        console.error('操作失败:', error);
      }
    },
    handleResetPassword(row) {
      this.passwordForm = {
        userId: row.id,
        newPassword: ''
      };
      this.passwordDialogVisible = true;
    },
    async handleSubmitPassword() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (!valid) return;
        this.passwordLoading = true;
        try {
          await resetUserPassword(this.passwordForm.userId, this.passwordForm.newPassword);
          this.$message.success('密码重置成功');
          this.passwordDialogVisible = false;
        } catch (error) {
          console.error('重置密码失败:', error);
        } finally {
          this.passwordLoading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
.user-manage {
  padding: 0;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.table-container {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(255, 105, 180, 0.08);
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
