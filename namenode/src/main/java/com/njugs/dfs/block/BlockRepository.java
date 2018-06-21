package com.njugs.dfs.block;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BlockRepository extends CrudRepository<BlockEntity, Long>{

    List<BlockEntity> findByFilename(String filename);

    BlockEntity findByLocationAndFilenameAndBlockId(String location, String filename, Integer blockId);

    BlockEntity findByINodeNameAndBlockId(String iNodeName, Integer blockId);

    List<BlockEntity> findAllByINodeName(String iNodeName);

    List<BlockEntity> findAllByLocationAndFilename(String location, String filename);

    void deleteBlockInfoEntitiesByLocationAndFilename(String location, String filename);
}
