<template>
  <div class="trace-query">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="溯源码">
          <el-input
            v-model="queryForm.traceCode"
            placeholder="请输入溯源码"
            clearable
            style="width: 320px;"
            @keyup.enter.native="handleQuery">
          </el-input>
        </el-form-item>
        <el-form-item label="或消费者">
          <el-input
            v-model="queryForm.consumerKeyword"
            placeholder="姓名/电话"
            clearable
            style="width: 280px;"
            @keyup.enter.native="handleQueryByConsumer">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">溯源码查询</el-button>
          <el-button icon="el-icon-search" @click="handleQueryByConsumer">消费者查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 消费者查询结果列表 -->
    <el-card v-if="showConsumerList" shadow="never" class="list-card">
      <div slot="header" class="card-header">
        <span>溯源记录列表</span>
      </div>
      <el-table :data="consumerTraceList" stripe border v-loading="consumerLoading" style="width: 100%;">
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="traceCode" label="溯源码" min-width="160" show-overflow-tooltip></el-table-column>
        <el-table-column prop="consumerName" label="消费者姓名" width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="consumerPhone" label="联系电话" width="130" show-overflow-tooltip></el-table-column>
        <el-table-column prop="batchNo" label="批次号" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="productQuality" label="产品质量" width="100" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-view" @click="handleSelectTrace(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 溯源详情展示 -->
    <el-card v-if="traceData && !showConsumerList" shadow="never" class="result-card">
      <div slot="header" class="card-header">
        <span>溯源信息 - {{ traceData.traceCode }}</span>
      </div>
      
      <!-- 基本信息 -->
      <el-descriptions :column="2" border size="small" class="info-section">
        <el-descriptions-item label="溯源码">{{ traceData.traceCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="traceData.status === '已审核' ? 'success' : 'warning'" size="small">{{ traceData.status || '待审核' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="批次号">{{ traceData.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="网箱ID">{{ traceData.cageId }}</el-descriptions-item>
        <el-descriptions-item label="苗种采购时间">{{ traceData.seedPurchaseTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="苗种规格">{{ traceData.seedSpec || '-' }}</el-descriptions-item>
        <el-descriptions-item label="苗种来源">{{ traceData.seedSource || '-' }}</el-descriptions-item>
        <el-descriptions-item label="捕捞时间">{{ traceData.harvestTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品质量">{{ traceData.productQuality || '-' }}</el-descriptions-item>
        <el-descriptions-item label="加工标准">{{ traceData.processStandard || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 消费者信息 -->
      <div class="section-title">消费者信息</div>
      <el-descriptions :column="2" border size="small" class="info-section">
        <el-descriptions-item label="消费者姓名">{{ traceData.consumerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ traceData.consumerPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ traceData.consumerAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="销售时间">{{ traceData.saleTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="销售数量">{{ traceData.saleQuantity || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 时间线展示 -->
      <el-timeline class="timeline-section">
        <!-- 节点1: 苗种信息 -->
        <el-timeline-item timestamp="苗种信息" placement="top" type="primary" icon="el-icon-star-on">
          <el-card shadow="never" class="timeline-card">
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="采购时间">{{ traceData.seedPurchaseTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="规格">{{ traceData.seedSpec || '-' }}</el-descriptions-item>
              <el-descriptions-item label="来源">{{ traceData.seedSource || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-timeline-item>

        <!-- 节点2: 养殖信息 -->
        <el-timeline-item timestamp="养殖信息" placement="top" type="success" icon="el-icon-office-building">
          <el-card shadow="never" class="timeline-card">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="网箱ID">{{ traceData.cageId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="批次号">{{ traceData.batchNo || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-timeline-item>

        <!-- 节点3: 投喂概况 -->
        <el-timeline-item timestamp="投喂概况" placement="top" type="warning" icon="el-icon-food">
          <el-card shadow="never" class="timeline-card">
            <p class="timeline-content">{{ traceData.feedingSummary || '暂无投喂信息' }}</p>
          </el-card>
        </el-timeline-item>

        <!-- 节点4: 病害概况 -->
        <el-timeline-item timestamp="病害概况" placement="top" type="danger" icon="el-icon-warning">
          <el-card shadow="never" class="timeline-card">
            <p class="timeline-content">{{ traceData.diseaseSummary || '暂无病害记录' }}</p>
          </el-card>
        </el-timeline-item>

        <!-- 节点5: 捕捞加工 -->
        <el-timeline-item timestamp="捕捞加工" placement="top" type="primary" icon="el-icon-box">
          <el-card shadow="never" class="timeline-card">
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="捕捞时间">{{ traceData.harvestTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="加工标准">{{ traceData.processStandard || '-' }}</el-descriptions-item>
              <el-descriptions-item label="产品质量">{{ traceData.productQuality || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 空状态 -->
    <el-card v-else-if="queried && !showConsumerList" shadow="never" class="result-card">
      <el-empty description="未查询到溯源信息，请检查溯源码是否正确"></el-empty>
    </el-card>
  </div>
</template>

<script>
import { queryByCode, searchByConsumer } from '@/api/trace';

export default {
  name: 'TraceQuery',
  data() {
    return {
      queryForm: {
        traceCode: '',
        consumerKeyword: ''
      },
      traceData: null,
      consumerTraceList: [],
      queried: false,
      loading: false,
      consumerLoading: false,
      showConsumerList: false
    };
  },
  methods: {
    async handleQuery() {
      if (!this.queryForm.traceCode) {
        this.$message.warning('请输入溯源码');
        return;
      }
      this.showConsumerList = false;
      this.loading = true;
      this.queried = true;
      try {
        const res = await queryByCode(this.queryForm.traceCode);
        this.traceData = res.data || res;
      } catch (error) {
        console.error('查询溯源信息失败:', error);
        this.traceData = null;
      } finally {
        this.loading = false;
      }
    },
    async handleQueryByConsumer() {
      if (!this.queryForm.consumerKeyword) {
        this.$message.warning('请输入消费者姓名或电话');
        return;
      }
      this.traceData = null;
      this.consumerLoading = true;
      this.queried = true;
      this.showConsumerList = true;
      try {
        const res = await searchByConsumer(this.queryForm.consumerKeyword);
        this.consumerTraceList = res.data || res || [];
        if (this.consumerTraceList.length === 0) {
          this.$message.info('未找到相关溯源记录');
        }
      } catch (error) {
        console.error('查询消费者溯源信息失败:', error);
        this.consumerTraceList = [];
      } finally {
        this.consumerLoading = false;
      }
    },
    handleSelectTrace(row) {
      this.traceData = row;
      this.showConsumerList = false;
    }
  }
};
</script>

<style scoped>
.trace-query {
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

.result-card >>> .el-card__body,
.list-card >>> .el-card__body {
  padding: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.info-section {
  margin-bottom: 20px;
}

.section-title {
  font-weight: 600;
  margin: 20px 0 12px;
  font-size: 14px;
  color: #303133;
}

.timeline-section {
  margin-top: 20px;
}

.timeline-card >>> .el-card__body {
  padding: 12px 16px;
}

.timeline-content {
  margin: 0;
  line-height: 1.8;
  color: #606266;
}
</style>
