package com.swak.core.seq;

/**
 * 根据业务标签获取相应的id
 * 
 * @author liuliang8
 *
 */
public interface IdGeneratorFactory {

    /**
     * 根据业务标签获取相应的id，这样可以动态的创建相应的服务，而不需要停服，
     * 这个接口相当于网关的作用
     * 
     * @param bizTag
     */
     Long getNextIdByBiz(String bizTag);

    default  Long getNextId() {
        return getNextIdByBiz("default");
    }
}
