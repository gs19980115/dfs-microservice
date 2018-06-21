<template align="center">
  <div id="app"
        v-loading.fullscreen.lock="loading"
        element-loading-text="疯狂加载中...(该不会出bug了吧)">


    <h2><font face="PingFang SC" size="17">A distributed file system built with microservices</font> </h2>
    <!-- style="height: 500px; width: 800px; border: 1px solid #eee"> -->
    <el-container>
      <el-aside width="200px">
        <h1>导航栏</h1>
        <el-popover
          placement="right"
          title="功能"
          width="200"
          trigger="hover"
          content="查看文件系统">
          <el-button type="primary" plain slot="reference" @click="viewFileSystem">File System</el-button>
        </el-popover>
        <el-popover
          placement="right"
          title="功能"
          width="200"
          trigger="hover"
          content="监测 datanode block的数目">
          <el-button type="primary" plain slot="reference" @click="viewDataNodeMonitor">DataNode Monitor</el-button>
        </el-popover>
      </el-aside>

      <div v-if="showFileSystem">
        <el-container>
          <el-header>
            <!-- 文件操作栏 -->
            <el-row>

              <div :v-show="currentLocation != '/'">
                <el-col :span="4">
                  <el-button type="primary" @click="goBackToParentFolder">返回上一级</el-button>
                </el-col>
              </div>

              <el-col :span="2">
                <!-- action 参数参考 https://blog.csdn.net/Longyu144714/article/details/78110886 -->
                <el-upload
                  class="upload-demo"
                  :action="uploadURL()"
                  :on-success="handleFileUploadExceed"
                  :on-progress="handleFileUploadProgress"
                  :show-file-list="false"
                  :file-list="fileList">
                  <el-button type="primary" >上传<i class="el-icon-upload el-icon--right"></i></el-button>
                </el-upload>
              </el-col>

              <el-col :span="4">
              <el-button type="primary" @click="createFolder">新建文件夹<i class="el-icon-circle-plus-outline el-icon--right"></i></el-button>
              </el-col>

              <el-col :span="6">
                <div v-if="list==true && selectd.file.id != -1">
                  <div v-if="selectd.file.directory">
                      <el-button type="primary" plain @click="changeFolder(selectd.file)">进入</el-button>
                      <el-button type="danger" plain @click="deleteFolderMsgBox(selectd.file)">删除</el-button>
                  </div>
                  <div v-else>
                    <el-button-group>
                      <el-button type="primary" icon="el-icon-download" @click="downLoadFile(selectd.file)">下载</el-button>
                      <el-button type="primary" icon="el-icon-delete">删除</el-button>
                      <el-button type="primary" icon="el-icon-more-outline">更多</el-button>
                    </el-button-group>
                  </div>
                </div>
              </el-col>

              <div v-if="!list">
                <el-col :span="4">
                  <el-button type="info" icon="el-icon-menu" @click="changeViewModel"></el-button>
                </el-col>
              </div>
              <div v-else>
                <el-col :span="4">
                  <el-button type="info" icon="el-icon-tickets" @click="changeViewModel"></el-button>
                </el-col>
              </div>
            </el-row>
          </el-header>


          <el-main>


            <!-- 图标网格模式 -->
            <div v-if="list">
              <el-row :gutter="20">
              <!-- 上传图标组件 -->
              <el-col :span="5" >
                <div>
                  <el-upload
                    class="avatar-uploader"
                    :action="uploadURL()"
                    :show-file-list="false"
                    :on-success="handleFileUploadExceed">
                    <i class="el-icon-plus avatar-uploader-icon"></i>
                  </el-upload>
                </div>
              </el-col>

              <!-- 文件图标网格 -->

              <el-col :span="5" align="bottom" class="grid-content" v-for="file in files" :key="file.filename">
                <div v-if="checkFilePrefix(file)">
                  <div @click="chooseFile(file)" @dblclick="changeFolder(file)" class="file-icon bg-purple-light">

                    <div class="icon-selected" align="left top" v-if="selectd.file.id==file.id">
                    <el-button type="primary" icon="el-icon-check" size="mini" circle></el-button>
                    </div>
                    <div class="icon-no-selected" v-if="selectd.file.id!=file.id">
                    <el-button type="info" icon="el-icon-check" size="mini" circle></el-button>
                    </div>
                    <div class="file-picture" >
                    <img :src="'../static/'+(file.filetype)+'.png'" width="40%" align="center">
                    </div>
                    <div>
                      <p class="file-name" slot="reference"><font size="2" face="微软雅黑"> {{file.filename}}</font> </p>
                        <el-popover
                          placement="top"
                          title="文件名"
                          width="200"
                          trigger="hover">
                          <div style="widht:100%;height:100%;word-wrap: break-word;">
                          <p>{{file.filename}}</p>
                          </div>
                          <el-button slot="reference" type="text">...</el-button>
                        </el-popover>
                    </div>
                    <div v-if="file.directory"></div>
                  </div>
                </div>
              </el-col>
              </el-row>
            </div>

          <!-- 列表模式 -->
          <div v-else>
            <el-table
              ref="singleTable"
              :data="files"
              tooltip-effect="dark"
              style="width: 100%"
              highlight-current-row
              @current-change="handleCurrentChange"
              >
              <el-table-column label="类型" align="left">
                <template slot-scope="scope">
                  <img :src="'../static/'+(scope.row.filetype)+'.png'" width="40" height="40">
                </template>
              </el-table-column>
              <el-table-column prop="filename" label="文件名" width="200" align="left"></el-table-column>
              <el-table-column prop="numBytes" label="文件大小" width="200" align="left"></el-table-column>
              <el-table-column label="操作" width="500" align="left">
                <template slot-scope="scope">
                  <div v-if="!scope.row.directory">
                    <el-button size="mini" type="primary" @click="downLoadFile(scope.row)">下载</el-button>
                    <el-button size="mini" type="danger" @click="deleteFileMsgBox(scope.row)">删除</el-button>
                  </div>
                  <i v-else>
                    <el-button size="mini" type="primary" plain @click="changeFolder(scope.row)">进入</el-button>
                    <el-button size="mini" type="danger" plain @click="deleteFolderMsgBox(scope.row)">删除</el-button>
                  </i>
                </template>
              </el-table-column>
            </el-table>
          </div>

          </el-main>
        </el-container>
      </div>

      <div v-else>
        <el-container>
          <el-header>
            <el-row>
              <el-col :span="4">
              <el-button type="primary" @click="getAllDatanodes">刷新</el-button>
              </el-col>
            </el-row>
          </el-header>
          <el-main>
            <ve-line :data="dataNodeChartData" :settings="chartSettings"></ve-line>
          </el-main>
        </el-container>
      </div>
    </el-container>



  </div>
</template>

<script>
import axios from 'axios'
export default {
  name: 'App',

  data() {
    return {

      loading : false,

      showDataNodeMonitor : false,

      showFileSystem : true,

      currentLocation : "/",

      currentRow: null,

      selectd : {
        file :{
          id : -1,
          location : "",
          filename : "Foo",
          numBlocks : 0,
          numBytes : 0,
          directory : true,
          filetype : "directory"
        }
      },

      inputFilePrefix: "",

      list : false,

      fileList :[],

      files : [
        {
          id : 0,
          location : "",
          filename : "Foo",
          numBlocks : 0,
          numBytes : 0,
          directory : true,
          filetype : "directory"
        },
        {
          id : 1,
          location : "",
          filename : "Bar",
          numBlocks : 0,
          numBytes : 0,
          directory : false,
          filetype : "zip"
        }
      ],
      dataNodeChartData: {
          columns: ['url', 'numBlocks','valid'],
          rows: [
            {
              'url' : 'http:/127.0.0.1:8081',
              'numBlocks' : 2,
              'valid' : true
            },
            {
              'url' : 'http:/127.0.0.1:8082',
              'numBlocks' : 4,
              'valid' : true
            },
            {
              'url' : 'http:/127.0.0.1:8083',
              'numBlocks' : 5,
              'valid' : true
            }
          ]
        },
      chartSettings:{
        metrics: ['numBlocks'],
        dimension: ['url'],
        area: true
      }
    }
  },

  methods : {

    test() {
      alert("hbj,knaer")
    },

    // 视图控制
    viewFileSystem(){
      this.showDataNodeMonitor = false
      this.showFileSystem = true
    },
    viewDataNodeMonitor(){
      this.showDataNodeMonitor = true
      this.showFileSystem = false
      this.getAllDatanodes()
    },
    changeViewModel(){
      this.list = !this.list
    },

    chooseFile(file) {
      if(this.selectd.file.id == file.id){
        this.selectd.file.id = -1;
      } else{
        this.selectd.file.id = file.id
        this.selectd.file.directory = file.directory
        this.selectd.file.filename = file.filename
        this.selectd.file.location = file.location
        this.selectd.file.numBlocks = file.numBlocks
        this.selectd.file.numBytes = file.numBytes
        this.selectd.file.filetype = file.filetype
      }
      ////console.log(this.selectd.file.id)
    },

    // cd 切换目录
    changeFolder(file) {
      if(file.directory){
        this.currentLocation = this.currentLocation + file.filename + "/"

        ////console.log(this.currentLocation)
        this.$nextTick(() => {
          this.selectd.file.id = -1
          this.getContentsInCurrentLocation()
        })
        this.getContentsInCurrentLocation()
      }
    },
    notRootFolder() {
      if(this.location == "/")
        return false
      else
        return true
    },

    goBackToParentFolder() {
      if(this.currentLocation == "/")
        return;
      else{
        // /a/b/c/ => /a/b/
        var location = this.currentLocation.lastIndexOf("/")
        var parant = this.currentLocation.substring(0, location)
        //console.log(parant)
        location = parant.lastIndexOf("/")
        parant = parant.substring(0, location + 1)
        //console.log(parant)
        if(parant == "")
          this.currentLocation = "/"
        else
          this.currentLocation = parant
      }
      //console.log("父目录是" + this.currentLocation)
      this.$nextTick(() => {
        this.getContentsInCurrentLocation()
      })
    },

    createFolder(){
      //console.log(this.currentLocation)
      this.$prompt('请输入文件夹名称', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^[^\\\\\\/:*?\\"<>|]+$/,
        inputErrorMessage: '文件夹名称格式不正确',
      }).then(({ value }) => {

        var url = "http://127.0.0.1:8761/create-new-folder" + this.currentLocation + value

        //console.log("创建文件夹链接" + url)
        axios.post(url).then(() => {
          this.getContentsInCurrentLocation()
          this.$nextTick(() => {
            this.getContentsInCurrentLocation()
          })
        })
        // 不放心，再获取一遍(废代码)
        this.getContentsInCurrentLocation()
        this.$message({
          type: 'success',
          message: '文件夹 ' + value + '新建成功！'
        });
      }).catch(() => {
        var url = "http://127.0.0.1:8761/create-new-folder" + this.currentLocation

        //console.log("创建文件夹链接" + url)

        this.$message({
          type: 'info',
          message: '取消输入'
        });
      });
    },

    deleteFolderMsgBox(file){
      this.$confirm('此操作将永久删除该文件夹, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          //console.log("下面我要删除这个文件了")
          this.deleteFolder(file)
          this.loading = true
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
          this.getContentsInCurrentLocation()
          this.loading = false
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });
        });
    },

    deleteFolder(file){
      var url = "http://127.0.0.1:8761/delete-folder" + this.currentLocation + file.filename
      //console.log("delete 请求的网址是" + url)
      axios.delete(url).then((response) => {
        this.getContentsInCurrentLocation();
      })
    },

    // 匹配文件前缀用的，时间关系，没完成这个功能，目前还没用上
    checkFilePrefix(file){
      var fdStart = file.filename.indexOf(this.inputFilePrefix);
      if(fdStart == 0){
        return true;
      }else if(fdStart == -1){
        return false
      }
      return false
    },

    deleteFileMsgBox(file){
      this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          //console.log("下面我要删除这个文件了")
          this.deleteFile(file)
          this.loading = true
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
          this.$nextTick(() => {
            this.getContentsInCurrentLocation()
            this.loading = false
          })

        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });
        });
    },

    deleteFile(file){
      var url = "http://127.0.0.1:8761" + this.currentLocation + file.filename
      //console.log("delete 请求的网址是" + url)
      axios.delete(url).then((response) => {
        this.getContentsInCurrentLocation();
      })
    },
    // 获取当前路径内容
    getContentsInCurrentLocation() {
      //console.log("getContentFilesInPath")
      var url
      if(this.currentLocation == "/")
        url = "http://127.0.0.1:8761/get-contents-in-folder/"
      else{
        // "/a/b/c/"   =>   "/a/b/c"
        var location = this.currentLocation.lastIndexOf("/")
        var parant = this.currentLocation.substring(0, location)

        url = "http://127.0.0.1:8761/get-contents-in-folder" + parant
      }

      //console.log(url)
      axios.get(url).then(
        (response) => {
          //console.log(response)
          this.$nextTick(() => {
             this.files = JSON.parse(JSON.stringify(response.data))
          })
          // TODO 测试files有没有更新
          //console.log(JSON.parse(JSON.stringify(response.data)))
        })
    },

    // 文件下载 参考https://thewebtier.com/snippets/download-files-with-axios/
    downLoadFile(file){
      if(file.id == -1){
          this.$message({
            type: 'danger',
            message: '未选择文件!'
          });
      }
      var downloadUrl = "http://127.0.0.1:8761" + this.currentLocation + file.filename
      axios({
        url: downloadUrl,
        method: 'GET',
        responseType: 'blob', // important
      }).then((response) => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', file.filename); //or any other extension
        document.body.appendChild(link);
        link.click();
      });
    },



    // 文件上传结束，更新一下页面
    handleFileUploadExceed(){
      this.loading = false

      this.getContentsInCurrentLocation()
    },
    handleFileUploadProgress(){
      this.loading = true
    },
    setCurrent(row) {
      this.$refs.singleTable.setCurrentRow(row);
    },
    handleCurrentChange(val) {
      this.currentRow = val;
    },

    uploadURL(){
      //console.log("上传文件的Post请求URL是："+ "http://127.0.0.1:8761" + this.currentLocation)
      return "http://127.0.0.1:8761" + this.currentLocation
    },

    getAllDatanodes(){
      //console.log("获取datanode信息")
      var url = "http://127.0.0.1:8761/get-all-datanodes"
      axios.get(url).then(response => {
          this.$nextTick(() => {
             this.dataNodeChartData.rows = JSON.parse(JSON.stringify(response.data))
             //console.log(this.dataNodeChartData.rows)
          })
      })
    }

  },

  mounted(){
    this.getContentsInCurrentLocation();
  }

}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif,"微软雅黑";
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  /* width: 900px;    这里可能要改 */
  color: #2c3e50;
  /* margin-top: 60px; */
}
</style>

<style>

    .container {
      max-width: 800px;
      /* margin:  0 auto; */
    }

  /* Container 布局容器 */
  .el-header, .el-footer {
    background-color: #B3C0D1;
    color: #333;
    text-align: center;
    line-height: 60px;
  }

  .el-aside {
    background-color: #D3DCE6;
    color: #333;
    text-align: center;
    line-height: 50px;
  }

  .el-main {
    background-color: #E9EEF3;
    color: #333;
    text-align: center;
    line-height: 30px;
    height: 550px;
    width: 1200px;
  }

  body > .el-container {
    margin-bottom: 40px;
  }

  .el-container:nth-child(5) .el-aside,
  .el-container:nth-child(6) .el-aside {
    line-height: 260px;
  }

  .el-container:nth-child(7) .el-aside {
    line-height: 320px;
  }


  /* Layout 布局 */
  .el-row {
    margin-bottom: 20px;
    &:last-child {
      margin-bottom: 0;
    }
  }
  .el-col {
    border-radius: 4px;
    /* span: 4;
    offset: 4 */
  }
  .bg-purple-dark {
    background: #99a9bf;
  }
  .bg-purple {
    background: #d3dce6;
  }
  .bg-purple-light {
    background: #e5e9f2;
  }
  .grid-content {
    border-radius: 4px;
    min-height: 200px;
  }
  .row-bg {
    padding: 10px 0;
    background-color: #f9fafc;
  }
  /* 上传文件的图标 */
    .avatar-uploader .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }
  .avatar-uploader .el-upload:hover {
    border-color: #409EFF;
  }
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 190px;
    height: 170px;
    line-height: 170px;
    text-align: center;
  }
  .avatar {
    width: 190px;
    height: 190px;
    display: block;
  }

  /* 我自定义的，看起来很蠢吧 */

  h1,h2,h3,h4 {
    font-family : "微软雅黑"
  }

  .icon-selected {
    /* display: none; //元素默认是隐藏的 */
    margin-top: 10px;
    margin-right: 50px;
    width:  50px;
    height: 35px;
  }
  .icon-no-selected {
    /* display: none; //元素默认是隐藏的 */
    visibility: hidden;
    margin-top: 10px;
    margin-right: 50px;
    width:  50px;
    height: 35px;
  }
  .file-icon {
    border: 2px dashed #d9d9d9;
    /* background:#edf20f; */
  }
  .file-picture{
    margin-top: 5px;
    border : 20px;
  }
  .file-icon:hover .icon-no-selected {
    visibility: visible;
  }
  .file-name {
    width: 160px;
    height: 25px;
    overflow:hidden;
    display:block;
    float: left;
  }
  .dot3_butten{
    width: 40px;
    height: 25px;
    overflow:hidden;
    display:block;
    float: right;
  }

  .contain { width:200px; height:160px; margin:20px; padding:10px 20px 10px 20px; border:1px solid #FF6600; text-align:center}
  .inner_contain { width:150px; height:30px; border:1px solid #009966}
</style>