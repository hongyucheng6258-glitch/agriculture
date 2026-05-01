import request from './request';

// 获取溯源记录分页列表
export function getList(data) {
  return request({
    url: '/trace/list',
    method: 'post',
    data
  });
}

// 根据ID获取溯源记录详情
export function getById(id) {
  return request({
    url: `/trace/${id}`,
    method: 'get'
  });
}

// 获取溯源详情（含关联数据）
export function getDetail(id) {
  return request({
    url: `/trace/${id}/detail`,
    method: 'get'
  });
}

// 新增溯源记录
export function add(data) {
  return request({
    url: '/trace',
    method: 'post',
    data
  });
}

// 更新溯源记录
export function update(data) {
  return request({
    url: `/trace/${data.id}`,
    method: 'put',
    data
  });
}

// 删除溯源记录
export function deleteById(id) {
  return request({
    url: `/trace/${id}`,
    method: 'delete'
  });
}

// 审核溯源记录
export function audit(id, data) {
  return request({
    url: `/trace/${id}/audit`,
    method: 'post',
    data
  });
}

// 批量生成溯源记录
export function batchGenerate(data) {
  return request({
    url: '/trace/batch',
    method: 'post',
    data
  });
}

// 根据溯源码查询
export function queryByCode(code) {
  return request({
    url: '/trace/query',
    method: 'get',
    params: { traceCode: code }
  });
}

// 根据消费者信息查询
export function searchByConsumer(keyword) {
  return request({
    url: '/trace/consumer/search',
    method: 'get',
    params: { keyword: keyword }
  });
}

// 获取溯源码二维码
export function getQRCodeUrl(id) {
  return request.defaults.baseURL + `/trace/${id}/qrcode`;
}
