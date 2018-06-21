package com.njugs.dfs.block;

import org.springframework.core.style.ToStringCreator;

import javax.persistence.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BlockEntity implements Comparable<BlockEntity>{
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String iNodeName;
    private String blockUuid;// TODO 这啥玩意儿
    private Integer blockId;
    private String filename;
    private String location;
    private Integer numByte;
    @Basic
    private ArrayList<String> dataNodeUrls;


    public BlockEntity() {
    }

    public BlockEntity(String blockUuid, String iNodeName, Integer blockId, Integer numByte, ArrayList<String> dataNodeUrls) {
        this.blockUuid = blockUuid;
        Path path = Paths.get(iNodeName);
        this.iNodeName = iNodeName;
        this.filename = path.getFileName().toString();
        this.location = path.getParent().toString();
        this.blockId = blockId;
        this.numByte = numByte;
        this.dataNodeUrls = dataNodeUrls;
        System.out.println(this.toString());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBlockUuid() {
        return blockUuid;
    }

    public void setBlockUuid(String blockUuid) {
        this.blockUuid = blockUuid;
    }

    public String getiNodeName() {
        return iNodeName;
    }

    public void setiNodeName(String iNodeName) {
        this.iNodeName = iNodeName;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public Integer getNumByte() {
        return numByte;
    }

    public void setNumByte(Integer numByte) {
        this.numByte = numByte;
    }


    public List<String> getDataNodeUrls() {
        return dataNodeUrls;
    }

    public void setDataNodeUrls(ArrayList<String> dataNodeUrls) {
        this.dataNodeUrls = dataNodeUrls;
    }


    @Override
    public int compareTo(BlockEntity o) {
        return this.blockId - o.blockId;
    }

    @Override
    public String toString(){
        return new ToStringCreator(this)
                .append("id", this.getId())
                .append("filename", this.getFilename())
                .append("location", this.getLocation())
                .append("blockUuid", this.getBlockUuid())
                .append("numBytes", this.getNumByte())
                .append("datanodes", this.getDataNodeUrls()).toString();
    }
}
