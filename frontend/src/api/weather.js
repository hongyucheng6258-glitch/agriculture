import request from './request';

// 获取气象数据分页列表
export function getList(data) {
  return request({
    url: '/weather/list',
    method: 'post',
    data
  });
}

// 新增气象数据
export function add(data) {
  return request({
    url: '/weather',
    method: 'post',
    data
  });
}

// 批量新增气象数据
export function batchAdd(data) {
  return request({
    url: '/weather/batch',
    method: 'post',
    data
  });
}

// 更新气象数据
export function update(data) {
  return request({
    url: `/weather/${data.id}`,
    method: 'put',
    data
  });
}

// 删除气象数据
export function deleteRecord(id) {
  return request({
    url: `/weather/${id}`,
    method: 'delete'
  });
}

// 模拟生成气象数据
export function simulateWeather(data) {
  return request({
    url: '/simulate/weather',
    method: 'post',
    data
  });
}
