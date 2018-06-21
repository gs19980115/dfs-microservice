package com.njugs.dfs.datanode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Service
public class DataNodeService {

    @Autowired
    private DataNodeRepository dataNodeRepository;


    public List<DataNodeEntity> getAllValidDataNodes() {
        return dataNodeRepository.findAllByValid(true);
    }

    public List<DataNodeEntity> getAllDataNodes() {
        return (List<DataNodeEntity>) dataNodeRepository.findAll();
    }

    public void addDataNode(String dataNodeUrl) {

        // 准备datanode的信息，数据迁移完毕前先把valid置为false
        DataNodeEntity dataNode = new DataNodeEntity(dataNodeUrl);
        // TODO 测试用，等会儿删除
        dataNode.setValid(true);

        dataNodeRepository.save(dataNode);
    }

    public boolean existsDataNode(String dataNodeUrl) {
        if(dataNodeRepository.findByUrl(dataNodeUrl) != null)
            return true;
        return false;
    }

    public DataNodeEntity getDataNodeByUrl(String dataNodeUrl) {
        return dataNodeRepository.findByUrl(dataNodeUrl);
    }

    // delete update 都要加这个注解，否则会报异常
    // 参考 https://blog.csdn.net/hanghangde/article/details/53241150
    @Transactional
    public void update(DataNodeEntity datanode) {
        dataNodeRepository.save(datanode);
    }

    @Transactional
    public void deleteDataNodeByUrl(String dataNodeUrl) {
        dataNodeRepository.deleteByUrl(dataNodeUrl);
    }

    public void increaseNumBlockInDataNode(String dataNodeUrl) {
        DataNodeEntity datanode = dataNodeRepository.findByUrl(dataNodeUrl);
        datanode.setNumBlocks(datanode.getNumBlocks() + 1);
        dataNodeRepository.save(datanode);
    }

    public void decreaseNumBlocksInDataNode(String dataNodeUrl) {
        DataNodeEntity datanode = dataNodeRepository.findByUrl(dataNodeUrl);
        datanode.setNumBlocks(datanode.getNumBlocks() - 1);
        dataNodeRepository.save(datanode);
    }

    public List<DataNodeEntity> getValidDataNodes(int numReplicas) {

        // 先获取所有有效节点信息
        List<DataNodeEntity> allValidDataNodes = dataNodeRepository.findAllByValid(true);

        // 对有效节点根据负载数排序
        Collections.sort(allValidDataNodes);

        // 根据datanode数量，选择实际能存储的副本数
        int numValidDataNodes = allValidDataNodes.size();
        int realNumReplicas = numReplicas >  numValidDataNodes? numValidDataNodes : numReplicas;

        // TODO 吐槽用，待删除
        if(realNumReplicas < numReplicas)
            System.out.println("你丫的，没那么多节点给你存");

        List<DataNodeEntity> targetDataNodes = allValidDataNodes.subList(0, realNumReplicas);;

        return targetDataNodes;

    }

    public void updateDataNode(DataNodeEntity datanode) {
        dataNodeRepository.save(datanode);
    }

}
