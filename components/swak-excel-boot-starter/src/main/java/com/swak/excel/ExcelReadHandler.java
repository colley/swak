package com.swak.excel;


import com.alibaba.excel.read.listener.ReadListener;
import com.swak.excel.builder.SwakExcelReadBuilder;
import com.swak.excel.metadata.ExcelSheetData;
import com.swak.excel.metadata.ReadExcelParams;
import com.swak.excel.validation.BeanDataValidator;
import com.swak.excel.validation.BizRowDataValidator;
import com.swak.excel.validation.RowDataValidator;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

/**
 * ExcelReadHandler.java
 * 
 * @author colley.ma
 * @since 3.0.0
 **/
public class ExcelReadHandler<T> {

    private SwakExcelReadBuilder excelReadBuilder;

    private ReadExcelParams readExcelParams = new ReadExcelParams();

    public ExcelReadHandler(SwakExcelReadBuilder excelReadBuilder) {
        this.excelReadBuilder = excelReadBuilder;
    }

    public static <T> ExcelReadHandler<T> read(MultipartFile file) {
        return new ExcelReadHandler<T>(SwakExcelReadBuilder.read(file));
    }

    public static <T> ExcelReadHandler<T> read(MultipartFile file, Class<?> head) {
        return new ExcelReadHandler<T>(SwakExcelReadBuilder.read(file, head));
    }

    public static <T> ExcelReadHandler<T> read(InputStream inputStream) {
        return new ExcelReadHandler<T>(SwakExcelReadBuilder.read(inputStream));
    }

    public static <T> ExcelReadHandler<T> read(InputStream inputStream, Class<?> head) {
        return new ExcelReadHandler<T>(SwakExcelReadBuilder.read(inputStream, head));
    }

    public ExcelReadHandler<T> readExcelParams(ReadExcelParams readExcelParams) {
        this.readExcelParams = readExcelParams;
        return this;
    }

    public ExcelReadHandler<T> head(Class<?> head) {
        excelReadBuilder.head(head);
        return this;
    }

    public ExcelReadHandler<T> multiHead(Class<?>... headList) {
        excelReadBuilder.multiHead(headList);
        return this;
    }

    public ExcelReadHandler<T> addBizValidator(BizRowDataValidator<T> bizValidator) {
        excelReadBuilder.addBizValidator(bizValidator);
        return this;
    }

    ExcelReadHandler<T> registerReadListener(ReadListener<T> readListener) {
        excelReadBuilder.registerReadListener(readListener);
        return this;
    }

    public ExcelReadHandler<T> addDataValidator(RowDataValidator<?> bizValidator) {
        excelReadBuilder.addDataValidator(bizValidator);
        return this;
    }

    public <R> R doRead(Function<List<T>, R> handler) {
        List<T> listData = doRead();
        return handler.apply(listData);
    }

    public <R> R doReadAll(Function<ExcelSheetData, R> handler) {
        return handler.apply(doReadAll());
    }

    public ExcelSheetData doReadAll() {
        return excelReadBuilder.doReadAllSheet(readExcelParams);
    }

    public List<T> doRead() {
        // 增加到第一位校验
        return excelReadBuilder.addFirstValidator(BeanDataValidator.newDataValidator(readExcelParams.getGroups()))
                .defaultReadHandler().doRead();
    }
}
