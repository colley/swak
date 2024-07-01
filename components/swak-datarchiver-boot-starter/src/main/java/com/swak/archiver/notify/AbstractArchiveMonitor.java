
package com.swak.archiver.notify;

import com.google.common.base.Joiner;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveLog;

import java.util.Collections;
import java.util.Optional;


public abstract class AbstractArchiveMonitor implements ArchiveMonitor {

    @Override
    public void monitor(ArchiveLog archiveLog) {
        if (archiveLog.getConfig().isClickhouseDb()) {
            _monitor(monitorClickhouseLog(archiveLog));
            return;
        }
        _monitor(monitorLog(archiveLog));
    }

    public abstract void _monitor(String monitorLog);

    protected String monitorLog(ArchiveLog archiveLog) {
        ArchiveConfig config = archiveLog.getConfig();
        StringBuilder stringBuilder = new StringBuilder();
        if (archiveLog.isSucc()) {
            stringBuilder.append(config.isArchive() ? "归档数据成功汇总  " : "清除历史数据成功汇总");
        } else {
            stringBuilder.append(config.isArchive() ? "归档数据失败   " : "清除历史数据失败 ");
        }
        stringBuilder.append(" - 调度类型 : ").append(config.isArchive() ? "归档" : "历史数据清除");
        stringBuilder.append(" - 数据库类型 : mysql");
        stringBuilder.append(" - 数据库 : ").append(config.getDatabaseName());
        stringBuilder.append(" - 当前归档表 : ").append(config.getSrcTblName());
        if (config.isArchive()) {
            stringBuilder.append(" - 目标表 : ").append(config.getDesTblName());
        }
        stringBuilder.append(" - " + (config.isArchive() ? "未归档数 : " : "未删除数:"))
                .append(archiveLog.getRemainCount());
        stringBuilder.append(" - 最大max(id) : ").append(archiveLog.getMaxId());
        stringBuilder.append(" - Limit数 : ").append(archiveLog.getLimit());
        if (config.isArchive()) {
            stringBuilder.append(" - 归档总数 : ").append(archiveLog.getProgressSize());
            stringBuilder.append(" - 归档重复总数 : ").append(archiveLog.getRepeatNum());
        }
        stringBuilder.append(" - 总数据量: ").append(archiveLog.getDataCount());
        stringBuilder.append(" - 删除总数: ").append(archiveLog.getDelTotalNum());
        stringBuilder.append(" - 重试总次数 : ").append(archiveLog.getRetries());
        if (config.isArchive()) {
            stringBuilder.append(" - 是否删除归档表数据 : ").append(config.isPurge());
        }
        stringBuilder.append(" - where条件: ").append(config.getWhere());
        stringBuilder.append(" - 总花费时间: ").append(archiveLog.getCostTime());

        if (!archiveLog.isSucc()) {
            stringBuilder.append(" - 异常信息: ").append(archiveLog.getTraceInfo());
        }

        return stringBuilder.toString();
    }

    protected String monitorClickhouseLog(ArchiveLog archiveLog) {
        ArchiveConfig config = archiveLog.getConfig();
        StringBuilder stringBuilder = new StringBuilder();
        if (archiveLog.isSucc()) {
            stringBuilder.append(config.isArchive() ? "归档数据成功汇总  " : "清除历史数据成功汇总");
        } else {
            stringBuilder.append(config.isArchive() ? "归档数据失败   " : "清除历史数据失败 ");
        }
        stringBuilder.append(" - 调度类型 : ").append(config.isArchive() ? "归档" : "历史数据清除");
        stringBuilder.append(" - 数据库类型 : clickhouse");
        stringBuilder.append(" - 数据库 : ").append(config.getDatabaseName());
        stringBuilder.append(" - 元数据表 : ").append(config.getSrcTblName());
        if (config.isArchive()) {
            stringBuilder.append(" - 目标表 : ").append(config.getDesTblName());
        }
        stringBuilder.append(" - " + (config.isArchive() ? "归档分区 : " : "删除分区:"))
                .append(Joiner.on(",").join(Optional.ofNullable(archiveLog.getPartition()).orElse(Collections.emptyList())).toString());
        stringBuilder.append(" - 重试总次数 : ").append(archiveLog.getRetries());
        if (config.isArchive()) {
            stringBuilder.append(" - 是否删除归档表数据 : ").append(config.isPurge());
        }
        stringBuilder.append(" - where条件: ").append(config.getWhere());
        stringBuilder.append(" - 总花费时间: ").append(archiveLog.getCostTime());
        if (!archiveLog.isSucc()) {
            stringBuilder.append(" - 异常信息: ").append(archiveLog.getTraceInfo());
        }
        return stringBuilder.toString();
    }
}
