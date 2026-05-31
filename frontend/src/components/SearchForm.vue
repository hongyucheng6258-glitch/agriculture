<template>
  <div class="search-form-container">
    <el-form
      ref="searchForm"
      :inline="true"
      :model="searchParams"
      class="search-form"
    >
      <slot :searchParams="searchParams" />
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        <slot name="extra-buttons" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'SearchForm',
  props: {
    initParams: {
      type: Object,
      default() {
        return {};
      }
    }
  },
  data() {
    return {
      searchParams: { ...this.initParams }
    };
  },
  watch: {
    initParams: {
      handler(val) {
        this.searchParams = { ...val };
      },
      deep: true
    }
  },
  methods: {
    handleSearch() {
      this.$emit('search', { ...this.searchParams });
    },
    handleReset() {
      this.searchParams = { ...this.initParams };
      if (this.$refs.searchForm) {
        this.$refs.searchForm.resetFields();
      }
      this.$emit('reset');
      this.$emit('search', { ...this.searchParams });
    },
    getSearchParams() {
      return { ...this.searchParams };
    }
  }
};
</script>

<style scoped>
.search-form-container {
  padding-bottom: 15px;
}

.search-form .el-input,
.search-form .el-select {
  width: 200px;
}

@media screen and (max-width: 768px) {
  .search-form .el-input,
  .search-form .el-select {
    width: 100%;
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>
