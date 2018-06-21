package com.njugs.dfs.namespace;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface INodeRepository  extends CrudRepository<INode, Long> {
    List<INode> findAllByLocationAndFilenameAndIsDirectory(String location, String filename, Boolean isDirectory);

    INode findByLocationAndFilenameAndIsDirectory(String location, String filename, boolean b);

    INode findByINodeNameAndIsDirectory(String iNodeName, Boolean isDirectory);

    void deleteByINodeName(String iNodeName);

    List<INode> findAllByLocation(String location);
}
