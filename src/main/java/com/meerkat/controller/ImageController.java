package com.meerkat.controller;

import com.meerkat.base.util.JsonResponse;
import com.meerkat.service.ImageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by wm on 16/9/22.
 */
@RequestMapping(value = "image/")
@Controller
public class ImageController {

    @Inject
    ImageService imageService;

    @RequestMapping(value = "upload")
    public JsonResponse uploadImage(HttpServletRequest request) {
        JsonResponse jsonResponse = new JsonResponse(false);
        List<String> images = imageService.uploadImages(request);
        jsonResponse.set("paths", images);
        jsonResponse.setSuccess(true);
        return jsonResponse;
    }

}
