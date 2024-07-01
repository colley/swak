package com.swak.core.seq;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.ConcurrentHashMap;


/**
 * ID 生成器factory
 * 
 * @author liuliang8
 *
 */
public class MysqlIdGeneratorFactory implements IdGeneratorFactory {

    private static ConcurrentHashMap<String, IdGenerator> Factory =
        new ConcurrentHashMap<>();
    private JdbcTemplate jdbcTemplate;

    private String seqTableName;

    @Override
    public Long getNextIdByBiz(String bizTag) {
        IdGenerator disservice = null;

        if (Factory.get(bizTag) == null) {
            synchronized (Factory) {
                if (Factory.get(bizTag) == null) {
                    MySqlIdGeneratorServiceImpl service = new MySqlIdGeneratorServiceImpl();
                    service.setBizTag(bizTag);
                    service.setAsynLoadingSegment(true);
                    service.setJdbcTemplate(jdbcTemplate);
                    service.setSeqTableName(seqTableName);
                    service.init();
                    Factory.putIfAbsent(bizTag, service);

                }
            }
        }

        disservice = Factory.get(bizTag);
        return disservice.nextId();
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public void setSeqTableName(String seqTableName) {
        this.seqTableName = seqTableName;
    }
}
