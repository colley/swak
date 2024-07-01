package com.swak.archiver.executor;

import com.swak.archiver.ArchiveHandler;
import com.swak.archiver.common.SwakTemplateExecutor;
import com.swak.archiver.notify.ArchiveMonitor;
import com.swak.archiver.spi.clickhouse.ExecuteClickHandler;
import com.swak.archiver.spi.clickhouse.PartitionCheckHandler;
import com.swak.archiver.spi.clickhouse.PartitionClickhouseHandler;

import java.util.ArrayList;
import java.util.List;

public class ClickArchiverEngineExecutor extends ArchiverEngineExecutor{

    private static final List<ArchiveHandler> ARCHIVER_CLICK_HOUSE = new ArrayList<ArchiveHandler>(){{
        add(new PartitionClickhouseHandler());
        add(new PartitionCheckHandler());
        add(new ExecuteClickHandler());
    }};

    public ClickArchiverEngineExecutor(SwakTemplateExecutor executor, ArchiveMonitor monitor) {
        super(executor, monitor);
    }

    @Override
    protected List<ArchiveHandler> getArchiveHandler() {
        return ARCHIVER_CLICK_HOUSE;
    }
}
