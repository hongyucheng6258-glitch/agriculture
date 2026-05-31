<template>
  <div class="feed-stock">
    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <div slot="header" class="card-header">
        <span>饲料库存管理</span>
        <el-button type="success" icon="el-icon-plus" size="small" style="float: right;" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="tableData" stripe border v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="编号" width="60" align="center" />
        <el-table-column prop="feedType" label="饲料类型" min-width="120" show-overflow-tooltip />
        <el-table-column prop="stockAmount" label="当前库存(kg)" width="140" align="center" />
        <el-table-column prop="unitPrice" label="单价(元/kg)" width="130" align="center" />
        <el-table-column prop="supplier" label="供应商" min-width="140" show-overflow-tooltip />
        <el-table-column prop="lastRestockTime" label="最近补货时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" icon="el-icon-goods" style="color: #E6A23C;" @click="handleRestock(scope.row)">补货</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增对话框 -->
    <el-dialog title="新增饲料" :visible.sync="addDialogVisible" width="520px" @close="resetAddForm">
      <el-form ref="addForm" :model="addForm" :rules="addRules" label-width="100px">
        <el-form-item label="饲料类型" prop="feedType">
          <el-input v-model="addForm.feedType" placeholder="请输入饲料类型" />
        </el-form-item>
        <el-form-item label="库存量(kg)" prop="stockAmount">
          <el-input-number v-model="addForm.stockAmount" :min="0" :precision="2" :step="10" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="单价(元/kg)" prop="unitPrice">
          <el-input-number v-model="addForm.unitPrice" :min="0" :precision="2" :step="0.5" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="addForm.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="addForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="addDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitAddForm">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog title="编辑饲料" :visible.sync="editDialogVisible" width="520px" @close="resetEditForm">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="饲料类型" prop="feedType">
          <el-input v-model="editForm.feedType" placeholder="请输入饲料类型" />
        </el-form-item>
        <el-form-item label="库存量(kg)" prop="stockAmount">
          <el-input-number v-model="editForm.stockAmount" :min="0" :precision="2" :step="10" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="单价(元/kg)" prop="unitPrice">
          <el-input-number v-model="editForm.unitPrice" :min="0" :precision="2" :step="0.5" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="editForm.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitEditForm">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 补货对话框 -->
    <el-dialog title="饲料补货" :visible.sync="restockDialogVisible" width="480px" @close="resetRestockForm">
      <el-form ref="restockForm" :model="restockForm" :rules="restockRules" label-width="100px">
        <el-form-item label="饲料类型">
          <el-input :value="restockForm.feedType" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :value="restockForm.currentStock + ' kg'" disabled />
        </el-form-item>
        <el-form-item label="补货数量(kg)" prop="restockAmount">
          <el-input-number v-model="restockForm.restockAmount" :min="0.1" :precision="2" :step="10" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="restockDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitRestockForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getList, add, update, restock } from '@/api/feedStock';

export default {
  name: 'FeedStock',
  data() {
    return {
      loading: false,
      tableData: [],
      submitLoading: false,
      // 新增对话框
      addDialogVisible: false,
      addForm: {
        feedType: '',
        stockAmount: 0,
        unitPrice: 0,
        supplier: '',
        remark: ''
      },
      addRules: {
        feedType: [{ required: true, message: '请输入饲料类型', trigger: 'blur' }],
        stockAmount: [{ required: true, message: '请输入库存量', trigger: 'blur' }],
        unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
        supplier: [{ required: true, message: '请输入供应商', trigger: 'blur' }]
      },
      // 编辑对话框
      editDialogVisible: false,
      editForm: {
        id: null,
        feedType: '',
        stockAmount: 0,
        unitPrice: 0,
        supplier: '',
        remark: ''
      },
      editRules: {
        feedType: [{ required: true, message: '请输入饲料类型', trigger: 'blur' }],
        stockAmount: [{ required: true, message: '请输入库存量', trigger: 'blur' }],
        unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
        supplier: [{ required: true, message: '请输入供应商', trigger: 'blur' }]
      },
      // 补货对话框
      restockDialogVisible: false,
      restockForm: {
        id: null,
        feedType: '',
        currentStock: 0,
        restockAmount: 0
      },
      restockRules: {
        restockAmount: [{ required: true, message: '请输入补货数量', trigger: 'blur' }]
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
        const res = await getList();
        const data = res.data || res;
        this.tableData = data.records || data.list || data || [];
      } catch (error) {
        console.error('获取饲料库存列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    handleAdd() {
      this.addForm = {
        feedType: '',
        stockAmount: 0,
        unitPrice: 0,
        supplier: '',
        remark: ''
      };
      this.addDialogVisible = true;
    },
    handleEdit(row) {
      this.editForm = {
        id: row.id,
        feedType: row.feedType,
        stockAmount: row.stockAmount,
        unitPrice: row.unitPrice,
        supplier: row.supplier,
        remark: row.remark
      };
      this.editDialogVisible = true;
    },
    handleRestock(row) {
      this.restockForm = {
        id: row.id,
        feedType: row.feedType,
        currentStock: row.stockAmount,
        restockAmount: 0
      };
      this.restockDialogVisible = true;
    },
    submitAddForm() {
      this.$refs.addForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          await add(this.addForm);
          this.$message.success('新增成功');
          this.addDialogVisible = false;
          this.getList();
        } catch (error) {
          console.error('新增失败:', error);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    submitEditForm() {
      this.$refs.editForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          await update(this.editForm);
          this.$message.success('更新成功');
          this.editDialogVisible = false;
          this.getList();
        } catch (error) {
          console.error('更新失败:', error);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    submitRestockForm() {
      this.$refs.restockForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          await restock({
            id: this.restockForm.id,
            amount: this.restockForm.restockAmount
          });
          this.$message.success('补货成功');
          this.restockDialogVisible = false;
          this.getList();
        } catch (error) {
          console.error('补货失败:', error);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    resetAddForm() {
      if (this.$refs.addForm) {
        this.$refs.addForm.resetFields();
      }
    },
    resetEditForm() {
      if (this.$refs.editForm) {
        this.$refs.editForm.resetFields();
      }
    },
    resetRestockForm() {
      if (this.$refs.restockForm) {
        this.$refs.restockForm.resetFields();
      }
    }
  }
};
</script>

<style scoped>
.feed-stock {
  padding: 0;
}

.table-card >>> .el-card__body {
  padding: 16px 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}
</style>
