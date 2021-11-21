import axios from 'axios'
import querystring from 'querystring'

// https://demo1.topscrm.cn
// http://192.168.2.202:8080
const baseUrl = process.env.NODE_ENV === 'production' ? '' : 'https://demo1.topscrm.cn'
const apiMap = {
  // 获取钉钉授权参数配置
  'getDingInfo': '/dingding/getSign',
  // 查询钉盘 spaceId
  'getDingSpaceId': '/dingding/cusSpace/getCusSpace',
  // 钉盘授权
  'grantDingCusSpace': '/dingding/cusSpace/getGrantCusSpace',
}

const instance = axios.create({
  baseURL: baseUrl + location.href.includes('demo1.topscrm.cn') ? '' : '',
  timeout: 0,
  headers: {
    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    "Accept": "application/json"
  }
})

// post 请求
function post(apiKey, param, config) {
  if (config && config.json) {
    config.headers = {
      'Content-Type': 'application/json;charset=UTF-8'
    } 
  }

  return instance.post(apiMap[apiKey], config && config.json ? param : querystring.stringify(param), config)
}

// 拦截器
instance.interceptors.request.use(function (config) {
  config.headers.token = window.token
  return config
})

instance.interceptors.response.use(function (rs) {
  return rs.data
})

// 封装 ajax 方法
const ajaxMap = {
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