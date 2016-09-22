package com.meerkat.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wm on 16/9/22.
 */
@Service
public class ImageService {

    Logger log = LoggerFactory.getLogger(ImageService.class);

    private static final String SAVE_PATH = "/Users/wm/workspace/idea/meerkat/upload";

    public List<String> uploadImages(HttpServletRequest request) {
        List<String> pathList = new ArrayList<String>();
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> fileResult = multipartHttpServletRequest.getMultiFileMap();
        for (Map.Entry entry : fileResult.entrySet()) {
            List<MultipartFile> fileList = (List<MultipartFile>) entry.getValue();
            for (int i = 0; i < fileList.size(); i++) {
                MultipartFile file = fileList.get(i);
                InputStream in = null;
                try {
                    in = file.getInputStream();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (in != null) {
                    String fileName = getImgSaveName(file.getOriginalFilename());
                    String filePath = getImgSavePath();
                    String relativePath = getImgSaveRelativePath();
                    try {
                        int result = saveImgByStream(in, filePath, fileName);
                        if (result > 0) {
                            pathList.add(relativePath + File.separator + fileName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return pathList;

    }

    /**
     * 将接收的字符串转换成图片保存
     *
     * @param inputStream 文件流
     * @param path        图片的保存路径
     * @param fileName    图片的名称
     * @return 1：保存正常
     * 0：保存失败
     */
    public int saveImgByStream(InputStream inputStream, String path, String fileName) throws IOException {
        int result;
        File parent = new File(path);
        File file = new File(parent, fileName);
        log.info("即将保存图片为：" + file.getAbsoluteFile());
        FileUtils.forceMkdir(parent);
        OutputStream outputStream = new FileOutputStream(file.getPath());
        try {
            IOUtils.copy(new AutoCloseInputStream(inputStream), outputStream);
        } catch (IOException e) {
            log.error("保存图片出错:" + file.getAbsoluteFile() + File.separator + fileName, e);
        } finally {
            IOUtils.closeQuietly(outputStream);
            result = 1;
        }
        log.info("保存图片为：" + file.getAbsoluteFile() + "成功！！");
        return result;
    }

    public String getImgSaveName(String name) {
        int index = name.lastIndexOf(".");
        String extension = ".jpg";
        if (index > 0) {
            String tmpExtension = name.substring(index);
            if (tmpExtension.length() < 6) {
                extension = tmpExtension;
            }
        }
        String imageName = UUID.randomUUID().toString().replaceAll("-", "") + extension;
        return imageName;
    }

    public String getImgSavePath() {
        String uploadPath = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        uploadPath = uploadPath.replaceAll("-", File.separator);
        return SAVE_PATH + uploadPath;
    }

    public String getImgSaveRelativePath() {
        String uploadPath = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        uploadPath = uploadPath.replaceAll("-", File.separator);
        return uploadPath;
    }

}
