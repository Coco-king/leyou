package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {

    private static final List<String> CONTENT_TYPES = Arrays.asList("image/gif", "image/jpeg", "image/png");
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    @Autowired
    private FastFileStorageClient storageClient;

    /**
     * 上传图片
     *
     * @param file 图片
     * @return 图片的url地址
     */
    public String uploadImage(MultipartFile file) {
        // 获取文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件的类型
        String contentType = file.getContentType();
        if (!CONTENT_TYPES.contains(contentType)) {
            LOGGER.info("数据类型不合法：{}", originalFilename);
            return null;
        }
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                LOGGER.info("文件内容不合法：{}", originalFilename);
                return null;
            }
            String ext = StringUtils.substringAfterLast(originalFilename, ".");
            StorePath uploadFile = storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);
            return "http://image.leyou.com/" + uploadFile.getFullPath();
        } catch (IOException e) {
            LOGGER.info("服务器内部错误：{}", originalFilename);
            e.printStackTrace();
        }
        return null;
    }
}
