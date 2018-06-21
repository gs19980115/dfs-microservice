package com.njugs.dfs.namenode;

import com.njugs.dfs.block.BlockEntity;
import com.njugs.dfs.block.BlockService;
import com.njugs.dfs.datanode.DataNodeEntity;
import com.njugs.dfs.datanode.DataNodeService;
import com.njugs.dfs.namespace.INode;
import com.njugs.dfs.namespace.INodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 这里的命名参考了HDFS中的部分命名
 * 参考https://tech.meituan.com/namenode.html
 */
@Service
public class NameNodeService {

    // block 大小
    @Value(value="${block.default-size}")
    public int BLOCK_SIZE;

    // 副本数
    @Value(value="${block.default-replicas}")
    public int REPLICA_NUM;

    private final String FILE_DIRECTORY = "Files/";

    // 管理数据存储节点
    @Autowired
    private DataNodeService dataNodeManager;

    // 模拟文件系统
    @Autowired
    private INodeService iNodeManager;

    // 管理 blocks
    @Autowired
    private BlockService blockManager;

    public List<INode> getAllFiles() {
        return iNodeManager.getAllFiles();
    }

    public List<DataNodeEntity> getAllDataNodes() {
        return dataNodeManager.getAllDataNodes();
    }

    public NameNodeService() {
    }

    /**
     * 分块上传文件
     * 按照配置的大小BLOCK_SIZE对文件分块
     * 对每个Block选取当前负载最少的DataNode上传
     * 同时记录或更新Block、DataNode、namespace数据库、文件系统
     * @param file
     * @param iNodeName
     * @return
     * @throws IOException
     */
    public Boolean handleUploadedFile(MultipartFile file, String iNodeName) throws IOException {

        System.out.println("NameNodeService handleUploadedFile 文件");
        // 检查该文件是否存在
        // TODO 存在就覆盖,覆盖 = 先删除namespace + blocks，再存储
        if(iNodeManager.existsFile(iNodeName)){
            System.out.println("这文件已经存在了");
            return false;
        }

        // 解析该文件的属性
        int numBytes = file.getBytes().length;
        int numBlocks = numBytes / BLOCK_SIZE;
        numBlocks = (numBytes) % this.BLOCK_SIZE == 0 ? numBlocks : numBlocks + 1;
        System.out.println("这文件有" + numBytes +"个字节");

        // 文件分块上传
        FileInputStream fis = (FileInputStream) file.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(fis);

        for(int blockId = 0; blockId < numBlocks; ++blockId){

            // 分割文件，临时文件持久化到硬盘，UUID随机命名
            byte blockByteArray[] = new byte[BLOCK_SIZE];
            int blockSize = bis.read(blockByteArray);

            String blockUuid = UUID.randomUUID().toString();
            System.out.println(blockUuid);
            blockManager.store(blockByteArray, blockUuid, blockSize);

            // 交给Datanode Manager，选择节点存储block
            // 暂时不考虑上传过程中一个节点突然失效的情况，KISS法则(emmm 强行解释)
            List<DataNodeEntity> validDataNodes = dataNodeManager.getValidDataNodes(REPLICA_NUM);

            ArrayList<String> dataNodes = new ArrayList<>();

            // 上传
            for(int i = 0; i < REPLICA_NUM; ++i){

                // 获取该datanode的url
                String dataNodeUrl = validDataNodes.get(i).getUrl();

                // 上传Block
                blockManager.uploadBlockToDataNode(blockUuid, dataNodeUrl);

                // TODO 检查是否上传成功

                // 上传成功
                System.out.println(blockUuid + "被上传到" + dataNodeUrl);

                // 更新DataNode信息
                dataNodeManager.increaseNumBlockInDataNode(dataNodeUrl);

                dataNodes.add(validDataNodes.get(i).getUrl());
            }

            // 删除之前在硬盘暂时持久化的block
            blockManager.deleteLocalBlock(blockUuid);

            // 在 Block Manager 中记录当前Block，并记录对应datanode
            BlockEntity block = new BlockEntity(blockUuid, iNodeName, blockId,  blockSize,  dataNodes);

            // 上传成功, 在Block Manager中记录该节点以及存储该block的data node url
            blockManager.save(block);
        }
        fis.close();
        bis.close();


        // 在文件系统中添加该文件信息
        INode inode = new INode(iNodeName, numBlocks, numBytes, false);
        iNodeManager.addINodeFile(inode);

        return true;
    }

    /**
     * 注册新DataNode，将其纳入namenode的管理中
     * 注意：DataNode 上线时不进行数据迁移操作负载均衡
     * (时间有限，没读懂相关算法orz，挖个坑先)
     * @param dataNodeUrl
     */
    public void registerNewDataNode(String dataNodeUrl) {

        if(dataNodeManager.existsDataNode(dataNodeUrl)){
            // 已经存在该datanode，不重复注册
            return;
        }
        // 先在DataNode Manager中注册该节点,负载均衡前不提供服务(valid置为true)
        dataNodeManager.addDataNode(dataNodeUrl);

        // 数据迁移，保证负载均衡
//        dataNodeManager.

    }

    public List<BlockEntity> getAllBlocks() {
        return blockManager.getAllBlocks();
    }

    /**
     * 下载各个Block，拼成完整的文件，以Resource类型发送
     * @param iNodeName
     * @return
     * @throws IOException
     */
    public Resource loadFileAsResource(String iNodeName) throws IOException {

        // 检查该文件是否存在，不存在则直接返回
        if(!iNodeManager.existsFile(iNodeName)){
            System.out.println("要下载的文件不存在");
            return null;
        }


        // 获取文件基本信息
        INode iNode = iNodeManager.getINodeByName(iNodeName);
        int numBytes = iNode.getNumBytes();
        String filename = iNode.getFilename();

        // 创建新文件，用作拼接Block
        File downloadFile = new File(FILE_DIRECTORY + filename);
        downloadFile.createNewFile();

        // 关于文件拼接，参考链接https://blog.csdn.net/top_code/article/details/8844725
        // 输出管道 TODO（ 这名字对不对？）
        FileChannel outChannel = new FileOutputStream(downloadFile).getChannel();

        // 获取该文件有关的blocks信息，并按照block ID 排好序
        List<BlockEntity> blocks = blockManager.getBlocksByINodeName(iNodeName);

        int numBlocks = blocks.size();
        for(BlockEntity block : blocks){

            // 获取该block所在的所有datanode的url
            List<String> dataNodeUrls = block.getDataNodeUrls();
            String blockUuid = block.getBlockUuid();
            long blockSize = block.getNumByte();

            File blockFile = null;

            for(String dataNodeUrl : dataNodeUrls){

                // 判断该节点是否有效
                if(dataNodeManager.getDataNodeByUrl(dataNodeUrl).getValid()){

                    // 从url下载Block, 暂时存到Datanode本地目录 BlockManager-dir/{blockUuid}
                     blockFile = blockManager.downloadBlockFromDataNode(dataNodeUrl, blockUuid);

                    // 从有效地datanode下载到相应数据
                    // 循环就可以退出，不用再去其他节点下载副本
                    // TODO 这里估计得校验一下Block,暂时还不会
                    break;
                }

            }

            // 把该block的内容拼接到文件downloadFile末尾
            FileChannel inChannel = new FileInputStream(blockFile).getChannel();
            inChannel.transferTo(0, blockSize, outChannel);
            inChannel.close();
            blockManager.deleteLocalBlock(blockUuid);

        }

        outChannel.close();

        Path path = Paths.get(FILE_DIRECTORY + filename);
        Resource resource = new UrlResource(path.toUri());

        File file = new File(FILE_DIRECTORY+filename);

        if (resource.exists() || resource.isReadable()) {
            return resource;
        }
        return null;
    }

    /**
     * DataNode下线，进行数据迁移，使其负载均衡
     * @param dataNodeUrl
     * @throws IOException
     */
    public void cancelDataNode(String dataNodeUrl) throws IOException {

        // 检查该节点是否存在
        if(dataNodeManager.getDataNodeByUrl(dataNodeUrl) == null){
            System.out.println("找不到这个节点了？？？");
            dataNodeManager.getAllDataNodes().forEach(
                    item -> {
                        System.out.println(item.toString());
                    });
            return ;
        }
        // 先检查该datanode是否为无效，已经无效就啥都不做
        if(!dataNodeManager.getDataNodeByUrl(dataNodeUrl).getValid()){
            return ;
        }
        DataNodeEntity datanode = dataNodeManager.getDataNodeByUrl(dataNodeUrl);

        // 数据迁移前，先把该节点置为无效,不对外提供服务
        datanode.setValid(false);
        dataNodeManager.updateDataNode(datanode);

        List<BlockEntity> blocks = blockManager.getAllBlocksByDataNodeUrl(dataNodeUrl);

        for(BlockEntity block : blocks){

            // 获取该block的UUID，以便去对应datanode下载
            String blockUuid = block.getBlockUuid();

            List<String> urls = block.getDataNodeUrls();
            for(String url : urls){

                if(dataNodeManager.getDataNodeByUrl(url).getValid()){

                    // 先Get一份Block到namenode
                    blockManager.downloadBlockFromDataNode(url, blockUuid);
                    System.out.println("我从有效datanode上下载了一份Block，叫做"+blockUuid);
                }
            }


            // 找可用的datanode，准备迁移数据到该节点
            List<DataNodeEntity> validDataNodes = dataNodeManager.getValidDataNodes(1);
            String targetDataNodeUrl = validDataNodes.get(0).getUrl();
            System.out.println("我又找到了负载最少的节点，叫做"+targetDataNodeUrl);

            // 把Get到的Block在复制到其他datanode上
            blockManager.uploadBlockToDataNode(blockUuid, targetDataNodeUrl);
            System.out.println("把这个block上传给了他");



            // 更新数据库中datanode村的block数目
            dataNodeManager.increaseNumBlockInDataNode(targetDataNodeUrl);

            // 准备更新数据库中的Block存储的data node url
            ArrayList<String> newDataNodeUrls = new ArrayList<String>();
            urls.forEach(s -> {
                if(!s.equals(dataNodeUrl)){
                    newDataNodeUrls.add(s);
                }
            });
            newDataNodeUrls.add(targetDataNodeUrl);
            block.setDataNodeUrls(newDataNodeUrls);
            blockManager.updateBlock(block);
        }

        // 迁移完毕,删除该节点
        dataNodeManager.deleteDataNodeByUrl(dataNodeUrl);
    }

    /**
     * 删除文件
     * 查找存储Blocks信息的数据库，获取存储该文件的所有Block的datanode
     * 向相关datanode发起delete请求
     * 最后更新文件系统、block数据库、datanode数据库
     * @param iNodeName
     */
    public void deleteFile(String iNodeName) {

        // 获取所有该文件相关的block
        List<BlockEntity> blocks = blockManager.getBlocksByINodeName(iNodeName);

        for(BlockEntity block : blocks){

            String blockUuid = block.getBlockUuid();
            List<String> dataNodeUrls = block.getDataNodeUrls();

            for(String url:dataNodeUrls){
                blockManager.deleteBlockInDataNode(blockUuid, url);
                dataNodeManager.decreaseNumBlocksInDataNode(url);
            }
        }

        blockManager.deleteBlockInfosByINodeName(iNodeName);

        iNodeManager.deleteFileByINodeName(iNodeName);
    }

    /**
     * 查找某个目录下的所有文件信息
     * @param location
     * @return
     */
    public List<INode> getFilesInLocation(String location) {
        return iNodeManager.getFilesByLocation(location);
    }

    /**
     * 创建目录
     * @param iNodeName
     */
    public void createINodeDirectory(String iNodeName) {
        INode inode = new INode(iNodeName, 0, 0, true);
        iNodeManager.addINodeDirectory(inode);
    }

    /**
     * 删除目录
     * @param iNodeName
     */
    public void deleteFolder(String iNodeName) {
        List<INode> inodes = iNodeManager.getFilesByLocation(iNodeName);

        for(INode inode : inodes){

            // 文件夹: 递归删除
            if(inode.getDirectory()) {
                deleteFolder(inode.getiNodeName());
            }
            // 文件: 直接删除
            else {
                deleteFile(inode.getiNodeName());
            }
        }
        iNodeManager.deleteDirectoryByINodeName(iNodeName);
    }
}
