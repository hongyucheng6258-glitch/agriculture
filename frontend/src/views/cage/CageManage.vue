<template>
  <div class="cage-manage">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="网箱编号">
          <el-input
            v-model="searchForm.cageCode"
            placeholder="请输入网箱编号"
            clearable
            @keyup.enter.native="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option label="使用中" value="使用中" />
            <el-option label="空闲" value="空闲" />
            <el-option label="维修" value="维修" />
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
        <el-table-column prop="cageCode" label="网箱编号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="location" label="位置" min-width="140" show-overflow-tooltip />
        <el-table-column prop="breedType" label="养殖品种" min-width="120" show-overflow-tooltip />
        <el-table-column prop="scale" label="规模(尾)" width="100" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
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
      <el-form ref="cageForm" :model="cageForm" :rules="cageRules" label-width="90px">
        <el-form-item label="网箱编号" prop="cageCode">
          <el-input v-model="cageForm.cageCode" placeholder="请输入网箱编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="cageForm.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="养殖品种" prop="breedType">
          <el-input v-model="cageForm.breedType" placeholder="请输入养殖品种" />
        </el-form-item>
        <el-form-item label="规模(尾)" prop="scale">
          <el-input-number v-model="cageForm.scale" :min="0" :max="999999" placeholder="请输入规模" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="cageForm.status" placeholder="请选择状态" style="width: 100%;">
            <el-option label="使用中" value="使用中" />
            <el-option label="空闲" value="空闲" />
            <el-option label="维修" value="维修" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="cageForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { getCageList, addCage, updateCage, deleteCage } from '@/api/cage';
import Pagination from '@/components/Pagination';

export default {
  name: 'CageManage',
  components: { Pagination },
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      searchForm: {
        cageCode: '',
        status: ''
      },
      listQuery: {
        page: 1,
        limit: 10
      },
      dialogVisible: false,
      isEdit: false,
      dialogTitle: '新增网箱',
      submitLoading: false,
      cageForm: {
        id: null,
        cageCode: '',
        location: '',
        breedType: '',
        scale: 0,
        status: '空闲',
        remark: ''
      },
      cageRules: {
        cageCode: [
          { required: true, message: '请输入网箱编号', trigger: 'blur' }
        ],
        location: [
          { required: true, message: '请输入位置', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
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
          cageCode: this.searchForm.cageCode || undefined,
          status: this.searchForm.status || undefined
        };
        const res = await getCageList(params);
        const data = res.data || res;
        this.tableData = data.records || data.list || [];
        this.total = data.total || 0;
      } catch (error) {
        console.error('获取网箱列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.listQuery.page = 1;
      this.getList();
    },
    handleReset() {
      this.searchForm = { cageCode: '', status: '' };
      this.listQuery.page = 1;
      this.getList();
    },
    handleAdd() {
      this.isEdit = false;
      this.dialogTitle = '新增网箱';
      this.cageForm = {
        id: null,
        cageCode: '',
        location: '',
        breedType: '',
        scale: 0,
        status: '空闲',
        remark: ''
      };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.isEdit = true;
      this.dialogTitle = '编辑网箱';
      this.cageForm = {
        id: row.id,
        cageCode: row.cageCode,
        location: row.location,
        breedType: row.breedType,
        scale: row.scale,
        status: row.status,
        remark: row.remark
      };
      this.dialogVisible = true;
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除网箱「${row.cageCode}」吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        await deleteCage(row.id);
        this.$message.success('删除成功');
        this.getList();
        this.$store.dispatch('fetchCageList');
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error);
        }
      }
    },
    submitForm() {
      this.$refs.cageForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          if (this.isEdit) {
            await updateCage(this.cageForm);
            this.$message.success('更新成功');
          } else {
            await addCage(this.cageForm);
            this.$message.success('新增成功');
          }
          this.dialogVisible = false;
          this.getList();
          this.$store.dispatch('fetchCageList');
        } catch (error) {
          console.error('操作失败:', error);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    resetForm() {
      if (this.$refs.cageForm) {
        this.$refs.cageForm.resetFields();
      }
    },
    getStatusType(status) {
      const map = {
        '使用中': 'success',
        '空闲': 'info',
        '维修': 'warning'
      };
      return map[status] || 'info';
    }
  }
};
</script>

<style scoped>
.cage-manage {
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
