<template>
  <div id="app">
    <div>
      请输入登录用户的token，用于请求接口：
      <input type="text" v-model="token">
    </div>
    <div>
      请输入dingCorpId：
      <input type="text" v-model="dingCorpId">
    </div>
    <div>
      请输入dingAgentId：
      <input type="text" v-model="dingAgentId">
    </div>
    <div>
      请输入dingUserId：
      <input type="text" v-model="dingUserId">
    </div>
    <div>
      <button @click="upload">上传附件</button>
    </div>

    <div>
      <div 
        class="file-item"
        v-for="item in files" 
        :key="item.url">
        {{item.name}}
        <button @click="preview(item)">预览附件</button>
      </div>
    </div>
  </div>
</template>

<script>
import ajax from './ajax'
export default {
  data() {
    return {
      token: '', // nIb3oKqUMV14g/k0fP/IdblugCKoLgjj7S6Qbv9+o5/W+N/OZ7YK5PZr+0hcfOkh5YLpHKNS/TNxEGrr2ZCp+uVKWH7UUMX6AB2WNS3nxWUaGlfvFWRbO7clVfTD9/AsM8YZQpsn1DacVhd664HQokhtOA+Jz6XRP+lB1rwZEly2PSYDyEYzyVINEHMINeuK
      dingCorpId: '', // dingfba0e203973c37fc4ac5d6980864d335
      dingAgentId: '806630498', // 806630498 971634036
      dingUserId: '2227162436785834', // 0943635716659752
      files: []
    }
  },
  methods: {
    async upload(event) {
      // if (!this.token) {
      //   alert('token 必填')
      //   return
      // }
      // 必须先授权钉钉 api 可用
      await this.config()
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
          onSuccess: function (res) {
            // 上传成功后这里拿到文件相关 res 信息保存
            if (res && res.data && res.data.length) {
              let list = []
              for (let item of res.data) {
                list.push({
                  url: item.fileId,
                  name: item.fileName,
                  size: item.fileSize,
                  fileType: item.fileType,
                })
                this.files = list
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
      if (this.token) window.token = this.token
      
      const rs = await ajax.getDingInfo({
        url: window.encodeURIComponent(location.href.split('#')[0])
      })
      if (rs.success) {
        let params = {
          agentId: rs.data.agentId, // 必填，微应用ID
          corpId: rs.data.corpId,//必填，企业ID
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

        if (!this.dingCorpId) this.dingCorpId = params.corpId
        if (!this.dingAgentId) this.dingAgentId = params.agentId

        dd.config(params)

        dd.ready(() => {})

        dd.error(err => {
          alert(JSON.stringify(err || {msg: '未知错误'}))
        })
      } else {
        alert(rs.message || '获取授权信息失败')
      }
    }
  },
  mounted() {
    // 
    this.config()
  }
}
</script>


<style lang="scss">
</style>
