<template>
  <div id="app">
    <div class="header">
      <img class="logo" :src="require('@/assets/logo.png')">
      <b>北极星OKR模版</b>
    </div>
    <div class="container">
      <div class="main">
        <el-form ref="form">
          <el-form-item label="上传附件: ">
            <el-button type="primary" size="small" @click="upload">上传</el-button>
          </el-form-item>
          <el-form-item label="附件列表: ">
            <div
              class="file-item"
              v-for="item in files" 
              :key="item.url">
              <div class="icon el-icon-notebook-2"></div>
              <div>{{item.name}}</div>
              <el-button class="preview-btn" type="text" @click="preview(item)">预览</el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import ajax from './ajax'
export default {
  data() {
    return {
      dingCorpId: '',
      dingAgentId: '',
      dingUserId: '',
      files: []
    }
  },
  methods: {
    async upload(event) {
      setTimeout(() => {
        this.uploadFile({})
      }, 30)
    },
    // 获取钉盘 spaceId，用于钉盘的其他业务的前置条件
    async getDingSpaceId() {
      // 调用自己后台接口获取 spaceId
      const rs = await ajax.getDingSpaceId({
        dingAgentId: this.dingAgentId,
        dingCorpId: this.dingCorpId,
        dingDomain: 'aaa'
      })
      if (rs.success) {
        return rs.data // 后端返回的 spaceId
      } else {
        return ''
      }
    },
    // 钉盘授权
    async grantDingCusSpace({ type, fileIds }) {
      // 调用后台代码授权钉盘
      const rs = await ajax.grantDingCusSpace({
        dingUserId: this.dingUserId,
        dingAgentId: this.dingAgentId,
        dingCorpId: this.dingCorpId,
        fileIds: fileIds || null,
        grantType: type, // add 或者是 download(为 download 时必须传 fileIds)
        dingDomain: 'aaa'
      })
      // 返回是否授权成功
      return !!rs.success
    },
    // 上传文件至钉盘
    async uploadFile(param) {
      // 这里要先获取到钉盘授权的 spaceId 和 grant 授权信息
      let spaceId = await this.getDingSpaceId()
      let isSuccess = await this.grantDingCusSpace({
        type: 'add'
      })
      if (spaceId && isSuccess) {
        dd.biz.util.uploadAttachment({
          multiple: !!param.multiple,
          image: {
            multiple: !!param.multiple,
            compress: param.compress !== false,
            max: param.max || 1,
            spaceId: spaceId,
          },
          space: {
            corpId: this.dingCorpId,
            spaceId: spaceId,
            max: param.max || 1,
          },
          file: {
            spaceId: spaceId,
            max: param.max || 1,
          },
          types: param.types || ['file', 'space'], // 若是纯图片上传暂不对接钉钉，因钉钉不支持缩略图预览
          onSuccess: res => {
            // 上传成功后这里拿到文件相关 res 信息保存
            if (res && res.data && res.data.length) {
              for (let item of res.data) {
                this.files.push({
                  url: item.fileId,
                  name: item.fileName,
                  size: item.fileSize,
                  fileType: item.fileType,
                })
              }
            }
          },
          onFail: function (err) {
            alert(JSON.stringify(err || {msg: '未知错误'}))
          },
        })
      }
    },
    // 预览钉盘文件
    async preview(param) {
      // 这里要先获取到钉盘授权的 spaceId 和 grant 授权信息
      let spaceId = await this.getDingSpaceId()
      // 文件的id来源于之前上传钉盘成功后返回的文件id
      let fileId = param.url
      let isSuccess = await this.grantDingCusSpace({
        type: 'download',
        fileIds: fileId,
      })
      if (spaceId && isSuccess) {
        dd.biz.cspace.preview({
          corpId: this.dingCorpId, // 这个是企业id，一般我们存全局的
          fileId: fileId,
          fileName: param.name,
          fileSize: param.size,
          fileType: param.fileType,
          spaceId: spaceId,
          onSuccess: function (result) {

          },
          onFail: function (err) {
            alert(JSON.stringify(err || {msg: '未知错误'}))
          },
        })
      }
    },
    // 授权钉钉 api 方法可用，最先调用
    async config() {
      let rs = await ajax.getDingInfo({
        authCode: this.code,
        url: location.href.split('#')[0]
      })
      if (rs.success) {
        let params = {
          agentId: rs.data.agentId || rs.data.dingAgentId || this.dingAgentId, // 必填，微应用ID
          corpId: rs.data.corpId || rs.data.dingCorpId || this.dingCorpId,//必填，企业ID
          timeStamp: rs.data.timeStamp, // 必填，生成签名的时间戳
          nonceStr: rs.data.nonceStr, // 必填，生成签名的随机串
          signature: rs.data.signature, // 必填，签名
          type: 0, // 选填。0表示微应用的jsapi,1表示服务窗的jsapi；不填默认为0。该参数从dingtalk.js的0.8.3版本开始支持
          jsApiList : [ // 必填，需要使用的jsapi列表，注意：不要带 dd
            'biz.util.uploadAttachment',
            'biz.cspace.chooseSpaceDir',
            'biz.cspace.saveFile',
            'biz.cspace.preview'
          ]
        }

        if (params.corpId) this.dingCorpId = params.corpId
        if (params.agentId) this.dingAgentId = params.agentId
        if (rs.data.dingUserId) this.dingUserId = rs.data.dingUserId
        // 如果接口需要 token 我们自己开发需要准备好接口请求的 token
        if (rs.data.token) window.token = rs.data.token

        dd.config(params)
      } else {
        alert(rs.message || 'err getDingInfo')
      }
    }
  },
  async mounted() {
    // 获取 corpId 和 agentId
    let rs = await ajax.getAppConfig()
    if (rs.success) {
      dd.ready(() => {
        this.dingCorpId = rs.data.dingCorpId
        this.dingAgentId = rs.data.dingAgentId

        dd.runtime.permission.requestAuthCode({
          corpId: this.dingCorpId,
          onSuccess: info => {
            this.code = info.code
            this.config()
          },
          onFail: err => {
            alert(JSON.stringify(err || {msg: 'dd.runtime.permission.requestAuthCode'}))
          }
        })
      })

      dd.error(err => {
        alert(JSON.stringify(err || {msg: 'dd.error'}))
      })
    } else {
      alert(rs.message || 'err getAppConfig')
    }
  }
}
</script>


<style lang="scss">
* {
  box-sizing: border-box;
}
html,
body {
  padding: 0;
  margin: 0;
  height: 100vh;
  background-color: #f3f3f3;
}

#app {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.header {
  display: flex;
  border-bottom: 1px solid #eee;
  justify-content: center;
  align-items: center;
  height: 45px;
  background-color: #fff;
  flex-shrink: 0;
  .logo {
    width: 20px;
    margin-right: 6px;
    vertical-align: middle;
  }
}

.container {
  height: calc(100% - 45px);
  overflow: auto;
  .main {
    width: 750px;
    height: 100%;
    background-color: #fff;
    margin: 0 auto;
    padding: 20px 30px;
  }
  .el-form-item {
    display: flex;
  }
  .file-item {
    display: flex;
    align-items: center;
    .icon {
      margin-right: 6px;
    }
    .preview-btn {
      margin-left: 10px;
      padding: 0;
    }
    + .file-item {
      margin-top: -5px;
    }
  }
}
</style>
