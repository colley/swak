
package com.swak.archiver.conf;

import com.swak.archiver.common.SwakTemplateExecutor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ArchiveItem.java
 *
 * @author ColleyMa
 * @version 19-5-7 下午5:21
*/
@Data
public class ArchiveItem implements Serializable {
    private static final long serialVersionUID = 4977207034726930627L;
    private SwakTemplateExecutor executor;
    private String[] cols;
    private ArchiveConfig config;
    private Long maxId;
    
    //需要归档的数据量
    private Integer dataCount = 0;
    
    protected AtomicInteger progress;
    
    protected AtomicInteger delNum;
    
    /**重复数据**/
    protected AtomicInteger repeatNum;
    
    protected AtomicInteger retries;
    
    protected long startTime;
    
    //next limit maxId
    protected Long nextMaxId;

    /**
     * 分区
     */
    protected List<String> partition;

    public ArchiveItem(ArchiveConfig config) {
        this.config = config;
        this.progress= new AtomicInteger(0);
        this.retries = new AtomicInteger(0);
        this.delNum = new AtomicInteger(0);
        this.repeatNum = new AtomicInteger(0);
        this.startTime = System.currentTimeMillis(); 
    }
}
