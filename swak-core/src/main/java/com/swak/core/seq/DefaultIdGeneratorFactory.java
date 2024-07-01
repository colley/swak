package com.swak.core.seq;

import java.util.concurrent.ConcurrentHashMap;


/**
 * ID 生成器factory
 *
 */
public class DefaultIdGeneratorFactory implements IdGeneratorFactory {

    private static ConcurrentHashMap<String, IdGenerator> Factory =
        new ConcurrentHashMap<>();

    /**
     * 根据业务标签获取相应的id，这样可以动态的创建相应的服务，而不需要停服，
     * 这个接口相当于网关的作用
     *
     * @param bizTag
     * @return
     */
    @Override
    public Long getNextIdByBiz(String bizTag) {
        return Factory.computeIfAbsent(bizTag,(k)->new SwakDefaultIdGenerator()).nextId();
    }
}
