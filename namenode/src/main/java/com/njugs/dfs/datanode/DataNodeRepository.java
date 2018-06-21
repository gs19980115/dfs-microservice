package com.njugs.dfs.datanode;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DataNodeRepository extends CrudRepository<DataNodeEntity, String> {

    List<DataNodeEntity> findAllByValid(Boolean isValid);

    DataNodeEntity findByUrl(String url);

    void deleteByUrl(String url);

    void deleteAllByUrl(String url);
}
