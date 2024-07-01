
package com.swak.archiver.spi;


import com.swak.archiver.ArchiveHandler;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import com.swak.common.chain.FilterInvoker;
import com.swak.common.exception.ArchiveException;
import com.swak.common.util.GetterUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 获取原表需要归档的maxId
 * MaxIdHandler.java
 *
 * @author ColleyMa
 * @version 19-5-7 下午4:20
 */
@Slf4j
public class MaxIdHandler implements ArchiveHandler {

    @Override
    public void doFilter(ArchiveItem context, FilterInvoker<ArchiveItem> nextFilter) {
        ArchiveConfig config = context.getConfig();
        try {
            //dataCount<=0  没有数据需要归档
            if (context.getDataCount() <= 0) {
                log.warn("原始表数获取 - 原始表: {} - 需要处理数为0 - where条件: {}",
                        new Object[]{config.getSrcTblName(), config.getWhere()});
                nextFilter.invoke(context);
                return;
            }

            /**
             * SELECT MAX(id) FROM xxx  where gmt_create<"2020-08-10";
             */
            StringBuilder builderSql = new StringBuilder();
            builderSql.append("SELECT MAX(id) FROM ").append(config.getSrcTblName());
            builderSql.append(" ").append(config.getWhere());
            Long maxId = context.getExecutor().findMaxId(builderSql.toString());
            log.warn("maxId获取 - 原始表: {} - 最大max(id) :{} - where条件: {}",
                    new Object[]{config.getSrcTblName(), maxId, config.getWhere()});
            //设置最大的MaxId
            context.setMaxId(maxId);
        } catch (Exception e) {
            log.error("获取原始表maxId报错  - 归档表 ：{} - where条件: {} - 异常信息： {}", config.getSrcTblName(), config.getWhere(), e.getMessage());
            throw new ArchiveException(e);
        }
        //不删除原表数据，需要获取nextmaxId
        if (!config.isPurge()) {
            this.handlerNextMaxId(context);
        }
        nextFilter.invoke(context);
    }

    /**
     * 获取归档目标表的最大maxId
     *
     * @param item
     */
    protected void handlerNextMaxId(ArchiveItem item) {
        ArchiveConfig config = item.getConfig();
        try {
            /**
             * SELECT MAX(id) FROM xxx_archive;
             */
            StringBuilder builderSql = new StringBuilder();
            builderSql.append("SELECT MAX(id) FROM ").append(config.getDesTblName());
            Long maxId = item.getExecutor().findMaxId(builderSql.toString());
            log.warn("目标表nextMaxId获取 - 目标表: {} - nextMaxId(id) :{}",
                    new Object[]{config.getDesTblName(), maxId});
            //设置目标表的最大值
            item.setNextMaxId(GetterUtil.getLong(maxId, 0L));
        } catch (Exception e) {
            log.error("目标表nextMaxId获取 - 目标表: {}  - 异常信息： {}", config.getDesTblName(), e.getMessage());
            throw new ArchiveException(e);
        }
    }


}
