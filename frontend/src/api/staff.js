import request from './request';

// 获取人员分页列表
export function getList(data) {
  return request({
    url: '/staff/list',
    method: 'post',
    data
  });
}

// 根据ID获取人员详情
export function getById(id) {
  return request({
    url: `/staff/${id}`,
    method: 'get'
  });
}

// 新增人员
export function add(data) {
  return request({
    url: '/staff',
    method: 'post',
    data
  });
}

// 更新人员信息
export function update(data) {
  return request({
    url: `/staff/${data.id}`,
    method: 'put',
    data
  });
}

// 删除人员
export function deleteRecord(id) {
  return request({
    url: `/staff/${id}`,
    method: 'delete'
  });
}
