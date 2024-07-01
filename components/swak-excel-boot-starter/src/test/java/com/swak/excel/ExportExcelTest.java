package com.swak.excel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swak.excel.metadata.ExcelWriteData;
import com.swak.excel.metadata.ExcelWriteDynamicData;
import com.swak.excel.metadata.WriteExcelParams;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

public class ExportExcelTest {


    @Test
    public void testExport() throws FileNotFoundException {
        Map<String,String> dynamicTitleHeader = Maps.newHashMap();
        dynamicTitleHeader.put("name","Swak测试动态title");

        ExcelWriteData<UserExcel> excelWriteData = new ExcelWriteData<>();
        excelWriteData.setData(new ArrayList<>());
        for(int i=0;i<50;i++) {
            excelWriteData.getData().add(new UserExcel("colley"+i,i));
        }
        String fileName = ExportExcelTest.class.getResource("/").getPath() + "write" + System.currentTimeMillis() + ".xlsx";
        FileOutputStream outputStream = new FileOutputStream(fileName);
      ExcelWriteHandler
              .write(outputStream,UserExcel.class)
                       .writeExcelParams(new WriteExcelParams())
        .dynamicTitleHeader(dynamicTitleHeader).doWrite(excelWriteData);

        System.out.println(fileName);

    }

    @Test
    public void testDynamicHeadExport() throws FileNotFoundException {
        ExcelWriteDynamicData excelWriteData = new ExcelWriteDynamicData();
        excelWriteData.head("蓄力","技术");
        excelWriteData.setData(new ArrayList<>());
        for(int i=0;i<50;i++) {
            excelWriteData.getData().add(Lists.newArrayList(i,"colley"+i));
        }
        String fileName = ExportExcelTest.class.getResource("/").getPath() + "write" + System.currentTimeMillis() + ".xlsx";
        FileOutputStream outputStream = new FileOutputStream(fileName);
        ExcelWriteHandler.write(outputStream)
                .writeExcelParams(new WriteExcelParams())
               .doWrite(excelWriteData);
        System.out.println(fileName);

    }

}
