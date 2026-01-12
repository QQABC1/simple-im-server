package com.shixun.simpleimserver.model.ws;

import lombok.Data;

@Data
public class WSData {
    private String content;     // 文本内容
    private String url;         // 文件/图片 URL
    private String fileName;    // 文件名
    private String fileSize;    // 文件大小
    private FontStyle font;     // 字体样式
}