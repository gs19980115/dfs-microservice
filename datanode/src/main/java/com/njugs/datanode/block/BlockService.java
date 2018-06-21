package com.njugs.datanode.block;

import com.njugs.datanode.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockService {

    @Autowired
    private BlockRepository blockRepository;

    public List<Block> getAllBlocks(){
        return (List<Block>)blockRepository.findAll();
    }

    public void save(Block block){
        blockRepository.save(block);
    }

    public void delete(Block block){
        blockRepository.delete(block);
    }


}
