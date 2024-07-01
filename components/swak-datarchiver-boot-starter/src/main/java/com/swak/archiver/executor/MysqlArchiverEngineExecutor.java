package com.swak.archiver.executor;

import com.swak.archiver.ArchiveHandler;
import com.swak.archiver.common.SwakTemplateExecutor;
import com.swak.archiver.notify.ArchiveMonitor;
import com.swak.archiver.spi.CountHandler;
import com.swak.archiver.spi.ExecuteMysqlHandler;
import com.swak.archiver.spi.MaxIdHandler;
import com.swak.archiver.spi.TableCheckHandler;

import java.util.ArrayList;
import java.util.List;

public class MysqlArchiverEngineExecutor extends ArchiverEngineExecutor{

    private static final List<ArchiveHandler> ARCHIVER_MYSQL = new ArrayList<ArchiveHandler>(){{
        add(new TableCheckHandler());
        add(new CountHandler());
        add(new MaxIdHandler());
        add(new ExecuteMysqlHandler());
    }};

    public MysqlArchiverEngineExecutor(SwakTemplateExecutor executor, ArchiveMonitor monitor) {
        super(executor, monitor);
    }

    @Override
    protected List<ArchiveHandler> getArchiveHandler() {
        return ARCHIVER_MYSQL;
    }
}
