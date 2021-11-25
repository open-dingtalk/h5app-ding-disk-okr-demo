import axios from 'axios'
import querystring from 'querystring'

// https://demo1.topscrm.cn
// http://192.168.2.202:8080
const baseUrl = process.env.NODE_ENV === 'production' ? '' : 'https://demo1.topscrm.cn'
window.isDemo1 = baseUrl.includes('demo1.topscrm.cn') || location.href.includes('demo1.topscrm.cn')

const apiMap = {
  // 获取前置配置信息
  'getAppConfig': '/dingding/demo/getDingConfig',
  // 获取钉钉授权参数配置
  'getDingInfo': '/dingding/demo/getSign',
  // 查询钉盘 spaceId
  'getDingSpaceId': '/dingding/demo/cusSpace/getCusSpace',
  // 钉盘授权
  'grantDingCusSpace': '/dingding/demo/cusSpace/getGrantCusSpace'
}

const instance = axios.create({
  baseURL: baseUrl + (window.isDemo1 ? '/v3' : ''),
  timeout: 0,
  headers: {
    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    "Accept": "application/json"
  }
})

function get(apiKey, param, config) {
  return instance.get(apiMap[apiKey], { params: param }, config)
}

// post 请求
function post(apiKey, param, config) {
  if (config && config.json) {
    config.headers = {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  }
  // 如果需要的话可以传 token
  if (param && window.token) param.token = window.token

  return instance.post(apiMap[apiKey], config && config.json ? param : querystring.stringify(param), config)
}

// 拦截器
instance.interceptors.request.use(function (config) {
  // 如果需要的话可以传 token
  if (window.token) config.headers.token = window.token
  return config
})

instance.interceptors.response.use(function (rs) {
  return rs.data
})

// 封装 ajax 方法
const ajaxMap = {
  // 获取前置配置信息
  getAppConfig(param) {
    return get('getAppConfig', param)
  },
  // 获取钉钉授权参数配置
  getDingInfo(param) {
    return post('getDingInfo', param, {json: true})
  },
  // 查询钉盘spaceId
  getDingSpaceId(param) {
    return post('getDingSpaceId', param, {json: true})
  },
  // 钉盘授权
  grantDingCusSpace(param) {
    return post('grantDingCusSpace', param, {json: true})
  },
}

export default ajaxMap