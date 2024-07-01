package com.swak.excel.validation;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.metadata.holder.ReadSheetHolder;
import com.alibaba.excel.read.metadata.property.ExcelReadHeadProperty;
import com.google.common.collect.Lists;
import com.swak.excel.metadata.ExcelRow;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ExcelAnalysisDataHandler extends AnalysisEventListener<ExcelRow> {

    private final Map<String, Integer> colFieldHeadMap = new HashMap<String, Integer>();

    private List<RowDataValidator<?>> dataValidators = new ArrayList<>();

    private List<BizRowDataValidator<?>> bizValidators = new ArrayList<>();

    private List<ExcelRow> list = new ArrayList<ExcelRow>();

    private Map<Integer, List<String>> rowHeadMap = new HashMap<>();

    public ExcelAnalysisDataHandler() {
    }

    public ExcelAnalysisDataHandler(RowDataValidator<?>... validators) {
        if (ArrayUtils.isNotEmpty(validators)) {
            dataValidators.addAll(Lists.newArrayList(validators));
        }
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        super.invokeHead(headMap, context);
        ExcelReadHeadProperty excelReadHead = context.readSheetHolder().excelReadHeadProperty();
        excelReadHead.getHeadMap().forEach((k, v) -> {
            colFieldHeadMap.put(v.getFieldName(), k);
            rowHeadMap.put(k, v.getHeadNameList());
        });
    }


    @Override
    public void invoke(ExcelRow data, AnalysisContext context) {
        // 校验成功add数据
        data.setRowIndex(context.readRowHolder().getRowIndex());
        data.setRowHead(rowHeadMap);
        ReadSheetHolder readSheetHolder = context.readSheetHolder();
        data.setSheetName(readSheetHolder.getSheetName());
        for (RowDataValidator validator : dataValidators) {
            validator.validate(data, colFieldHeadMap);
        }
        list.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 验证逻辑判断
    }

    public <T> List<T> readAllData() {
        List<T> dataList = (List<T>) list;
        // 业务检验,总体校验
        for (BizRowDataValidator validator : bizValidators) {
            validator.validate(dataList, colFieldHeadMap);
        }
        return dataList;
    }

    public void addDataValidator(RowDataValidator<?> validator) {
        dataValidators.add(validator);
    }

    public void addFirstDataValidator(RowDataValidator<?> validator) {
        dataValidators.add(0, validator);
    }

    public void addBizValidator(BizRowDataValidator<?> validator) {
        bizValidators.add(validator);
    }
}
