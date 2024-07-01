package com.swak.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swak.common.dto.Response;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

@Slf4j
public class ResponseRender {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 发送文本。使用UTF-8编码。
     *
     * @param response HttpServletResponse
     * @param result     发送的字符串
     */
    public static void renderText(String result,HttpServletResponse response) throws IOException {
        render(result,response, "text/plain;charset=UTF-8");
    }

    /**
     * 发送json。使用UTF-8编码。
     *
     * @param response HttpServletResponse
     * @param result     发送的字符串
     */
    public static void renderJson(String result,HttpServletResponse response) throws IOException {
        render(result, response,"application/json;charset=UTF-8");
    }

    public static void renderJson(Response<?> result,HttpServletResponse response) throws IOException {
        renderJackson(result, response,"text/json;charset=UTF-8");
    }

    /**
     * 发送xml。使用UTF-8编码。
     *
     * @param response HttpServletResponse
     * @param result     发送的字符串
     */
    public static void renderXml(String result,HttpServletResponse response) throws IOException {
        render(result, response,"text/xml;charset=UTF-8");
    }

    /**
     * 发送内容。使用UTF-8编码。
     */
    public static void render(String text,HttpServletResponse response,String contentType) throws IOException {
        response.setContentType(contentType);
        response.setCharacterEncoding(StringPool.UTF8);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(text);
        }
    }

    public static void renderFastJson( Object value,HttpServletResponse response, String contentType) throws IOException {
        response.setContentType(contentType);
        response.setCharacterEncoding(StringPool.UTF8);
        try (OutputStream writer = response.getOutputStream()) {
            JSON.writeTo(writer, value, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.BrowserCompatible);
            writer.flush();
        }
    }

    public static void renderJackson( Object value,HttpServletResponse response, String contentType) throws IOException {
        response.setContentType(contentType);
        response.setCharacterEncoding(StringPool.UTF8);
        try (PrintWriter writer = response.getWriter()) {
            objectMapper.writeValue(writer, value);
            writer.flush();
        }
    }

    public static void renderFastJson(Response<?> result,HttpServletResponse response) throws IOException {
        renderFastJson(result, response,"text/json;charset=UTF-8");
    }

    public static void renderJackson(Response<?> result,HttpServletResponse response) throws IOException {
        renderJackson(result, response,"text/json;charset=UTF-8");
    }
}
