/**
 * Copyright (C), 2018 store
 * Encoding: UTF-8
 * Date: 19-5-13 下午2:26
 * History:
 */
package com.swak.archiver.executor;


import com.swak.archiver.conf.ArchiveConfig;

/**
 * ArchiverDataEngine.java
 * @author ColleyMa
 * @version 19-5-13 下午2:26
 */
public interface ArchiverDataEngine {
	
     void engine(ArchiveConfig config);
    
     void cancel();
}
