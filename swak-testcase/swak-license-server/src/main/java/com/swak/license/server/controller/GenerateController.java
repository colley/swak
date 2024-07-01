package com.swak.license.server.controller;


import com.swak.common.dto.Response;
import com.swak.common.enums.BasicErrCode;
import com.swak.license.server.config.LicenseCreator;
import com.swak.license.server.domain.LicenseCreatorParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * 用于生成证书文件，不能放在给客户部署的代码里
 */

@Slf4j
@RestController
@RequestMapping("/license")
public class GenerateController {

    /**
     * 密钥库存储路径
     */
    @Value("${license.privateKeysStorePath}")
    private String privateKeysStorePath;


    /**
     * {
     *   "subject": "license_sub",
     *   "keyPass": "abc@123",
     *   "edition": "standard",
     *   "issuedTime": "2024-06-14 00:00:00",
     *   "expiryTime": "2024-06-29 00:00:00",
     *   "description": "license_sub",
     *   "distinguishedName": "CN=xx.CN",
     *   "licenseCheckModel": {
     *     "ipAddress": ["2001:0:2841:aa90:34fb:8e63:c5ce:e345", "192.168.153.155"],
     *     "macAddress": ["00-00-00-00-00-00-00-E0","B0-52-16-27-F5-EF"],
     *     "userAmount": "40",
     *     "key1": ""//根据需求定义
     *   }
     * }
     * privateKeysStorePath = /pkcs8_private.key  私钥路径
     */
    @RequestMapping(value = "/generateLicense")
    public Response<?> generateLicense(@RequestBody LicenseCreatorParam param) {
        // 这里参数从配置中读取
        String licenseFileName = System.currentTimeMillis()+".lic";
        String licensePath = System.getProperty("java.io.tmpdir")+licenseFileName;
        param.setLicensePath(licensePath);
        param.setLicenseFileName(licenseFileName);
        param.setPrivateKeyPath(privateKeysStorePath);
        LicenseCreator licenseCreator = new LicenseCreator(param);
        boolean result = licenseCreator.generateLicense();
        if (result) {
            return Response.success(param);
        } else {
            return Response.fail(BasicErrCode.SWAK_ERROR.getCode(), "授权文件生成失败！");
        }
    }

    /**
     * 下载文件到浏览器
     */
    @RequestMapping(value = "/download")
    public void downFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String licenseFileName = request.getParameter("licenseFileName");
        String licensePath = System.getProperty("java.io.tmpdir")+licenseFileName;
        File file = new File(licensePath);
        // 文件存在才下载
        if (file.exists()) {
            OutputStream out = null;
            FileInputStream in = null;
            try {
                // 读取要下载的内容
                in = new FileInputStream(file);
                // 解决文件名乱码问题，获取浏览器类型，转换对应文件名编码格式，IE要求文件名必须是utf-8, firefo要求是iso-8859-1编码
                String filename = URLEncoder.encode("license.lic", "UTF-8");
                // 设置一个响应头，无论是否被浏览器解析，都下载
                response.setHeader("Content-disposition", "attachment; filename=" + filename);
                // 将要下载的文件内容通过输出流写到浏览器
                out = response.getOutputStream();
                int len = 0;
                byte[] buffer = new byte[1024];
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            } catch (IOException e) {
                log.error("",e);
            } finally {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            }
        }
    }
}