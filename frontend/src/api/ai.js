import request from './request';

export function chat(message) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: { message }
  });
}

export function chatWithHistory(messages) {
  return request({
    url: '/ai/chat/history',
    method: 'post',
    data: { messages }
  });
}

export function analyzeDashboard() {
  return request({
    url: '/ai/analyze/dashboard',
    method: 'post'
  });
}

export function analyzeWaterQuality() {
  return request({
    url: '/ai/analyze/water-quality',
    method: 'post'
  });
}

export function analyzeAlerts() {
  return request({
    url: '/ai/analyze/alerts',
    method: 'post'
  });
}

export function analyzeFeeding() {
  return request({
    url: '/ai/analyze/feeding',
    method: 'post'
  });
}

export function smartSuggest(type) {
  return request({
    url: '/ai/suggest',
    method: 'post',
    data: { type }
  });
}

export function analyzeDisease() {
  return request({
    url: '/ai/analyze/disease',
    method: 'post'
  });
}

export function analyzeWeather() {
  return request({
    url: '/ai/analyze/weather',
    method: 'post'
  });
}

export function analyzeCage() {
  return request({
    url: '/ai/analyze/cage',
    method: 'post'
  });
}

export function analyzeStock() {
  return request({
    url: '/ai/analyze/stock',
    method: 'post'
  });
}

export function generateComprehensiveReport() {
  return request({
    url: '/ai/report/comprehensive',
    method: 'post'
  });
}
