package com.njugs.dfs.namespace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class INodeService {

    @Autowired
    private INodeRepository iNodeRepository;


    public List<INode> getAllFiles() {
        // TODO 未测试
        return (List<INode>) iNodeRepository.findAll();
    }

    public boolean existsFile(String iNodeName) {
        Path path = Paths.get(iNodeName);
        String location = path.getParent().toString();
        System.out.println(location);
        String filename = path.getFileName().toString();
        System.out.println(filename);
        if(iNodeRepository.findAllByLocationAndFilenameAndIsDirectory(location,filename, false).isEmpty()){
            return false;
        }
        System.out.println(iNodeRepository.findAllByLocationAndFilenameAndIsDirectory(location,filename, false).toString());
        return true;
    }

    public void save(INode inode) {
        iNodeRepository.save(inode);
    }

    public INode getINodeByName(String iNodeName) {
        Path path = Paths.get(iNodeName);
        String location = path.getParent().toString();
        String filename = path.getFileName().toString();
        return iNodeRepository.findByLocationAndFilenameAndIsDirectory(location,filename,false);
    }

    public List<INode> getFilesByLocation(String location) {
        return iNodeRepository.findAllByLocation(location);
    }

    public void addINodeFile(INode inode) {

        int numBytes = inode.getNumBytes();

        Path path = Paths.get(inode.getiNodeName());
        while(!path.getParent().toString().equals("/")){
            path = path.getParent();

            INode t = iNodeRepository.findByINodeNameAndIsDirectory(path.toString(), true);
            t.setNumBytes(t.getNumBytes() + numBytes);
            iNodeRepository.save(t);

        }

        iNodeRepository.save(inode);
    }


    public void addINodeDirectory(INode inode) {
        iNodeRepository.save(inode);
    }

    @Transactional
    public void deleteFileByINodeName(String iNodeName) {

        INode inode = iNodeRepository.findByINodeNameAndIsDirectory(iNodeName, false);

        if(inode == null)
            return;

        int numBytes = inode.getNumBytes();

        Path path = Paths.get(inode.getiNodeName());
        while(!path.getParent().toString().equals("/")){
            path = path.getParent();

            INode t = iNodeRepository.findByINodeNameAndIsDirectory(path.toString(), true);
            t.setNumBytes(t.getNumBytes() - numBytes);
            iNodeRepository.save(t);

        }

        iNodeRepository.deleteByINodeName(iNodeName);
    }

    @Transactional
    public void deleteDirectoryByINodeName(String iNodeName){
        INode inode = iNodeRepository.findByINodeNameAndIsDirectory(iNodeName, true);
        if(inode == null)
            return;

        iNodeRepository.deleteByINodeName(iNodeName);
    }

}
