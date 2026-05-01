/**
 * 日期格式化工具函数
 */

/**
 * 格式化日期
 * @param {Date|string|number} date 日期对象、时间戳或日期字符串
 * @param {string} format 格式化模板，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return '';

  const d = new Date(date);
  if (isNaN(d.getTime())) return '';

  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
}

/**
 * 格式化为日期（不含时间）
 * @param {Date|string|number} date
 * @returns {string}
 */
export function formatDateShort(date) {
  return formatDate(date, 'YYYY-MM-DD');
}

/**
 * 格式化为时间（不含日期）
 * @param {Date|string|number} date
 * @returns {string}
 */
export function formatTime(date) {
  return formatDate(date, 'HH:mm:ss');
}

/**
 * 获取相对时间描述（如：刚刚、5分钟前、1小时前等）
 * @param {Date|string|number} date
 * @returns {string}
 */
export function getRelativeTime(date) {
  if (!date) return '';

  const d = new Date(date);
  if (isNaN(d.getTime())) return '';

  const now = new Date();
  const diff = now.getTime() - d.getTime();

  if (diff < 0) return '刚刚';

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (seconds < 60) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 30) return `${days}天前`;

  return formatDateShort(date);
}

/**
 * 获取最近N天的日期范围
 * @param {number} days 天数
 * @returns {{ start: string, end: string }}
 */
export function getRecentDays(days) {
  const end = new Date();
  const start = new Date();
  start.setDate(start.getDate() - days);

  return {
    start: formatDateShort(start),
    end: formatDateShort(end)
  };
}

/**
 * 将日期字符串解析为Date对象
 * @param {string} dateStr 日期字符串
 * @returns {Date|null}
 */
export function parseDate(dateStr) {
  if (!dateStr) return null;

  const d = new Date(dateStr);
  return isNaN(d.getTime()) ? null : d;
}
