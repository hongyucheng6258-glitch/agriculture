import request from './request';

// 导出数据
export function exportData(params) {
  return request({
    url: '/backup/export',
    method: 'get',
    params
  });
}

// 备份数据库
export function backupDatabase() {
  return request({
    url: '/backup/database',
    method: 'post'
  });
}
