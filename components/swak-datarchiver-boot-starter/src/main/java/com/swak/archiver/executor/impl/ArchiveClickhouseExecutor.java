package com.swak.archiver.executor.impl;


import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ArchiveClickhouseExecutor extends AbsArchiveExecutor {

    @Override
    public void execute(ArchiveItem item) {
        for (String partition : item.getPartition()) {
            deletePartitionItem(item, partition);
            try {
                TimeUnit.MILLISECONDS.sleep(item.getConfig().getSleep());
                log.warn("每次删除一个分区后休眠{} 毫秒", item.getConfig().getSleep());
            } catch (Exception e) {
                //igore
                //每次删除一个分区后的休眠120秒（单位为秒）
                log.error("每次删除一个分区后休眠报错！");
            }
        }
    }


    public void deletePartitionItem(ArchiveItem item, String partition) {
        try {
            if (StringUtils.isEmpty(partition)) {
                return;
            }
            ArchiveConfig config = item.getConfig();
            /**
             * alter table 库.表名 drop partition 'partition';
             * e.g alter table fdc_dev.trace_data_local on cluster fdc_cluster drop partition '20230220';
             */
            StringBuilder builderSql = new StringBuilder();
            builderSql.append("alter table ").append(config.getDatabaseName()).append(".")
                    .append(config.getSrcTblName());
            if(StringUtils.isNotEmpty(config.getClusterName())) {
                builderSql.append(" ON CLUSTER ").append(config.getClusterName());
            }
            builderSql.append(" drop partition '").append(partition).append("'");

            log.info("执行删除分区sql:{}", builderSql);
            item.getExecutor().getJdbcTemplate().execute(builderSql.toString());
        } catch (Exception e) {
            int retries = item.getRetries().incrementAndGet();
            log.error("执行删除分区报错，重试次数为  retries:{}", retries, e);
            if (item.getRetries().get() >= item.getConfig().getRetries()) {
                //超过重试次数
                return;
            }
            try {
                //500ms后重试
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e1) {
                //igore
                log.error("{} ms后重试！", 500);
            }
        }
    }

    /**
     * 归档数据
     *
     */
    @Override
    public int archiveItem (ArchiveItem item, List<Map< String, Object >> archiveData){
        return 0;
    }

    /**
     * 归档成功后删除原始表数据
     *
     */
    @Override
    public int deleteItem (ArchiveItem item, List<Map< String, Object >> archiveData){
        return 0;
    }

    public static void main(String[] args) {
        String builderSql = "alter table " + "fdc_dev" + "." +
                "trace_data_local" +
                " drop partition '" + "20230220" + "'" +
                " on cluster " + "fdc_cluster";
        System.out.println(builderSql);
    }
}
