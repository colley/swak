package com.swak.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.swak.common.i18n.I18nMessageUtil;
import com.swak.common.util.GetterUtil;
import com.swak.excel.metadata.WriteExcelParams;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;
import java.util.Objects;

public class I18nHeadNameHandler implements CellWriteHandler {

    private Class<?> headClazz;
    private  WriteExcelParams writeExcelParams;

    public I18nHeadNameHandler(Class<?> headClazz,WriteExcelParams writeExcelParams) {
        this.headClazz = headClazz;
        this.writeExcelParams = writeExcelParams;
    }

    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head, Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {
        if (!isHead || !writeExcelParams.isUseI18n() || Objects.isNull(headClazz) || Objects.isNull(head)) {
            return;
        }
        List<String> headNameList = head.getHeadNameList();
        if (CollectionUtils.isNotEmpty(headNameList)) {
            String defaultHeadName = headNameList.get(0);
            int startIndex = defaultHeadName.indexOf(DynamicCustomerHandler.placeholderPrefix);
            if (startIndex == -1) {
                String i18nKeyPrefix = GetterUtil.getString(writeExcelParams.getI18nKeyPrefix(),headClazz.getName());
                String i18nKey = i18nKeyPrefix + "." + head.getFieldName();
                String newHeadName = I18nMessageUtil.getMessage(i18nKey, defaultHeadName);
                headNameList.set(0, newHeadName);
            }
        }
    }
}
