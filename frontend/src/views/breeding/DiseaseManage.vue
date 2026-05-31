<template>
  <div class="disease-manage">
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
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="请选择处理状态" clearable @change="handleSearch">
            <el-option label="待处理" value="待处理" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已解决" value="已解决" />
          </el-select>
        </el-form-item>
        <el-form-item label="发现日期">
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
        <el-table-column prop="diseaseName" label="病害名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="symptom" label="症状" min-width="140" show-overflow-tooltip />
        <el-table-column label="严重程度" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getSeverityType(scope.row.severity)" size="small">
              {{ scope.row.severity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discoverTime" label="发现时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" icon="el-icon-s-tools" style="color: #E6A23C;" @click="handleProcess(scope.row)">处理</el-button>
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

    <!-- 新增对话框 -->
    <el-dialog title="新增病害记录" :visible.sync="addDialogVisible" width="560px" @close="resetAddForm">
      <el-form ref="addForm" :model="addForm" :rules="addRules" label-width="100px">
        <el-form-item label="网箱ID" prop="cageId">
          <el-select v-model="addForm.cageId" placeholder="请选择网箱" style="width: 100%;">
            <el-option
              v-for="item in cageList"
              :key="item.id"
              :label="item.cageCode"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="病害名称" prop="diseaseName">
          <el-input v-model="addForm.diseaseName" placeholder="请输入病害名称" />
        </el-form-item>
        <el-form-item label="症状" prop="symptom">
          <el-input v-model="addForm.symptom" type="textarea" :rows="2" placeholder="请输入症状描述" />
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="addForm.severity" placeholder="请选择严重程度" style="width: 100%;">
            <el-option label="轻度" value="轻度" />
            <el-option label="中度" value="中度" />
            <el-option label="重度" value="重度" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理措施" prop="treatment">
          <el-input v-model="addForm.treatment" type="textarea" :rows="2" placeholder="请输入处理措施" />
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="addForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="发现时间" prop="discoverTime">
          <el-date-picker
            v-model="addForm.discoverTime"
            type="datetime"
            placeholder="请选择发现时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="addForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="addDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitAddForm">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog title="编辑病害记录" :visible.sync="editDialogVisible" width="560px" @close="resetEditForm">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="网箱ID" prop="cageId">
          <el-select v-model="editForm.cageId" placeholder="请选择网箱" style="width: 100%;">
            <el-option
              v-for="item in cageList"
              :key="item.id"
              :label="item.cageCode"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="病害名称" prop="diseaseName">
          <el-input v-model="editForm.diseaseName" placeholder="请输入病害名称" />
        </el-form-item>
        <el-form-item label="症状" prop="symptom">
          <el-input v-model="editForm.symptom" type="textarea" :rows="2" placeholder="请输入症状描述" />
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="editForm.severity" placeholder="请选择严重程度" style="width: 100%;">
            <el-option label="轻度" value="轻度" />
            <el-option label="中度" value="中度" />
            <el-option label="重度" value="重度" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理措施" prop="treatment">
          <el-input v-model="editForm.treatment" type="textarea" :rows="2" placeholder="请输入处理措施" />
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="editForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="发现时间" prop="discoverTime">
          <el-date-picker
            v-model="editForm.discoverTime"
            type="datetime"
            placeholder="请选择发现时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitEditForm">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog title="处理病害" :visible.sync="handleDialogVisible" width="520px" @close="resetHandleForm">
      <el-form ref="handleForm" :model="handleForm" :rules="handleRules" label-width="100px">
        <el-form-item label="处理措施" prop="treatment">
          <el-input v-model="handleForm.treatment" type="textarea" :rows="3" placeholder="请输入处理措施" />
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="handleForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理时间" prop="handleTime">
          <el-date-picker
            v-model="handleForm.handleTime"
            type="datetime"
            placeholder="处理时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="处理备注" prop="handleRemark">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="2" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="handleDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitHandleForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getList, add, update, handleDisease } from '@/api/disease';
import { mapGetters } from 'vuex';
import Pagination from '@/components/Pagination';

export default {
  name: 'DiseaseManage',
  components: { Pagination },
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      searchForm: {
        cageId: '',
        status: '',
        dateRange: null
      },
      listQuery: {
        page: 1,
        limit: 10
      },
      submitLoading: false,
      // 新增对话框
      addDialogVisible: false,
      addForm: {
        cageId: '',
        diseaseName: '',
        symptom: '',
        severity: '',
        treatment: '',
        handler: '',
        discoverTime: '',
        remark: ''
      },
      addRules: {
        cageId: [{ required: true, message: '请选择网箱', trigger: 'change' }],
        diseaseName: [{ required: true, message: '请输入病害名称', trigger: 'blur' }],
        severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }],
        discoverTime: [{ required: true, message: '请选择发现时间', trigger: 'change' }]
      },
      // 编辑对话框
      editDialogVisible: false,
      editForm: {
        id: null,
        cageId: '',
        diseaseName: '',
        symptom: '',
        severity: '',
        treatment: '',
        handler: '',
        discoverTime: '',
        remark: ''
      },
      editRules: {
        cageId: [{ required: true, message: '请选择网箱', trigger: 'change' }],
        diseaseName: [{ required: true, message: '请输入病害名称', trigger: 'blur' }],
        severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }],
        discoverTime: [{ required: true, message: '请选择发现时间', trigger: 'change' }]
      },
      // 处理对话框
      handleDialogVisible: false,
      handleForm: {
        id: null,
        treatment: '',
        handler: '',
        handleTime: '',
        handleRemark: ''
      },
      handleRules: {
        treatment: [{ required: true, message: '请输入处理措施', trigger: 'blur' }],
        handler: [{ required: true, message: '请输入处理人', trigger: 'blur' }]
      }
    };
  },
  computed: {
    ...mapGetters(['cageList'])
  },
  created() {
    this.getList();
    this.$store.dispatch('fetchCageList');
  },
  methods: {
    async getList() {
      this.loading = true;
      try {
        const params = {
          page: this.listQuery.page,
          size: this.listQuery.limit,
          cageId: this.searchForm.cageId || undefined,
          status: this.searchForm.status || undefined,
          startTime: this.searchForm.dateRange ? this.searchForm.dateRange[0] : undefined,
          endTime: this.searchForm.dateRange ? this.searchForm.dateRange[1] : undefined
        };
        const res = await getList(params);
        const data = res.data || res;
        this.tableData = data.records || data.list || [];
        this.total = data.total || 0;
      } catch (error) {
        console.error('获取病害记录列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.listQuery.page = 1;
      this.getList();
    },
    handleReset() {
      this.searchForm = { cageId: '', status: '', dateRange: null };
      this.listQuery.page = 1;
      this.getList();
    },
    handleAdd() {
      this.addForm = {
        cageId: '',
        diseaseName: '',
        symptom: '',
        severity: '',
        treatment: '',
        handler: '',
        discoverTime: '',
        remark: ''
      };
      this.addDialogVisible = true;
    },
    handleEdit(row) {
      this.editForm = {
        id: row.id,
        cageId: row.cageId,
        diseaseName: row.diseaseName,
        symptom: row.symptom,
        severity: row.severity,
        treatment: row.treatment,
        handler: row.handler,
        discoverTime: row.discoverTime,
        remark: row.remark
      };
      this.editDialogVisible = true;
    },
    handleProcess(row) {
      this.handleForm = {
        id: row.id,
        treatment: row.treatment || '',
        handler: row.handler || '',
        handleTime: this.formatNow(),
        handleRemark: ''
      };
      this.handleDialogVisible = true;
    },
    formatNow() {
      const d = new Date();
      const pad = n => String(n).padStart(2, '0');
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
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
    submitHandleForm() {
      this.$refs.handleForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          await handleDisease(this.handleForm);
          this.$message.success('处理成功');
          this.handleDialogVisible = false;
          this.getList();
        } catch (error) {
          console.error('处理失败:', error);
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
    resetHandleForm() {
      if (this.$refs.handleForm) {
        this.$refs.handleForm.resetFields();
      }
    },
    getSeverityType(severity) {
      const map = { '轻度': 'success', '中度': 'warning', '重度': 'danger' };
      return map[severity] || 'info';
    },
    getStatusType(status) {
      const map = { '待处理': 'danger', '处理中': 'warning', '已解决': 'success' };
      return map[status] || 'info';
    }
  }
};
</script>

<style scoped>
.disease-manage {
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
