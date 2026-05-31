<template>
  <el-dialog title="批量生成溯源码" :visible.sync="dialogVisible" width="500px" @close="handleClose">
    <el-form ref="form" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="网箱" prop="cageId">
        <el-select v-model="form.cageId" placeholder="请选择网箱" style="width: 100%;">
          <el-option
            v-for="item in cageList"
            :key="item.id"
            :label="item.cageCode"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="生成数量" prop="count">
        <el-input-number v-model="form.count" :min="1" :max="100" style="width: 100%;"></el-input-number>
      </el-form-item>
    </el-form>
    <span slot="footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { mapGetters } from 'vuex';
import { batchGenerate } from '@/api/trace';

export default {
  name: 'BatchGenerateDialog',
  data() {
    return {
      dialogVisible: false,
      submitting: false,
      form: {
        cageId: null,
        count: 10
      },
      rules: {
        cageId: [{ required: true, message: '请选择网箱', trigger: 'change' }],
        count: [{ required: true, message: '请输入生成数量', trigger: 'blur' }]
      }
    };
  },
  computed: {
    ...mapGetters(['cageList'])
  },
  methods: {
    open() {
      this.form = {
        cageId: null,
        count: 10
      };
      this.dialogVisible = true;
    },
    handleClose() {
      if (this.$refs.form) {
        this.$refs.form.resetFields();
      }
    },
    async handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return;
        
        this.submitting = true;
        try {
          await batchGenerate(this.form);
          this.$message.success('批量生成成功');
          this.dialogVisible = false;
          this.$emit('success');
        } catch (error) {
          this.$message.error('批量生成失败');
          console.error(error);
        } finally {
          this.submitting = false;
        }
      });
    }
  }
};
</script>
