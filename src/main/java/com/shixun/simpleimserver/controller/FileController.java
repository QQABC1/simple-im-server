package com.shixun.simpleimserver.controller;

import com.shixun.simpleimserver.common.result.Result;
import com.shixun.simpleimserver.model.vo.FileUploadVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@Api(tags = "文件上传接口")
public class FileController {

    @Value("${file.upload-path}")
    private String uploadPath; // 从配置文件读取保存路径

    @Value("${server.port}")
    private String serverPort; // 获取当前端口

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            // 1. 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 获取后缀 (如 .png)
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 2. 生成新文件名 (UUID防止重名)
            String newFileName = UUID.randomUUID().toString() + suffix;

            // 3. 按日期生成子目录 (如 20231010)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String datePath = sdf.format(new Date());

            // 4. 创建目标目录对象
            File destDir = new File(uploadPath + datePath);
            if (!destDir.exists()) {
                destDir.mkdirs(); // 自动创建多级目录
            }

            // 5. 保存文件到磁盘
            File destFile = new File(destDir, newFileName);
            file.transferTo(destFile);

            // 6. 拼接返回的 URL
            // 格式: http://localhost:8080/files/20231010/uuid.png
            String scheme = request.getScheme(); // http
            String serverName = request.getServerName(); // localhost

            // 注意：这里硬编码了 /files/ 对应 WebConfig 中的配置，实际项目可以更灵活
            String url = scheme + "://" + serverName + ":" + serverPort + "/files/" + datePath + "/" + newFileName;

            // 7. 返回结果
            FileUploadVO vo = new FileUploadVO(url, originalFilename, file.getSize());
            return Result.success(vo);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}