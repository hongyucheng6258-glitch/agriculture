import request from './request';

// 模拟水质数据
export function simulateWaterQuality(data) {
  return request({
    url: '/simulate/water-quality',
    method: 'post',
    data
  });
}

// 模拟气象数据
export function simulateWeather(data) {
  return request({
    url: '/simulate/weather',
    method: 'post',
    data
  });
}

// 模拟溯源数据
export function simulateTrace(data) {
  return request({
    url: '/simulate/trace',
    method: 'post',
    data
  });
}
