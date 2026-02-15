package com.community.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadFileVO {
    private String url;
    private String filename;
    private String originalFilename;
    private String contentType;
    private long size;
    private String bizType;
}
