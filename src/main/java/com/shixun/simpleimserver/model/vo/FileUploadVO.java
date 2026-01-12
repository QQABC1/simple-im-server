package com.shixun.simpleimserver.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadVO {
    private String url;      // 文件访问地址
    private String fileName; // 原始文件名
    private Long fileSize;   // 文件大小(字节)
}