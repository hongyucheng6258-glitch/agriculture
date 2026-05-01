<template>
  <el-dialog title="溯源详情" :visible.sync="dialogVisible" width="900px" @close="handleClose">
    <div v-if="detailData" class="detail-content">
      <!-- 基本信息 -->
      <el-card shadow="never" class="info-card">
        <div slot="header" class="card-header">基本信息</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="溯源码">{{ detailData.trace.traceCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailData.trace.status === '已审核' ? 'success' : 'warning'">
              {{ detailData.trace.status || '待审核' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="批次号">{{ detailData.trace.batchNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="网箱ID">{{ detailData.trace.cageId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="苗种采购时间">{{ detailData.trace.seedPurchaseTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="捕捞时间">{{ detailData.trace.harvestTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="苗种规格">{{ detailData.trace.seedSpec || '-' }}</el-descriptions-item>
          <el-descriptions-item label="苗种来源">{{ detailData.trace.seedSource || '-' }}</el-descriptions-item>
          <el-descriptions-item label="加工标准">{{ detailData.trace.processStandard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="产品质量">{{ detailData.trace.productQuality || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 消费者信息 -->
      <el-card shadow="never" class="info-card">
        <div slot="header" class="card-header">消费者信息</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="消费者姓名">{{ detailData.trace.consumerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailData.trace.consumerPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ detailData.trace.consumerAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="销售时间">{{ detailData.trace.saleTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="销售数量" :span="2">{{ detailData.trace.saleQuantity || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 投喂记录 -->
      <el-card shadow="never" class="info-card">
        <div slot="header" class="card-header">投喂记录</div>
        <el-table :data="detailData.feedingList || []" stripe border size="small" max-height="300">
          <el-table-column prop="feedType" label="饲料类型" width="120"></el-table-column>
          <el-table-column prop="feedAmount" label="投喂量" width="100"></el-table-column>
          <el-table-column prop="feedingTime" label="投喂时间" width="160"></el-table-column>
          <el-table-column prop="operator" label="操作人" width="100"></el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip></el-table-column>
        </el-table>
        <el-empty v-if="!detailData.feedingList || detailData.feedingList.length === 0" description="暂无投喂记录"></el-empty>
      </el-card>

      <!-- 病害记录 -->
      <el-card shadow="never" class="info-card">
        <div slot="header" class="card-header">病害记录</div>
        <el-table :data="detailData.diseaseList || []" stripe border size="small" max-height="300">
          <el-table-column prop="diseaseName" label="病害名称" width="120"></el-table-column>
          <el-table-column prop="symptom" label="症状" show-overflow-tooltip></el-table-column>
          <el-table-column prop="severity" label="严重程度" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.severity === '严重' ? 'danger' : scope.row.severity === '中等' ? 'warning' : 'success'" size="small">
                {{ scope.row.severity }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="discoverTime" label="发现时间" width="160"></el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === '已处理' ? 'success' : 'warning'" size="small">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!detailData.diseaseList || detailData.diseaseList.length === 0" description="暂无病害记录"></el-empty>
      </el-card>

      <!-- 水质记录 -->
      <el-card shadow="never" class="info-card">
        <div slot="header" class="card-header">水质记录</div>
        <el-table :data="detailData.waterQualityList || []" stripe border size="small" max-height="300">
          <el-table-column prop="recordTime" label="记录时间" width="160"></el-table-column>
          <el-table-column prop="waterTemp" label="水温(℃)" width="100"></el-table-column>
          <el-table-column prop="salinity" label="盐度" width="100"></el-table-column>
          <el-table-column prop="dissolvedOxygen" label="溶解氧(mg/L)" width="130"></el-table-column>
          <el-table-column prop="ph" label="pH值" width="80"></el-table-column>
          <el-table-column prop="dataSource" label="数据来源" width="100"></el-table-column>
        </el-table>
        <el-empty v-if="!detailData.waterQualityList || detailData.waterQualityList.length === 0" description="暂无水质记录"></el-empty>
      </el-card>
    </div>
    <div v-else class="loading-container">
      <i class="el-icon-loading" style="font-size: 40px; color: #409eff;"></i>
      <p>加载中...</p>
    </div>
  </el-dialog>
</template>

<script>
import { getDetail } from '@/api/trace';

export default {
  name: 'TraceDetailDialog',
  data() {
    return {
      dialogVisible: false,
      detailData: null,
      currentId: null
    };
  },
  methods: {
    async open(id) {
      this.currentId = id;
      this.dialogVisible = true;
      this.detailData = null;
      try {
        const res = await getDetail(id);
        this.detailData = res.data || res;
      } catch (error) {
        this.$message.error('获取详情失败');
        console.error(error);
      }
    },
    handleClose() {
      this.detailData = null;
      this.currentId = null;
    }
  }
};
</script>

<style scoped>
.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.info-card {
  margin-bottom: 16px;
}

.card-header {
  font-weight: 600;
  font-size: 14px;
}

.loading-container {
  text-align: center;
  padding: 40px 0;
}

.loading-container p {
  margin-top: 12px;
  color: #909399;
}
</style>
