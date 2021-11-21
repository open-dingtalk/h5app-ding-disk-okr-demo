// 覆盖 webpack 配置
module.exports = {
  // 路径配置
  publicPath: './',
  // sourcemap
  productionSourceMap: false,
  // devServer: {
  //   proxy: {
  //     '/dingding/getSign': {
  //       target: 'http://192.168.2.202:8080/dingding/getSign',
  //       changeOrigin: true
  //     }
  //   }
  // }
}