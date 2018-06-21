package com.njugs.dfs.namespace;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.nio.file.Path;
import java.nio.file.Paths;

@Entity
public class INode {
    @Id
    @GeneratedValue
    private Long id;

    private String iNodeName;
    private String location;
    private String filename;
    private String filetype;
    private Integer numBlocks;
    private int numBytes;
    private Boolean isDirectory;

    public INode() {
    }

    public INode(String iNodeName) {
        Path path = Paths.get(iNodeName);
        this.location = path.getParent().toString();
        this.filename = path.getFileName().toString();
    }

    public INode(String iNodeName, Integer numBlocks, int numBytes, Boolean isDirectory) {

        Path path = Paths.get(iNodeName);
        this.iNodeName = iNodeName;
        this.location = path.getParent().toString();
        this.filename = path.getFileName().toString();
        this.numBlocks = numBlocks;
        this.numBytes = numBytes;
        this.isDirectory = isDirectory;

        if(filename.contains(".")) {
            String filetype = iNodeName.substring(iNodeName.lastIndexOf("."), iNodeName.length());
            this.filetype = filetype.replaceFirst(".", "");
        }
        else if(isDirectory == true){
            this.filetype = "directory";
        }
        else
            this.filetype = "unknown";

        System.out.println("这个文件类型是" + filetype);
    }

    public String getiNodeName() {
        return iNodeName;
    }

    public void setiNodeName(String iNodeName) {
        this.iNodeName = iNodeName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public Integer getNumBlocks() {
        return numBlocks;
    }

    public void setNumBlocks(Integer numBlocks) {
        this.numBlocks = numBlocks;
    }

    public int getNumBytes() {
        return numBytes;
    }

    public void setNumBytes(int numBytes) {
        this.numBytes = numBytes;
    }

    public Boolean getDirectory() {
        return isDirectory;
    }

    public void setDirectory(Boolean directory) {
        isDirectory = directory;
    }

    public INode(String location, String filename, Integer numBlocks, int numBytes, Boolean isDirectory) {

        this.location = location;
        this.filename = filename;
        this.numBlocks = numBlocks;
        this.numBytes = numBytes;
        this.isDirectory = isDirectory;
    }

    public void setINode(String iNodeName, int numBlocks, int numBytes, boolean isDirectory) {
        Path path = Paths.get(iNodeName);
        this.location = path.getParent().toString();
        this.filename = path.getFileName().toString();
        this.numBlocks = numBlocks;
        this.numBytes = numBytes;
        this.isDirectory = isDirectory;
    }
}

