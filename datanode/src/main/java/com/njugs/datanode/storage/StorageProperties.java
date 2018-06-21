package com.njugs.datanode.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    @Value(value = "${server.port}")
    private Integer location;

    public String getLocation() {
        return location.toString();
    }


}
