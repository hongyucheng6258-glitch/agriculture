/**
 * 表单验证规则
 */

/**
 * 手机号验证规则
 */
export function phoneValidator(rule, value, callback) {
  if (!value) {
    callback(new Error('请输入手机号码'));
    return;
  }
  const reg = /^1[3-9]\d{9}$/;
  if (!reg.test(value)) {
    callback(new Error('请输入正确的手机号码'));
  } else {
    callback();
  }
}

/**
 * 邮箱验证规则
 */
export function emailValidator(rule, value, callback) {
  if (!value) {
    callback(new Error('请输入邮箱地址'));
    return;
  }
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  if (!reg.test(value)) {
    callback(new Error('请输入正确的邮箱地址'));
  } else {
    callback();
  }
}

/**
 * 密码验证规则（至少6位，包含字母和数字）
 */
export function passwordValidator(rule, value, callback) {
  if (!value) {
    callback(new Error('请输入密码'));
    return;
  }
  if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'));
    return;
  }
  const reg = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,}$/;
  if (!reg.test(value)) {
    callback(new Error('密码需包含字母和数字'));
  } else {
    callback();
  }
}

/**
 * 确认密码验证规则
 */
export function confirmPasswordValidator(formRef, fieldName = 'password') {
  return (rule, value, callback) => {
    if (!value) {
      callback(new Error('请再次输入密码'));
      return;
    }
    const form = formRef;
    if (value !== form[fieldName]) {
      callback(new Error('两次输入的密码不一致'));
    } else {
      callback();
    }
  };
}

/**
 * 非负数验证
 */
export function nonNegativeValidator(rule, value, callback) {
  if (value === '' || value === null || value === undefined) {
    callback(new Error('请输入数值'));
    return;
  }
  const num = Number(value);
  if (isNaN(num) || num < 0) {
    callback(new Error('请输入有效的非负数'));
  } else {
    callback();
  }
}

/**
 * 正整数验证
 */
export function positiveIntegerValidator(rule, value, callback) {
  if (value === '' || value === null || value === undefined) {
    callback(new Error('请输入正整数'));
    return;
  }
  const reg = /^[1-9]\d*$/;
  if (!reg.test(String(value))) {
    callback(new Error('请输入有效的正整数'));
  } else {
    callback();
  }
}

/**
 * 常用验证规则集合
 */
export const commonRules = {
  // 必填
  required(message = '此项为必填项') {
    return [
      { required: true, message, trigger: 'blur' }
    ];
  },

  // 必选（下拉框）
  requiredSelect(message = '请选择') {
    return [
      { required: true, message, trigger: 'change' }
    ];
  },

  // 手机号
  phone() {
    return [
      { required: true, validator: phoneValidator, trigger: 'blur' }
    ];
  },

  // 邮箱
  email() {
    return [
      { required: true, validator: emailValidator, trigger: 'blur' }
    ];
  },

  // 密码
  password() {
    return [
      { required: true, validator: passwordValidator, trigger: 'blur' }
    ];
  },

  // 非负数
  nonNegative() {
    return [
      { required: true, validator: nonNegativeValidator, trigger: 'blur' }
    ];
  },

  // 正整数
  positiveInteger() {
    return [
      { required: true, validator: positiveIntegerValidator, trigger: 'blur' }
    ];
  },

  // 用户名（4-20位字母数字下划线）
  username() {
    return [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 4, max: 20, message: '长度在4到20个字符', trigger: 'blur' },
      { pattern: /^[a-zA-Z0-9_]+$/, message: '只允许字母、数字和下划线', trigger: 'blur' }
    ];
  },

  // 长度限制
  minLength(min, message) {
    return [
      { min, message: message || `最少输入${min}个字符`, trigger: 'blur' }
    ];
  },

  maxLength(max, message) {
    return [
      { max, message: message || `最多输入${max}个字符`, trigger: 'blur' }
    ];
  }
};
