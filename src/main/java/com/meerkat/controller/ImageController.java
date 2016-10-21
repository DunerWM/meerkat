package com.meerkat.controller;

import com.meerkat.base.util.ConfigPropertiesUtil;
import com.meerkat.base.util.JsonResponse;
import com.meerkat.service.ImageService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * Created by wm on 16/9/22.
 */
@RequestMapping(value = "image/")
@Controller
public class ImageController {

    @Inject
    ImageService imageService;
    Logger log = LoggerFactory.getLogger(ImageService.class);
    private static final String SAVE_PATH = ConfigPropertiesUtil.getValue("img.save.path");

    @RequestMapping(value = "{year:[\\d]{4}}/{month:[\\d]{1,2}}/{day:[\\d]{1,2}}/{name:[\\S]+[\\.][\\S]{3}$}")
    public void getImage(HttpServletResponse response, @PathVariable String year, @PathVariable String month, @PathVariable String day, @PathVariable String name) {
        String filePath = File.separator + year + File.separator + month + File.separator + day + File.separator + name;
        File imgFile = new File(SAVE_PATH + filePath);
        if (!imgFile.exists() || !imgFile.canRead()) {
            log.info("no file named {}, path={}", filePath, imgFile.getAbsoluteFile());
            return;
        }
        try {
            IOUtils.copy(new AutoCloseInputStream(new FileInputStream(imgFile)), response.getOutputStream());
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @RequestMapping(value = "upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse uploadImage(HttpServletRequest request) {
        JsonResponse jsonResponse = new JsonResponse(false);
        List<String> images = imageService.uploadImages(request);
        jsonResponse.set("paths", images);
        jsonResponse.setSuccess(true);
        return jsonResponse;
    }

}
