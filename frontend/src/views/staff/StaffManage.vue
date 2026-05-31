<template>
  <div class="staff-manage">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="岗位">
          <el-select v-model="searchForm.position" placeholder="请选择岗位" clearable @change="handleSearch">
            <el-option label="养殖员" value="养殖员" />
            <el-option label="技术员" value="技术员" />
            <el-option label="管理员" value="管理员" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option label="在职" value="在职" />
            <el-option label="离职" value="离职" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
          <el-button type="success" icon="el-icon-plus" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe border v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="编号" width="60" align="center" />
        <el-table-column prop="name" label="姓名" width="100" align="center" />
        <el-table-column prop="phone" label="联系电话" width="140" align="center" />
        <el-table-column prop="position" label="岗位" width="100" align="center" />
        <el-table-column prop="responsibleCage" label="负责网箱" min-width="120" show-overflow-tooltip />
        <el-table-column prop="entryDate" label="入职日期" width="120" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '在职' ? 'success' : 'info'" size="small">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" icon="el-icon-delete" style="color: #F56C6C;" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        :total="total"
        :page.sync="listQuery.page"
        :limit.sync="listQuery.limit"
        @pagination="getList"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="520px" @close="resetForm">
      <el-form ref="staffForm" :model="staffForm" :rules="staffRules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="staffForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="staffForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="岗位" prop="position">
          <el-select v-model="staffForm.position" placeholder="请选择岗位" style="width: 100%;">
            <el-option label="养殖员" value="养殖员" />
            <el-option label="技术员" value="技术员" />
            <el-option label="管理员" value="管理员" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责网箱" prop="responsibleCage">
          <el-input v-model="staffForm.responsibleCage" placeholder="请输入负责网箱" />
        </el-form-item>
        <el-form-item label="入职日期" prop="entryDate">
          <el-date-picker
            v-model="staffForm.entryDate"
            type="date"
            placeholder="请选择入职日期"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="staffForm.status" placeholder="请选择状态" style="width: 100%;">
            <el-option label="在职" value="在职" />
            <el-option label="离职" value="离职" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="staffForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getList, add, update, deleteRecord } from '@/api/staff';
import Pagination from '@/components/Pagination';

export default {
  name: 'StaffManage',
  components: { Pagination },
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      searchForm: {
        name: '',
        position: '',
        status: ''
      },
      listQuery: {
        page: 1,
        limit: 10
      },
      dialogVisible: false,
      isEdit: false,
      dialogTitle: '新增人员',
      submitLoading: false,
      staffForm: {
        id: null,
        name: '',
        phone: '',
        position: '',
        responsibleCage: '',
        entryDate: '',
        status: '在职',
        remark: ''
      },
      staffRules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
        position: [{ required: true, message: '请选择岗位', trigger: 'change' }],
        entryDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    async getList() {
      this.loading = true;
      try {
        const params = {
          page: this.listQuery.page,
          size: this.listQuery.limit,
          name: this.searchForm.name || undefined,
          position: this.searchForm.position || undefined,
          status: this.searchForm.status || undefined
        };
        const res = await getList(params);
        const data = res.data || res;
        this.tableData = data.records || data.list || [];
        this.total = data.total || 0;
      } catch (error) {
        console.error('获取人员列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.listQuery.page = 1;
      this.getList();
    },
    handleReset() {
      this.searchForm = { name: '', position: '', status: '' };
      this.listQuery.page = 1;
      this.getList();
    },
    handleAdd() {
      this.isEdit = false;
      this.dialogTitle = '新增人员';
      this.staffForm = {
        id: null,
        name: '',
        phone: '',
        position: '',
        responsibleCage: '',
        entryDate: '',
        status: '在职',
        remark: ''
      };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.isEdit = true;
      this.dialogTitle = '编辑人员';
      this.staffForm = {
        id: row.id,
        name: row.name,
        phone: row.phone,
        position: row.position,
        responsibleCage: row.responsibleCage,
        entryDate: row.entryDate,
        status: row.status,
        remark: row.remark
      };
      this.dialogVisible = true;
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除人员「${row.name}」吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        await deleteRecord(row.id);
        this.$message.success('删除成功');
        this.getList();
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error);
        }
      }
    },
    submitForm() {
      this.$refs.staffForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          if (this.isEdit) {
            await update(this.staffForm);
            this.$message.success('更新成功');
          } else {
            await add(this.staffForm);
            this.$message.success('新增成功');
          }
          this.dialogVisible = false;
          this.getList();
        } catch (error) {
          console.error('操作失败:', error);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    resetForm() {
      if (this.$refs.staffForm) {
        this.$refs.staffForm.resetFields();
      }
    }
  }
};
</script>

<style scoped>
.staff-manage {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
}

.search-card >>> .el-card__body {
  padding: 18px 20px 0;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.table-card >>> .el-card__body {
  padding: 16px 20px;
}
</style>
