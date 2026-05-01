<template>
  <div class="feeding-manage">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="网箱ID">
          <el-select v-model="searchForm.cageId" placeholder="请选择网箱" clearable @change="handleSearch">
            <el-option
              v-for="item in cageList"
              :key="item.id"
              :label="item.cageCode"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="饲料类型">
          <el-input v-model="searchForm.feedType" placeholder="请输入饲料类型" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="投喂日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
          />
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
        <el-table-column prop="cageId" label="网箱ID" width="100" align="center" />
        <el-table-column prop="feedType" label="饲料类型" min-width="120" show-overflow-tooltip />
        <el-table-column prop="feedAmount" label="投喂量(kg)" width="120" align="center" />
        <el-table-column prop="feedingTime" label="投喂时间" min-width="160" show-overflow-tooltip />
        <el-table-column prop="operator" label="操作人员" width="100" align="center" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
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
      <el-form ref="feedingForm" :model="feedingForm" :rules="feedingRules" label-width="100px">
        <el-form-item label="网箱ID" prop="cageId">
          <el-select v-model="feedingForm.cageId" placeholder="请选择网箱" style="width: 100%;">
            <el-option
              v-for="item in cageList"
              :key="item.id"
              :label="item.cageCode"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="饲料类型" prop="feedType">
          <el-select v-model="feedingForm.feedType" placeholder="请选择饲料类型" style="width: 100%;">
            <el-option
              v-for="item in feedStockList"
              :key="item.id"
              :label="item.feedType"
              :value="item.feedType"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="currentStockInfo" label="当前库存">
          <div style="font-weight: 600; color: #2d3436;">
            {{ currentStockInfo.stockAmount.toFixed(1) }} kg
          </div>
        </el-form-item>
        <el-form-item label="投喂量(kg)" prop="feedAmount">
          <el-input-number v-model="feedingForm.feedAmount" :min="0" :precision="1" :step="0.5" style="width: 100%;" />
        </el-form-item>
        <el-form-item v-if="stockWarningMessage">
          <el-alert
            :title="stockWarningMessage"
            :type="stockWarningMessage.includes('库存不足') ? 'error' : 'warning'"
            :closable="false"
            show-icon
          />
        </el-form-item>
        <el-form-item label="投喂时间" prop="feedingTime">
          <el-date-picker
            v-model="feedingForm.feedingTime"
            type="datetime"
            placeholder="请选择投喂时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="操作人员" prop="operator">
          <el-input v-model="feedingForm.operator" placeholder="请输入操作人员" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="feedingForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { getList, add, update, deleteRecord } from '@/api/feeding';
import { getList as getFeedStockList } from '@/api/feedStock';
import { mapGetters } from 'vuex';
import Pagination from '@/components/Pagination';

export default {
  name: 'FeedingManage',
  components: { Pagination },
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      feedStockList: [],
      searchForm: {
        cageId: '',
        feedType: '',
        dateRange: null
      },
      listQuery: {
        page: 1,
        limit: 10
      },
      dialogVisible: false,
      isEdit: false,
      dialogTitle: '新增投喂记录',
      submitLoading: false,
      feedingForm: {
        id: null,
        cageId: '',
        feedType: '',
        feedAmount: 0,
        feedingTime: '',
        operator: '',
        remark: ''
      },
      feedingRules: {
        cageId: [{ required: true, message: '请选择网箱', trigger: 'change' }],
        feedType: [{ required: true, message: '请选择饲料类型', trigger: 'change' }],
        feedAmount: [{ required: true, message: '请输入投喂量', trigger: 'blur' }],
        feedingTime: [{ required: true, message: '请选择投喂时间', trigger: 'change' }],
        operator: [{ required: true, message: '请输入操作人员', trigger: 'blur' }]
      }
    };
  },
  computed: {
    ...mapGetters(['cageList']),
    currentStockInfo() {
      if (!this.feedingForm.feedType) return null;
      return this.feedStockList.find(item => item.feedType === this.feedingForm.feedType);
    },
    stockWarningMessage() {
      if (!this.currentStockInfo) return '';
      const stock = this.currentStockInfo.stockAmount || 0;
      const feedAmount = this.feedingForm.feedAmount || 0;
      if (stock < feedAmount) {
        return `库存不足！当前库存: ${stock.toFixed(1)}kg，预计使用: ${feedAmount.toFixed(1)}kg`;
      }
      if (stock - feedAmount < 5) {
        return `库存紧张！剩余库存: ${(stock - feedAmount).toFixed(1)}kg`;
      }
      return '';
    }
  },
  created() {
    this.getList();
    this.loadFeedStock();
    this.$store.dispatch('fetchCageList');
  },
  methods: {
    async loadFeedStock() {
      try {
        const res = await getFeedStockList();
        const data = res.data || res;
        // 直接获取data数组，支持多种返回格式
        if (Array.isArray(data)) {
          this.feedStockList = data;
        } else {
          this.feedStockList = data.records || data.list || [];
        }
        console.log('加载的饲料库存数据:', this.feedStockList);
      } catch (error) {
        console.error('获取饲料库存失败:', error);
        this.$message.error('加载饲料类型失败，请刷新页面');
      }
    },
    async getList() {
      this.loading = true;
      try {
        const params = {
          page: this.listQuery.page,
          size: this.listQuery.limit,
          cageId: this.searchForm.cageId || undefined,
          feedType: this.searchForm.feedType || undefined,
          startTime: this.searchForm.dateRange ? this.searchForm.dateRange[0] : undefined,
          endTime: this.searchForm.dateRange ? this.searchForm.dateRange[1] : undefined
        };
        const res = await getList(params);
        const data = res.data || res;
        this.tableData = data.records || data.list || [];
        this.total = data.total || 0;
      } catch (error) {
        console.error('获取投喂记录列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.listQuery.page = 1;
      this.getList();
    },
    handleReset() {
      this.searchForm = { cageId: '', feedType: '', dateRange: null };
      this.listQuery.page = 1;
      this.getList();
    },
    handleAdd() {
      this.isEdit = false;
      this.dialogTitle = '新增投喂记录';
      this.feedingForm = {
        id: null,
        cageId: '',
        feedType: '',
        feedAmount: 0,
        feedingTime: '',
        operator: '',
        remark: ''
      };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.isEdit = true;
      this.dialogTitle = '编辑投喂记录';
      this.feedingForm = {
        id: row.id,
        cageId: row.cageId,
        feedType: row.feedType,
        feedAmount: row.feedAmount,
        feedingTime: row.feedingTime,
        operator: row.operator,
        remark: row.remark
      };
      this.dialogVisible = true;
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该投喂记录吗？', '提示', {
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
      this.$refs.feedingForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          if (this.isEdit) {
            await update(this.feedingForm);
            this.$message.success('更新成功');
          } else {
            await add(this.feedingForm);
            this.$message.success('新增成功');
            // 新增投喂后重新加载库存数据
            this.loadFeedStock();
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
      if (this.$refs.feedingForm) {
        this.$refs.feedingForm.resetFields();
      }
    }
  }
};
</script>

<style scoped>
.feeding-manage {
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
