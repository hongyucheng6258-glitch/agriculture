import request from './request';

// 获取告警分页列表 - 使用POST避免中文参数URL编码400错误
export function getList(params) {
  return request({
    url: '/alert/list',
    method: 'post',
    data: params
  });
}

// 获取未处理告警列表
export function getUnhandled(params) {
  return request({
    url: '/alert/unhandled',
    method: 'get',
    params
  });
}

// 根据ID获取告警详情
export function getById(id) {
  return request({
    url: `/alert/${id}`,
    method: 'get'
  });
}

// 处理告警
export function handleAlert(data) {
  return request({
    url: `/alert/${data.id}/handle`,
    method: 'put',
    data
  });
}

// 获取未处理告警数量
export function getCount() {
  return request({
    url: '/alert/count',
    method: 'get'
  });
}
