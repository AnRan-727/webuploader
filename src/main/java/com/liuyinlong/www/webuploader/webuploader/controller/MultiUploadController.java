package com.liuyinlong.www.webuploader.webuploader.controller;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: AnRan
 * Url: www.liuyinlong.com
 * Date: 2020/7/28
 */
@Controller
public class MultiUploadController {

    //该路径可根据项目需求存储到数据库
    //正式文件路径
    @Value("${upload.file.path}")
    private String decryptFilePath;
    //临时文件路径  分片
    @Value("${upload.file.path.temp}")
    private String decryptFilePathTemp;

    /**
     * 分片上传文件
     * @param request
     * @param file 文件
     * @param chunks 分片总数
     * @param chunk 现在是第几片
     * @param guid 整个分片文件的guid  当作临时文件路径
     * @return
     * @throws IOException
     */
    @PostMapping("/multi/upload")
    @ResponseBody
    public ResponseEntity<Void> decrypt(HttpServletRequest request, @RequestParam MultipartFile file, Integer chunks, Integer chunk,String guid) throws IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            if (file != null) {
                if (chunks == null && chunk == null) {
                    chunk = 0;
                }
                File outFile = new File(decryptFilePathTemp + File.separator+guid, chunk + ".part");
                InputStream inputStream = file.getInputStream();
                FileUtils.copyInputStreamToFile(inputStream, outFile);
            }
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 合并分片的文件
     * @param fileArray 文件信息  guid分片文件夹 name文件名称 webkitRelativePath文件路径+名称
     *                  [{"guid":"wu_1eeaqpv3af8i1m461iqn3q41m6v9","name":"VMware-workstation-full-14.1.1.28517.exe","webkitRelativePath":"测试文件/VMware-workstation-full-14.1.1.28517.exe"}
     *                  ,{"guid":"wu_1eeaqqqoth351mqt1ki71hua1aao38","name":"Xftp-6.0.0095p.exe","webkitRelativePath":"测试文件/Xftp-6.0.0095p.exe"}
     *                  ,{"guid":"wu_1eeaqqsj34e2t4mcj290h1do23h","name":"bootdemo-0.0.1-SNAPSHOT.jar","webkitRelativePath":"测试文件/测试用的web项目/bootdemo-0.0.1-SNAPSHOT.jar"}
     *                  ,{"guid":"wu_1eeaqqsql99r107116au10cbpl33m","name":"test.war","webkitRelativePath":"测试文件/测试用的web项目/test.war"}
     *                  ,{"guid":"wu_1eeaqqss2n883erh1stsnv0k3o","name":"webdemo.war","webkitRelativePath":"测试文件/测试用的web项目/webdemo.war"}
     *                  ,{"guid":"wu_1eeaqqsu4mcbpbv19mb8rq15h93q","name":"bootdemo-0.0.1-SNAPSHOT - 副本.jar","webkitRelativePath":"测试文件/测试用的web项目/阿萨德/bootdemo-0.0.1-SNAPSHOT - 副本.jar"}]
     * @throws Exception Exception
     */
    @GetMapping("/multi/merge")
    @ResponseBody
    public void byteMergeAll(@RequestParam String fileArray) {
        List<Map<String,String>> listMap = (List<Map<String, String>>) JSON.parse(fileArray);
        for (Map<String, String> stringStringMap : listMap) {
            //获取分片文件路径
            File file = new File(decryptFilePathTemp+File.separator+stringStringMap.get("guid"));
            //判断是否是文件夹
            if (file.isDirectory()) {
                //得到该分片文件夹下的所有分片文件
                File[] files = file.listFiles();
                //判断是否为空
                if (files != null && files.length > 0) {
                    //文件名称
                    String fileName = stringStringMap.get("name");
                    //文件相对路径+名称
                    String webkitRelativePath =stringStringMap.get("webkitRelativePath");
                    //移除路径中的文件名
                    String remove = StringUtils.remove(webkitRelativePath, fileName);
                    //设置路径为 本地目录+文件相对路径
                    String filePath = decryptFilePath +"\\"+remove;
                    File file1 = new File(filePath);
                    if(!file1 .exists()) { //当该文件夹不存在时
                        file1.mkdirs();//创建目录
                    }
                    //创建真实文件
                    File partFile = new File(filePath + File.separator + fileName);
                    try {
                        //合并所有文件分片到真实文件中
                        for (int i = 0; i < files.length; i++) {
                            File s = new File(decryptFilePathTemp+File.separator+stringStringMap.get("guid"), i + ".part");
                            FileOutputStream destTempfos = new FileOutputStream(partFile, true);
                            FileUtils.copyFile(s,destTempfos );
                            destTempfos.close();
                        }
                        //删除文件分片
                        FileUtils.deleteDirectory(file);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
