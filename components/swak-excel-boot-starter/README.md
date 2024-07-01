## 原理
封装了easyExcel，统一表头样式和导入提供统一的校验

通过Spring Boot的autoConfig机制进行加载，无需手动配置，只需要添加如下依赖即可：
```xml
       	<dependency>
            <groupId>io.gitee.mcolley</groupId>
            <artifactId>swak-excel-boot-starter</artifactId>
        </dependency>
```


## 使用介绍
### 导出Excel
```java
        ExcelWriteData<UserExcel> excelWriteData = new ExcelWriteData<>();
        excelWriteData.setData(new ArrayList<>());
        for(int i=0;i<50;i++) {
        excelWriteData.getData().add(new UserExcel("colley"+i,i));
        }
        ExcelWriteHandler excelWriteHandler =  ExcelWriteHandler.write(outputStream,UserExcel.class);
        excelWriteHandler.writeExcelParams(new WriteExcelParams()).doWrite(excelWriteData);
```
#### 动态设置表头，使用占位符 ${xx} 方式实现动态表头
```java

    @Data
    @AllArgsConstructor
    public class UserExcel extends BaseRow {
        @ExcelProperty(value = "${name}")
        private String name;
        ......
    }
 Map<String,String> dynamicTitleHeader = Maps.newHashMap();
        dynamicTitleHeader.put("name","Swak测试动态title");
        
  excelWriteHandler.dynamicTitleHeader(dynamicTitleHeader).doWrite(excelWriteData);
```

#### 导出多个sheet
```java
  List<ExcelWriteData<?>> excelWriteDataList = Lists.newArrayList();
  excelWriteDataList.add(new ExcelWriteData(sheetName));
  excelWriteHandler.head(Class<?>... headClazzArray).doWrite(excelWriteDataList);
```
#### 导出非Bean的数据结构
```java
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
```

### 导入Excel
#### 单个sheetName导入
```java
ExcelReadHandler<UserExcel> excelReadHandler = ExcelReadHandler.read(file);
return excelReadHandler.head(UserExcel.class)
.readExcelParams(new ReadExcelParams())
.addBizValidator(item->dictionaryService.validate(command, item)) //业务逻辑检验
.doRead((sheetData)->dictionaryService.importRegister(command, sheetData)); //导入逻辑
```

#### 多个sheetName导入

开发中。。。。。。


