package com.swak.archiver.conf;

import com.swak.archiver.notify.ArchiveMonitor;
import com.swak.common.dto.base.DTO;
import lombok.Data;

import javax.sql.DataSource;

@Data
public class ArchiverProperties  implements DTO {
    private DataSource mysqlDataSource;
    private DataSource clickhouseDataSource;
    private ArchiveMonitor monitor;
}
