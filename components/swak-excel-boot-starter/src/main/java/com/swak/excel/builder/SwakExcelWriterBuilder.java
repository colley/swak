package com.swak.excel.builder;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.swak.common.util.StringPool;
import com.swak.excel.AutoColumnWidthStyleStrategy;
import com.swak.excel.DataValidationCellWriteHandler;
import com.swak.excel.DynamicCustomerHandler;
import com.swak.excel.I18nHeadNameHandler;
import com.swak.excel.converter.BooleanStrConverter;
import com.swak.excel.metadata.ExcelWriteData;
import com.swak.excel.metadata.WriteExcelParams;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SwakExcelWriterBuilder extends ExcelWriterBuilder {


    public static SwakExcelWriterBuilder write(OutputStream outputStream) {
        return write(outputStream, null);
    }

    public static SwakExcelWriterBuilder write(OutputStream outputStream, Class<?> head) {
        SwakExcelWriterBuilder excelWriterBuilder = new SwakExcelWriterBuilder();
        excelWriterBuilder.file(outputStream);
        if (head != null) {
            excelWriterBuilder.head(head);
        }
        return excelWriterBuilder;
    }


    @Override
    public SwakExcelWriterBuilder registerConverter(Converter<?> converter) {
        super.registerConverter(converter);
        return this;
    }

    public SwakExcelWriterBuilder registerConverter(List<Converter<?>> converters) {
        if (CollectionUtils.isNotEmpty(converters)) {
            for (Converter<?> converter : converters) {
                super.registerConverter(converter);
            }
        }
        return this;
    }

    public SwakExcelWriterBuilder dynamicTitleHeader(Map<String, String> dynamicHeader,WriteExcelParams writeExcelParams) {
        if(MapUtils.isNotEmpty(dynamicHeader)){
            super.registerWriteHandler(new DynamicCustomerHandler(dynamicHeader,writeExcelParams));
        }
        return this;
    }

    @Override
    public SwakExcelWriterBuilder registerWriteHandler(WriteHandler writeHandler) {
        super.registerWriteHandler(writeHandler);
        return this;
    }

    public SwakExcelWriterBuilder registerWriteHandler(List<WriteHandler> writeHandlers) {
        if (CollectionUtils.isNotEmpty(writeHandlers)) {
            for (WriteHandler writeHandler : writeHandlers) {
                super.registerWriteHandler(writeHandler);
            }
        }
        return this;
    }

    public SwakExcelWriterBuilder defaultWriteHandler(WriteExcelParams writeParams) {
        if (StringUtils.isNoneBlank(writeParams.getTemplatePath())) {
            super.withTemplate(writeParams.getTemplatePath());
        }
        super.autoCloseStream(Boolean.TRUE).charset(Charset.forName(StringPool.UTF8))
                .password(writeParams.getPassword()).excelType(writeParams.getExcelType());
        super.registerWriteHandler(AutoColumnWidthStyleStrategy.createStyleStrategy(writeParams));
        if (writeParams.isAutoColumnWidth()) {
            super.registerWriteHandler(new AutoColumnWidthStyleStrategy());
        }
        super.registerWriteHandler(new AutoColumnWidthStyleStrategy.CustomRowHeightStyleStrategy(writeParams));
        defaultRegisterConverter();
        return this;
    }

    public SwakExcelWriterBuilder defaultWriteHandler(WriteExcelParams writeParams, ExcelWriteData<?> excelWriteData) {
        defaultWriteHandler(writeParams).registerWriteHandler(new DataValidationCellWriteHandler(excelWriteData))
                .i18nWriteHandler(writeParams, excelWriteData).dynamicTitleHeader(excelWriteData.getDynamicTitleHeader(),writeParams);
        return this;
    }

    public SwakExcelWriterBuilder i18nWriteHandler(WriteExcelParams writeParams, ExcelWriteData<?> excelWriteData) {
        if (writeParams.isUseI18n()) {
            Class<?> headClazz = excelWriteData.getHeadClazz();
            if (Objects.isNull(headClazz)) {
                headClazz = parameter().getClazz();
            }
            super.registerWriteHandler(new I18nHeadNameHandler(headClazz, writeParams));
        }
        return this;
    }

    public SwakExcelWriterBuilder defaultRegisterConverter() {
        registerConverter(BooleanStrConverter.getConverter());
        return this;
    }
}
