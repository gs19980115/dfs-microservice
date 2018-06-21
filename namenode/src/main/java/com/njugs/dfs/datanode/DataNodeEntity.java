package com.njugs.dfs.datanode;

import org.springframework.core.style.ToStringCreator;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DataNodeEntity implements Comparable<DataNodeEntity> {
    @Id
    private String url;
    private Integer numBlocks;
    private Boolean valid;

    @Override
    public int compareTo(DataNodeEntity o) {
        return this.numBlocks - o.numBlocks;
    }

    public DataNodeEntity() {
    }

    // 服务上线时新建datanode时调用此函数
    public DataNodeEntity(String dataNodeUrl){
        this.url = dataNodeUrl;
        this.numBlocks = 0;
        // 数据迁移完毕前先把valid置为false
        this.valid = false;
    }

    public DataNodeEntity(String url, Integer numBlocks, Boolean valid) {
        this.url = url;
        this.numBlocks = numBlocks;
        this.valid = valid;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getNumBlocks() {
        return numBlocks;
    }

    public void setNumBlocks(Integer numBlocks) {
        this.numBlocks = numBlocks;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    @Override
    public String toString(){
        return new ToStringCreator(this)
                .append("url", this.getUrl())
                .append("numBlocks", this.getNumBlocks().toString())
                .append("valid", this.getValid().toString()).toString();
    }
}
