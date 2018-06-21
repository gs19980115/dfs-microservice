package com.njugs.datanode.block;


import com.njugs.datanode.storage.StorageFileNotFoundException;
import com.njugs.datanode.storage.StorageService;
import org.hibernate.mapping.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
public class BlockController {

    @Autowired
    private StorageService storageService;

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> downloadBlock(@PathVariable("filename") String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @DeleteMapping("/{filename}")
    public void deleteBlock(@PathVariable("filename") String filename) {
        storageService.delete(filename);
    }

    @PostMapping("/")
    public String handleFile(@RequestParam("block") MultipartFile file) throws IOException {

        storageService.store(file);
        return "yes";

    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }


}
