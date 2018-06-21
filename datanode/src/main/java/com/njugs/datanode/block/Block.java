package com.njugs.datanode.block;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Block {

    @Id
    private String name;
    private Integer size;

    public Block() {
    }

    public Block(String name, Integer size) {

        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
