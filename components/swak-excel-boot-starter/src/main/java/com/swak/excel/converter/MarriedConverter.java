package com.swak.excel.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.google.common.collect.Lists;
import com.swak.common.enums.Married;
import com.swak.common.util.GetterUtil;
import com.swak.common.util.StringPool;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarriedConverter implements Converter<Integer> {

    private static MarriedConverter instance = new MarriedConverter();

    public static final List<String> VALUE;

    public static Map<Integer, String> toJavaMap = new HashMap<>();

    public static Map<String, Integer> toExcelMap = new HashMap<>();

    static {
        toJavaMap.put(Married.YES.getValue(), "Yes");
        toJavaMap.put(Married.NO.getValue(), "No");
        toExcelMap.put("Yes".toLowerCase(), Married.YES.getValue());
        toExcelMap.put("No".toLowerCase(), Married.NO.getValue());
        VALUE = Lists.newArrayList(toJavaMap.values());
    }

    @Override
    public Class<?> supportJavaTypeKey() {
        return Boolean.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) throws Exception {
        String data = cellData.getStringValue();
        if (StringUtils.isEmpty(data)) {
            return Married.NO.getValue();
        }
        return toExcelMap.getOrDefault(data.toLowerCase(),Married.NO.getValue());
    }

    @Override
    public WriteCellData<?> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) throws Exception {
        if (value == null) {
            new WriteCellData<>(StringPool.EMPTY);
        }
        return new WriteCellData<>(toJavaMap.getOrDefault(value,"No"));
    }

    public static MarriedConverter getConverter() {
        return instance;
    }

    public static void main(String[] args) throws Exception {
        ReadCellData<?> cellData = new ReadCellData<>("Yes");
        System.out.println(instance.convertToJavaData(cellData,null,null));
    }
}
