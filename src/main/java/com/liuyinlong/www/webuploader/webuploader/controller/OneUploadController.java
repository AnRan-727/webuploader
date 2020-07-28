package com.liuyinlong.www.webuploader.webuploader.controller;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by IntelliJ IDEA.
 * User: AnRan
 * Url: www.liuyinlong.com
 * Date: 2020/7/28
 */
@Controller
public class OneUploadController {

    private static String FILENAME = "";

    private static String FILEPATH = "";

    //正式文件路径
    @Value("${upload.file.path}")
    private String decryptFilePath;

    //临时文件路径  分片
    @Value("${upload.file.path.temp}")
    private String decryptFilePathTemp;

    /**
     * 分片上传单文件
     * @return ResponseEntity<Void>
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Void> decrypt(HttpServletRequest request, @RequestParam(value = "file", required = false) MultipartFile file, Integer chunks, Integer chunk, String name, String guid) throws IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            if (file != null) {
                if (chunks == null && chunk == null) {
                    chunk = 0;
                }
                File outFile = new File(decryptFilePathTemp + File.separator+guid, chunk + ".part");
                if ("".equals(FILENAME)) {
                    FILENAME = name;
                }
                InputStream inputStream = file.getInputStream();
                FileUtils.copyInputStreamToFile(inputStream, outFile);
            }
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 合并单文件上传分片
     * @throws Exception Exception
     */
    @GetMapping("/merge")
    @ResponseBody
    public void byteMergeAll(String guid) throws Exception {
        //获取分片文件路径
        File file = new File(decryptFilePathTemp+File.separator+guid);
        //判断是否是文件夹
        if (file.isDirectory()) {
            //得到该分片文件夹下的所有分片文件
            File[] files = file.listFiles();
            //判断是否为空
            if (files != null && files.length > 0) {
                //获取当前时间
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter yyyymMdd = DateTimeFormatter.ofPattern("YYYYMMdd");
                String format = now.format(yyyymMdd);
                //设置路径为 本地目录+当前时间
                String filePath = decryptFilePath +"\\"+format;
                File file1 = new File(filePath);
                if(!file1 .exists()) { //当该文件夹不存在时
                    file1.mkdirs();//创建目录
                }
                //创建真实文件
                File partFile = new File(filePath + File.separator + FILENAME);
                //合并所有文件分片到真实文件中
                for (int i = 0; i < files.length; i++) {
                    File s = new File(decryptFilePathTemp+File.separator+guid, i + ".part");
                    FileOutputStream destTempfos = new FileOutputStream(partFile, true);
                    FileUtils.copyFile(s,destTempfos );
                    destTempfos.close();
                }
                //删除文件分片
                FileUtils.deleteDirectory(file);
                //System.out.println();
                FILENAME = "";
            }
        }
    }
}

