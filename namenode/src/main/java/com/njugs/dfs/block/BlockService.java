package com.njugs.dfs.block;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BlockService {

    @Autowired
    private BlockRepository blockRepository;

    private final String BLOCKS_DIRECTORY = "Blocks/";


    public void save(BlockEntity blockEntity) {
        blockRepository.save(blockEntity);
    }

    public List<BlockEntity> getAllBlocks() {
        return (List<BlockEntity>) blockRepository.findAll();
    }

    public List<BlockEntity> getAllBlocksByDataNodeUrl(String dataNodeUrl) {
        List<BlockEntity> allBlocks = (List<BlockEntity>) blockRepository.findAll();
        List<BlockEntity> blocksInDataNode = new ArrayList<BlockEntity> ();

        allBlocks.forEach(item -> {
            List<String> urls = item.getDataNodeUrls();
            for(String url : urls){
                if(url.equals(dataNodeUrl)){
                    blocksInDataNode.add(item);
                }
            }
        });
        return blocksInDataNode;
    }

    public void updateBlock(BlockEntity block) {
        blockRepository.save(block);
    }

    @Transactional
    public void deleteBlockInfosByINodeName(String iNodeName) {
        Path path = Paths.get(iNodeName);
        String location = path.getParent().toString();
        String filename = path.getFileName().toString();
        blockRepository.deleteBlockInfoEntitiesByLocationAndFilename(location,filename);
    }

    // 将用户上传的文件拆分成的Block持久化到硬盘
    public void store(byte[] blockByteArray, String blockUuid, int blockSize) throws IOException {
        String filePath = BLOCKS_DIRECTORY + blockUuid;
        // TODO 能不能不要持久化到硬盘，直接上传
        File blockFile = new File(filePath);
        FileOutputStream out = new FileOutputStream(blockFile);
        out.write(blockByteArray, 0, blockSize);
        out.close();
    }

    public void uploadBlockToDataNode(String blockUuid, String dataNodeUrl) {

        String filePath = BLOCKS_DIRECTORY + blockUuid;

        // 准备上传到datanode请求的param参数
        FileSystemResource resource = new FileSystemResource(new File(filePath));
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("block", resource);

        // 发起Post请求上传文件block
        RestTemplate rest = new RestTemplate();

        // Post 返回一个String类型的response,检查是否上传成功
        String response = rest.postForObject(dataNodeUrl, param, String.class);
        System.out.println(response);


        // TODO 是否需要一个返回值呢？
    }

    public File downloadBlockFromDataNode(String dataNodeUrl, String blockUuid) throws IOException {
        // Post请求的完整url
        String url = dataNodeUrl + "/" + blockUuid;

        //获取资源（Block文件）
        UrlResource urlResource = new UrlResource(new URL(url));
        InputStream inputStream = urlResource.getInputStream();

        // 持久化到硬盘（不知道有么有别的方法）
        Path blockPath = Paths.get(BLOCKS_DIRECTORY + blockUuid);
        Files.copy(inputStream,blockPath, StandardCopyOption.REPLACE_EXISTING);

        return new File(BLOCKS_DIRECTORY + blockUuid);
    }

    public List<BlockEntity> getBlocksByINodeName(String iNodeName) {
        List<BlockEntity> targetBlocks = blockRepository.findAllByINodeName(iNodeName);
        Collections.sort(targetBlocks);
        return targetBlocks;
    }

    public void deleteBlockInDataNode(String blockUuid, String dataNodeUrl) {
        RestTemplate rest = new RestTemplate();
        rest.delete(dataNodeUrl + blockUuid);
    }


    public void deleteLocalBlock(String blockUuid) {
        File file = new File(BLOCKS_DIRECTORY + blockUuid);
        if(file.exists()){
           file.delete();
        }
    }
}
