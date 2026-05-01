<template>
  <div class="trace-manage">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="溯源码">
          <el-input v-model="searchForm.traceCode" placeholder="请输入溯源码" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="searchForm.batchNo" placeholder="请输入批次号" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="消费者">
          <el-input v-model="searchForm.consumerKeyword" placeholder="姓名/电话" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <div slot="header" class="table-header">
        <span>溯源记录列表</span>
        <div class="header-buttons">
          <el-button type="success" icon="el-icon-plus" size="small" @click="handleAdd">新增</el-button>
          <el-button type="primary" icon="el-icon-s-grid" size="small" @click="handleBatchGenerate">批量生成</el-button>
        </div>
      </div>
      <el-table :data="tableData" stripe border v-loading="loading" style="width: 100%;">
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="traceCode" label="溯源码" min-width="160" show-overflow-tooltip></el-table-column>
        <el-table-column prop="batchNo" label="批次号" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '已审核' ? 'success' : 'warning'" size="small">{{ scope.row.status || '待审核' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="consumerName" label="消费者" width="100" show-overflow-tooltip></el-table-column>
        <el-table-column prop="consumerPhone" label="联系电话" width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="seedPurchaseTime" label="苗种采购时间" min-width="160" show-overflow-tooltip></el-table-column>
        <el-table-column prop="harvestTime" label="捕捞时间" min-width="160" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-view" @click="handleViewDetail(scope.row)">详情</el-button>
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" icon="el-icon-picture" @click="handleShowQRCode(scope.row)">二维码</el-button>
            <el-button type="text" icon="el-icon-check" v-if="scope.row.status !== '已审核'" @click="handleAudit(scope.row)">审核</el-button>
            <el-button type="text" icon="el-icon-delete" class="danger-text" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        :total="total"
        :page.sync="listQuery.page"
        :limit.sync="listQuery.limit"
        @pagination="getList">
      </pagination>
    </el-card>

    <!-- 新增对话框 -->
    <el-dialog title="新增溯源记录" :visible.sync="addDialogVisible" width="650px" @close="resetAddForm">
      <el-form ref="addForm" :model="addForm" :rules="addRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="网箱" prop="cageId">
              <el-select v-model="addForm.cageId" placeholder="请选择网箱" style="width: 100%;">
                <el-option
                  v-for="item in cageList"
                  :key="item.id"
                  :label="item.cageCode"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="addForm.batchNo" placeholder="请输入批次号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="苗种采购时间" prop="seedPurchaseTime">
              <el-date-picker
                v-model="addForm.seedPurchaseTime"
                type="datetime"
                placeholder="请选择苗种采购时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%;">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="苗种规格" prop="seedSpec">
              <el-input v-model="addForm.seedSpec" placeholder="请输入苗种规格"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="苗种来源" prop="seedSource">
          <el-input v-model="addForm.seedSource" placeholder="请输入苗种来源"></el-input>
        </el-form-item>
        <el-form-item label="投喂概况" prop="feedingSummary">
          <el-input v-model="addForm.feedingSummary" type="textarea" :rows="2" placeholder="请输入投喂概况"></el-input>
        </el-form-item>
        <el-form-item label="病害概况" prop="diseaseSummary">
          <el-input v-model="addForm.diseaseSummary" type="textarea" :rows="2" placeholder="请输入病害概况"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="捕捞时间" prop="harvestTime">
              <el-date-picker
                v-model="addForm.harvestTime"
                type="datetime"
                placeholder="请选择捕捞时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%;">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品质量" prop="productQuality">
              <el-input v-model="addForm.productQuality" placeholder="请输入产品质量"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="加工标准" prop="processStandard">
          <el-input v-model="addForm.processStandard" type="textarea" :rows="2" placeholder="请输入加工标准"></el-input>
        </el-form-item>
        <el-divider content-position="left">消费者信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="消费者姓名">
              <el-input v-model="addForm.consumerName" placeholder="请输入消费者姓名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="addForm.consumerPhone" placeholder="请输入联系电话"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="收货地址">
          <el-input v-model="addForm.consumerAddress" placeholder="请输入收货地址"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="销售时间">
              <el-date-picker
                v-model="addForm.saleTime"
                type="datetime"
                placeholder="请选择销售时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%;">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售数量">
              <el-input-number v-model="addForm.saleQuantity" :min="0" style="width: 100%;"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer">
        <el-button @click="addDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitAddForm">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog title="编辑溯源记录" :visible.sync="editDialogVisible" width="650px" @close="resetEditForm">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="网箱" prop="cageId">
              <el-select v-model="editForm.cageId" placeholder="请选择网箱" style="width: 100%;">
                <el-option
                  v-for="item in cageList"
                  :key="item.id"
                  :label="item.cageCode"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="editForm.batchNo" placeholder="请输入批次号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="苗种采购时间" prop="seedPurchaseTime">
              <el-date-picker
                v-model="editForm.seedPurchaseTime"
                type="datetime"
                placeholder="请选择苗种采购时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%;">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="苗种规格" prop="seedSpec">
              <el-input v-model="editForm.seedSpec" placeholder="请输入苗种规格"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="苗种来源" prop="seedSource">
          <el-input v-model="editForm.seedSource" placeholder="请输入苗种来源"></el-input>
        </el-form-item>
        <el-form-item label="投喂概况" prop="feedingSummary">
          <el-input v-model="editForm.feedingSummary" type="textarea" :rows="2" placeholder="请输入投喂概况"></el-input>
        </el-form-item>
        <el-form-item label="病害概况" prop="diseaseSummary">
          <el-input v-model="editForm.diseaseSummary" type="textarea" :rows="2" placeholder="请输入病害概况"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="捕捞时间" prop="harvestTime">
              <el-date-picker
                v-model="editForm.harvestTime"
                type="datetime"
                placeholder="请选择捕捞时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%;">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品质量" prop="productQuality">
              <el-input v-model="editForm.productQuality" placeholder="请输入产品质量"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="加工标准" prop="processStandard">
          <el-input v-model="editForm.processStandard" type="textarea" :rows="2" placeholder="请输入加工标准"></el-input>
        </el-form-item>
        <el-divider content-position="left">消费者信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="消费者姓名">
              <el-input v-model="editForm.consumerName" placeholder="请输入消费者姓名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="editForm.consumerPhone" placeholder="请输入联系电话"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="收货地址">
          <el-input v-model="editForm.consumerAddress" placeholder="请输入收货地址"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="销售时间">
              <el-date-picker
                v-model="editForm.saleTime"
                type="datetime"
                placeholder="请选择销售时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%;">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售数量">
              <el-input-number v-model="editForm.saleQuantity" :min="0" style="width: 100%;"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitEditForm">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 二维码对话框 -->
    <el-dialog title="溯源码二维码" :visible.sync="qrcodeDialogVisible" width="400px">
      <div class="qrcode-container">
        <img v-if="currentQRCodeUrl" :src="currentQRCodeUrl" alt="溯源码二维码" class="qrcode-image">
        <p class="trace-code-text">溯源码：{{ currentRow ? currentRow.traceCode : '' }}</p>
      </div>
      <span slot="footer">
        <el-button @click="qrcodeDialogVisible = false">关 闭</el-button>
        <el-button type="primary" @click="handleDownloadQRCode">下载二维码</el-button>
      </span>
    </el-dialog>

    <!-- 详情对话框 -->
    <trace-detail-dialog ref="detailDialog"></trace-detail-dialog>

    <!-- 批量生成对话框 -->
    <batch-generate-dialog ref="batchDialog" @success="getList"></batch-generate-dialog>
  </div>
</template>

<script>
import { getList, add, update, deleteById, audit, getQRCodeUrl } from '@/api/trace';
import { mapGetters } from 'vuex';
import Pagination from '@/components/Pagination';
import TraceDetailDialog from './TraceDetailDialog.vue';
import BatchGenerateDialog from './BatchGenerateDialog.vue';

export default {
  name: 'TraceManage',
  components: {
    Pagination,
    TraceDetailDialog,
    BatchGenerateDialog
  },
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      searchForm: {
        traceCode: '',
        batchNo: '',
        consumerKeyword: ''
      },
      listQuery: {
        page: 1,
        limit: 10
      },
      submitLoading: false,
      addDialogVisible: false,
      addForm: {
        cageId: null,
        batchNo: '',
        seedPurchaseTime: '',
        seedSpec: '',
        seedSource: '',
        feedingSummary: '',
        diseaseSummary: '',
        harvestTime: '',
        processStandard: '',
        productQuality: '',
        consumerName: '',
        consumerPhone: '',
        consumerAddress: '',
        saleTime: '',
        saleQuantity: null
      },
      addRules: {
        cageId: [{ required: true, message: '请选择网箱', trigger: 'change' }],
        batchNo: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
        seedPurchaseTime: [{ required: true, message: '请选择苗种采购时间', trigger: 'change' }]
      },
      editDialogVisible: false,
      editForm: {
        id: null,
        cageId: null,
        batchNo: '',
        seedPurchaseTime: '',
        seedSpec: '',
        seedSource: '',
        feedingSummary: '',
        diseaseSummary: '',
        harvestTime: '',
        processStandard: '',
        productQuality: '',
        consumerName: '',
        consumerPhone: '',
        consumerAddress: '',
        saleTime: '',
        saleQuantity: null
      },
      editRules: {
        cageId: [{ required: true, message: '请选择网箱', trigger: 'change' }],
        batchNo: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
        seedPurchaseTime: [{ required: true, message: '请选择苗种采购时间', trigger: 'change' }]
      },
      qrcodeDialogVisible: false,
      currentQRCodeUrl: '',
      currentRow: null
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
          traceCode: this.searchForm.traceCode || undefined,
          batchNo: this.searchForm.batchNo || undefined,
          consumerKeyword: this.searchForm.consumerKeyword || undefined
        };
        const res = await getList(params);
        const data = res.data || res;
        this.tableData = data.records || data.list || [];
        this.total = data.total || 0;
      } catch (error) {
        console.error('获取溯源记录列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.listQuery.page = 1;
      this.getList();
    },
    handleReset() {
      this.searchForm = {
        traceCode: '',
        batchNo: '',
        consumerKeyword: ''
      };
      this.listQuery.page = 1;
      this.getList();
    },
    handleAdd() {
      this.addForm = {
        cageId: null,
        batchNo: '',
        seedPurchaseTime: '',
        seedSpec: '',
        seedSource: '',
        feedingSummary: '',
        diseaseSummary: '',
        harvestTime: '',
        processStandard: '',
        productQuality: '',
        consumerName: '',
        consumerPhone: '',
        consumerAddress: '',
        saleTime: '',
        saleQuantity: null
      };
      this.addDialogVisible = true;
    },
    handleEdit(row) {
      this.editForm = {
        id: row.id,
        cageId: row.cageId,
        batchNo: row.batchNo,
        seedPurchaseTime: row.seedPurchaseTime,
        seedSpec: row.seedSpec,
        seedSource: row.seedSource,
        feedingSummary: row.feedingSummary,
        diseaseSummary: row.diseaseSummary,
        harvestTime: row.harvestTime,
        processStandard: row.processStandard,
        productQuality: row.productQuality,
        consumerName: row.consumerName,
        consumerPhone: row.consumerPhone,
        consumerAddress: row.consumerAddress,
        saleTime: row.saleTime,
        saleQuantity: row.saleQuantity
      };
      this.editDialogVisible = true;
    },
    handleViewDetail(row) {
      this.$refs.detailDialog.open(row.id);
    },
    handleShowQRCode(row) {
      this.currentRow = row;
      this.currentQRCodeUrl = getQRCodeUrl(row.id);
      this.qrcodeDialogVisible = true;
    },
    handleDownloadQRCode() {
      if (this.currentQRCodeUrl) {
        window.open(this.currentQRCodeUrl);
      }
    },
    async handleAudit(row) {
      this.$confirm('确定要审核通过这条溯源记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await audit(row.id, { auditUser: '管理员' });
          this.$message.success('审核成功');
          this.getList();
        } catch (error) {
          console.error('审核失败:', error);
        }
      }).catch(() => {});
    },
    async handleDelete(row) {
      this.$confirm('确定要删除这条溯源记录吗？删除后不可恢复！', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteById(row.id);
          this.$message.success('删除成功');
          this.getList();
        } catch (error) {
          console.error('删除失败:', error);
        }
      }).catch(() => {});
    },
    handleBatchGenerate() {
      this.$refs.batchDialog.open();
    },
    async submitAddForm() {
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
    async submitEditForm() {
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
    resetAddForm() {
      if (this.$refs.addForm) {
        this.$refs.addForm.resetFields();
      }
    },
    resetEditForm() {
      if (this.$refs.editForm) {
        this.$refs.editForm.resetFields();
      }
    }
  }
};
</script>

<style scoped>
.trace-manage {
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

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.danger-text {
  color: #f56c6c;
}

.qrcode-container {
  text-align: center;
  padding: 20px 0;
}

.qrcode-image {
  width: 250px;
  height: 250px;
  margin-bottom: 16px;
}

.trace-code-text {
  font-size: 14px;
  color: #606266;
  margin: 0;
}
</style>
