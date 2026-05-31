<template>
  <div class="data-simulate">
    <el-row :gutter="20">
      <!-- 水质数据模拟 -->
      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="simulate-card">
          <div slot="header" class="card-header">
            <i class="el-icon-water-cup" style="color: #409EFF; margin-right: 8px;"></i>
            <span>水质数据模拟</span>
          </div>
          <el-form ref="waterForm" :model="waterForm" :rules="waterRules" label-width="100px">
            <el-form-item label="选择网箱" prop="cageId">
              <el-select v-model="waterForm.cageId" placeholder="请选择网箱" filterable style="width: 100%;">
                <el-option
                  v-for="item in cageList"
                  :key="item.id"
                  :label="item.cageCode"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="生成数量" prop="count">
              <el-slider
                v-model="waterForm.count"
                :min="10"
                :max="100"
                :step="5"
                show-input
                :show-input-controls="false"
                input-size="small"
              />
            </el-form-item>
            <el-form-item label="时间范围" prop="dateRange">
              <el-date-picker
                v-model="waterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                icon="el-icon-cpu"
                :loading="waterLoading"
                @click="handleSimulateWater"
                style="width: 100%;"
              >
                {{ waterLoading ? '模拟中...' : '开始模拟水质数据' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 气象数据模拟 -->
      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="simulate-card">
          <div slot="header" class="card-header">
            <i class="el-icon-cloudy" style="color: #E6A23C; margin-right: 8px;"></i>
            <span>气象数据模拟</span>
          </div>
          <el-form ref="weatherForm" :model="weatherForm" :rules="weatherRules" label-width="100px">
            <el-form-item label="生成数量" prop="count">
              <el-slider
                v-model="weatherForm.count"
                :min="10"
                :max="100"
                :step="5"
                show-input
                :show-input-controls="false"
                input-size="small"
              />
            </el-form-item>
            <el-form-item label="时间范围" prop="dateRange">
              <el-date-picker
                v-model="weatherForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="warning"
                icon="el-icon-cpu"
                :loading="weatherLoading"
                @click="handleSimulateWeather"
                style="width: 100%;"
              >
                {{ weatherLoading ? '模拟中...' : '开始模拟气象数据' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 溯源数据模拟 -->
      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="simulate-card">
          <div slot="header" class="card-header">
            <i class="el-icon-document" style="color: #67C23A; margin-right: 8px;"></i>
            <span>溯源数据模拟</span>
          </div>
          <el-form ref="traceForm" :model="traceForm" :rules="traceRules" label-width="100px">
            <el-form-item label="选择网箱" prop="cageId">
              <el-select v-model="traceForm.cageId" placeholder="请选择网箱" filterable style="width: 100%;">
                <el-option
                  v-for="item in cageList"
                  :key="item.id"
                  :label="item.cageCode"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="生成数量" prop="count">
              <el-slider
                v-model="traceForm.count"
                :min="10"
                :max="200"
                :step="10"
                show-input
                :show-input-controls="false"
                input-size="small"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="success"
                icon="el-icon-cpu"
                :loading="traceLoading"
                @click="handleSimulateTrace"
                style="width: 100%;"
              >
                {{ traceLoading ? '模拟中...' : '开始模拟溯源数据' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { simulateWaterQuality, simulateTrace } from '@/api/simulate';
import { simulateWeather } from '@/api/weather';
import { mapGetters } from 'vuex';

export default {
  name: 'DataSimulate',
  data() {
    return {
      waterLoading: false,
      weatherLoading: false,
      traceLoading: false,
      waterForm: {
        cageId: '',
        count: 30,
        dateRange: null
      },
      waterRules: {
        cageId: [{ required: true, message: '请选择网箱', trigger: 'change' }],
        count: [{ required: true, message: '请设置生成数量', trigger: 'change' }],
        dateRange: [{ required: true, message: '请选择时间范围', trigger: 'change' }]
      },
      weatherForm: {
        count: 30,
        dateRange: null
      },
      weatherRules: {
        count: [{ required: true, message: '请设置生成数量', trigger: 'change' }],
        dateRange: [{ required: true, message: '请选择时间范围', trigger: 'change' }]
      },
      traceForm: {
        cageId: '',
        count: 50
      },
      traceRules: {
        cageId: [{ required: true, message: '请选择网箱', trigger: 'change' }],
        count: [{ required: true, message: '请设置生成数量', trigger: 'change' }]
      }
    };
  },
  computed: {
    ...mapGetters(['cageList'])
  },
  methods: {
    handleSimulateWater() {
      this.$refs.waterForm.validate(async (valid) => {
        if (!valid) return;
        this.waterLoading = true;
        try {
          // 后端接收 startDay/endDay（相对于当前日期的天数偏移）
          const now = new Date();
          now.setHours(0, 0, 0, 0);
          const startDate = new Date(this.waterForm.dateRange[0]);
          const endDate = new Date(this.waterForm.dateRange[1]);
          const startDay = Math.round((startDate - now) / (24 * 3600 * 1000));
          const endDay = Math.round((endDate - now) / (24 * 3600 * 1000));

          await simulateWaterQuality({
            cageId: this.waterForm.cageId,
            count: this.waterForm.count,
            startDay,
            endDay
          });
          this.$message.success(`成功模拟生成 ${this.waterForm.count} 条水质数据`);
        } catch (error) {
          console.error('水质数据模拟失败:', error);
        } finally {
          this.waterLoading = false;
        }
      });
    },
    handleSimulateWeather() {
      this.$refs.weatherForm.validate(async (valid) => {
        if (!valid) return;
        this.weatherLoading = true;
        try {
          // 后端接收 startDay/endDay（相对于当前日期的天数偏移）
          const now = new Date();
          now.setHours(0, 0, 0, 0);
          const startDate = new Date(this.weatherForm.dateRange[0]);
          const endDate = new Date(this.weatherForm.dateRange[1]);
          const startDay = Math.round((startDate - now) / (24 * 3600 * 1000));
          const endDay = Math.round((endDate - now) / (24 * 3600 * 1000));

          await simulateWeather({
            count: this.weatherForm.count,
            startDay,
            endDay
          });
          this.$message.success(`成功模拟生成 ${this.weatherForm.count} 条气象数据`);
        } catch (error) {
          console.error('气象数据模拟失败:', error);
        } finally {
          this.weatherLoading = false;
        }
      });
    },
    handleSimulateTrace() {
      this.$refs.traceForm.validate(async (valid) => {
        if (!valid) return;
        this.traceLoading = true;
        try {
          const res = await simulateTrace({
            cageId: this.traceForm.cageId,
            count: this.traceForm.count
          });
          this.$message.success(res.data || res || `成功模拟生成 ${this.traceForm.count} 条溯源数据`);
        } catch (error) {
          console.error('溯源数据模拟失败:', error);
        } finally {
          this.traceLoading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
.data-simulate {
  padding: 0;
}

.simulate-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.simulate-card >>> .el-card__body {
  padding: 24px 30px;
}
</style>
