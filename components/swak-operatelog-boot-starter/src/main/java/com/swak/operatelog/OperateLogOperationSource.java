package com.swak.operatelog;

import com.swak.core.interceptor.BasicOperationSource;
import com.swak.operatelog.annotation.OperateLogOperation;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;

public interface OperateLogOperationSource extends BasicOperationSource<OperateLogOperation> {
}
