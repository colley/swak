package com.swak.excel;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.google.common.collect.Lists;
import com.swak.excel.builder.SwakExcelWriterBuilder;
import com.swak.excel.metadata.ExcelWriteData;
import com.swak.excel.metadata.ExcelWriteDynamicData;
import com.swak.excel.metadata.WriteExcelParams;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.OutputStream;
import java.util.*;

public class ExcelWriteHandler {

    private SwakExcelWriterBuilder excelWriterBuilder;

    private WriteExcelParams writeExcelParams;



    public ExcelWriteHandler(SwakExcelWriterBuilder excelWriterBuilder) {
        this.excelWriterBuilder = excelWriterBuilder;
    }

    public static ExcelWriteHandler write(OutputStream out) {
        return new ExcelWriteHandler(SwakExcelWriterBuilder.write(out));
    }

    public static ExcelWriteHandler write(OutputStream out, Class<?> head) {
        return new ExcelWriteHandler(SwakExcelWriterBuilder.write(out, head));
    }

    public ExcelWriteHandler head(Class<?> headClazz) {
        excelWriterBuilder.head(headClazz);

        return this;
    }

    //设置需要导出的头部信息
    public ExcelWriteHandler includeColumnFieldNames(Collection<String> includeColumnFieldNames) {
         if(CollectionUtils.isNotEmpty(includeColumnFieldNames)){
             excelWriterBuilder.includeColumnFieldNames(includeColumnFieldNames);
         }
        return this;
    }

    public ExcelWriteHandler writeExcelParams(WriteExcelParams writeExcelParams) {
        this.writeExcelParams = writeExcelParams;
        return this;
    }

    public ExcelWriteHandler dynamicTitleHeader(Map<String, String> dynamicHeader) {
        if (MapUtils.isNotEmpty(dynamicHeader)) {
            excelWriterBuilder.dynamicTitleHeader(dynamicHeader,writeExcelParams);
        }
        return this;
    }

    public void doWrite(QueryDataHandler<ExcelWriteData<?>> query) {
        doWrite(query.query());
    }

    public void doDynamicWrite(QueryDataHandler<ExcelWriteDynamicData> query) {
        doDynamicWrite(query.query());
    }

    public void doWrite(MultiQueryDataHandler<ExcelWriteData<?>> multiQuery) {
        // 多个sheet导出
        doWrite(Optional.ofNullable(multiQuery.query()).orElse(Lists.newArrayList()));
    }

    public ExcelWriteHandler registerWriteHandler(WriteHandler writeHandler) {
        excelWriterBuilder.registerWriteHandler(writeHandler);
        return this;
    }

    public void doDynamicWrite(MultiQueryDataHandler<ExcelWriteDynamicData> multiQuery) {
        // 多个sheet导出
        doDynamicWrite(Optional.ofNullable(multiQuery.query()).orElse(Lists.newArrayList()));
    }

    public void doWrite(List<ExcelWriteData<?>> excelWriteDataList) {
        // 多个sheet导出
        try (ExcelWriter excelWriter = excelWriterBuilder.defaultWriteHandler(writeExcelParams).build()) {
            for (int i = 0; i < excelWriteDataList.size(); i++) {
                ExcelWriteData<?> excelWriteData = excelWriteDataList.get(i);
                String sheetName = StringUtils.firstNonBlank(excelWriteData.getSheetName(),writeExcelParams.getSheetName());
                ExcelWriterSheetBuilder writerSheetBuilder = EasyExcel.writerSheet(i, sheetName).head(excelWriteData.getHeadClazz());
                if(CollectionUtils.isNotEmpty(excelWriteData.getIncludeColumnFieldNames())){
                    writerSheetBuilder.includeColumnFieldNames(excelWriteData.getIncludeColumnFieldNames());
                }
                writerSheetBuilder.registerWriteHandler(new I18nHeadNameHandler(excelWriteData.getHeadClazz(),writeExcelParams))
                        .registerWriteHandler(new DataValidationCellWriteHandler(excelWriteData));
                if(MapUtils.isNotEmpty(excelWriteData.getDynamicTitleHeader())){
                    writerSheetBuilder.registerWriteHandler(new DynamicCustomerHandler(excelWriteData.getDynamicTitleHeader(),writeExcelParams));
                }
                excelWriter.write(Optional.ofNullable(excelWriteData.getData()).orElse(Collections.emptyList()),writerSheetBuilder.build());
            }
            excelWriter.finish();
        }
    }

    public void doDynamicWrite(List<ExcelWriteDynamicData> excelWriteDataList) {
        // 多个sheet导出
        try (ExcelWriter excelWriter = excelWriterBuilder.defaultWriteHandler(writeExcelParams).build()) {
            for (int i = 0; i < excelWriteDataList.size(); i++) {
                ExcelWriteDynamicData excelWriteData = excelWriteDataList.get(i);
                String sheetName = StringUtils.firstNonBlank(excelWriteData.getSheetName(),
                        writeExcelParams.getSheetName());
                WriteSheet writeSheet = EasyExcel.writerSheet(i, sheetName)
                        .head(excelWriteData.getHeads()).build();
                excelWriter.write(Optional.ofNullable(excelWriteData.getData()).orElse(Collections.emptyList()),
                        writeSheet);
            }
            excelWriter.finish();
        }
    }

    public void doWrite(ExcelWriteData<?> writeData) {
        if (writeData instanceof ExcelWriteDynamicData) {
            doDynamicWrite((ExcelWriteDynamicData) writeData);
            return;
        }
        if (Objects.nonNull(writeData.getHeadClazz())) {
            excelWriterBuilder.head(writeData.getHeadClazz());
        }
        if(CollectionUtils.isNotEmpty(writeData.getIncludeColumnFieldNames())){
            excelWriterBuilder.includeColumnFieldNames(writeData.getIncludeColumnFieldNames());
        }
        String sheetName = StringUtils.firstNonBlank(writeData.getSheetName(),
                writeExcelParams.getSheetName());
        excelWriterBuilder.defaultWriteHandler(writeExcelParams, writeData).sheet(0, sheetName)
                .doWrite(writeData.getData());
    }

    public void doDynamicWrite(ExcelWriteDynamicData writeData) {
        excelWriterBuilder.head(writeData.getHeads());
        try (ExcelWriter excelWriter = excelWriterBuilder.defaultWriteHandler(writeExcelParams,writeData).build()) {
            String sheetName = StringUtils.firstNonBlank(writeData.getSheetName(),
                    writeExcelParams.getSheetName());
            WriteSheet writeSheet = EasyExcel.writerSheet(0, sheetName).build();
            excelWriter.write(Optional.ofNullable(writeData.getData()).orElse(Collections.emptyList()),
                    writeSheet);
            excelWriter.finish();
        }
    }
}
