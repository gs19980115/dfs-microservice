package com.njugs.dfs.namenode;

import com.netflix.appinfo.InstanceInfo;
import com.njugs.dfs.block.BlockEntity;
import com.njugs.dfs.datanode.DataNodeEntity;
import com.njugs.dfs.namespace.INode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.server.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class NameNodeController {

    // namenode 业务层逻辑
    @Autowired
    private NameNodeService nameNodeService;

    // 获取前端发来的http请求
    @Autowired
    private HttpServletRequest request;

    /****************  以下三个方法是测试用的 ****************/
    @GetMapping("/get-all-files")
    public List<INode> getAllFiles(){
        return nameNodeService.getAllFiles();
    }

    @GetMapping("/get-all-blocks")
    public List<BlockEntity> getAllBlocks(){
        return nameNodeService.getAllBlocks();
    }

    @GetMapping("/get-all-datanodes")
    public List<DataNodeEntity>  getAllDataNodes(){
        return nameNodeService.getAllDataNodes();
    }


    /**
     * 上传文件
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/**")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String iNodeName = request.getRequestURI() + file.getOriginalFilename();
        iNodeName = URLDecoder.decode(iNodeName, "UTF-8");
        System.out.println("上传文件"+iNodeName);
        nameNodeService.handleUploadedFile(file, iNodeName);
        return ResponseEntity.ok().build();
    }


    /**
     * 下载文件
     * @param request
     * @return
     * @throws IOException
     */
    @GetMapping("/**")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) throws IOException {

        String iNodeName = request.getServletPath();

        // 标记一下其他方法功能
        // 以Get http://127.0.0.1:8761/download/gaygay/gay.txt为例
        // System.out.println(request.getRequestURI()); // /download/gaygay/gay2.txt
        // System.out.println(request.getRequestURL()); // http://127.0.0.1:8761/download/gaygay/gay2.txt
        // System.out.println(request.getServletPath());// /download/gaygay/gay2.txt

        iNodeName = iNodeName.replaceFirst("/download", "");
        System.out.println("下载文件"+iNodeName);

        Resource file = nameNodeService.loadFileAsResource(iNodeName);
        System.out.println(file.getFilename());
        HttpHeaders headers = new HttpHeaders();
        Path path = Paths.get(iNodeName);
        String filename = path.getFileName().toString();
        filename = new String(filename.getBytes("UTF-8"), "iso-8859-1");
        headers.add("Content-Disposition", "attachment;filename=\"" + filename + "\"");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("multipart/form-data")).body(file);
    }

    /**
     * 删除文件
     * @param request
     * @throws UnsupportedEncodingException
     */
    @DeleteMapping("/**")
    public void deleteFile(HttpServletRequest request) throws UnsupportedEncodingException {
        String iNodeName = request.getServletPath();
        iNodeName = URLDecoder.decode(iNodeName, "UTF-8");
        System.out.println("删除文件"+iNodeName);
        nameNodeService.deleteFile(iNodeName);
    }

    /**
     * 创建新文件夹
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping(value = "/create-new-folder/**")
    public String createNewFolder() throws UnsupportedEncodingException {
        String iNodeName = request.getRequestURI().replaceFirst("/create-new-folder","");
        iNodeName = URLDecoder.decode(iNodeName, "UTF-8");
        System.out.println("创建文件夹" + iNodeName);
        nameNodeService.createINodeDirectory(iNodeName);
        return "ok";
    }

    @DeleteMapping("/delete-folder/**")
    public void deleteFolder() throws UnsupportedEncodingException {
        String iNodeName = request.getRequestURI().replaceFirst("/delete-folder", "");
        iNodeName = URLDecoder.decode(iNodeName, "UTF-8");
        System.out.println("删除文件夹" + iNodeName);
        nameNodeService.deleteFolder(iNodeName);
    }

    /**
     * 获取该路径下的所有文件和文件夹
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/get-contents-in-folder/**")
    @ResponseBody
    public List<INode> getContentsInLocation() throws UnsupportedEncodingException {
        String location = request.getServletPath().replaceFirst("/get-contents-in-folder" , "");
        location = URLDecoder.decode(location, "UTF-8");
        System.out.println("获取目录"+location);
        return nameNodeService.getFilesInLocation(location);
    }


    /*****************   以下五个函数是Eureka服务发现相关的   *****************/

    /**
     * datanode下线，进行数据迁移，保证负载均衡
     * @param event
     * @throws IOException
     */
    @EventListener
    public void listen(EurekaInstanceCanceledEvent event) throws IOException {
        String dataNodeUrl = "http://" + event.getServerId() + "/";
        System.out.println("我要开始删除这个节点"+dataNodeUrl+"，并且迁移数据");
        nameNodeService.cancelDataNode(dataNodeUrl);
        System.err.println(event.getServerId() + "\t" + event.getAppName() + " 服务下线");
    }

    /**
     * datanode 注册，纳入整个系统
     * @param event
     */
    @EventListener
    public void listen(EurekaInstanceRegisteredEvent event) {
        InstanceInfo instanceInfo = event.getInstanceInfo();
        String dataNodeUrl = instanceInfo.getHomePageUrl();
        System.err.println(dataNodeUrl + "进行注册");
        nameNodeService.registerNewDataNode(dataNodeUrl);
    }

    /**
     * datanode 续约，告诉namenode他还在
     * @param event
     */
    @EventListener
    public void listen(EurekaInstanceRenewedEvent event) {
        System.err.println(event.getServerId() + "\t" + event.getAppName() + " 服务进行续约");
//        nameNodeService.registerNewDataNode(dataNodeUrl);

    }

    /**
     * namenode可以开始接受datanode注册
     * @param event
     */
    @EventListener
    public void listen(EurekaRegistryAvailableEvent event) {
        System.err.println("注册中心 启动");
    }

    /**
     * namenode启动
     * @param event
     */
    @EventListener
    public void listen(EurekaServerStartedEvent event) {
        System.err.println("Eureka Server 启动");
    }
}
