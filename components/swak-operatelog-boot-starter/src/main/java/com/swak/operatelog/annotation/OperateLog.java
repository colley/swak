
package com.swak.operatelog.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author colley.ma
 * @date 2021年4月7日 下午4:03:48
 */
@Documented
@Target({ java.lang.annotation.ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateLog {

	/**
	 * 模块
	 * @return
	 */
	  String module();

	/**
	 * 操作类型
	 * @return
	 */
	String operateType();

	/**
	 * 指定日志内容
	 * @return
	 */
	String content() default "";

	/**
	 * 过滤的字段
	 * @return
	 */
	String[] excludeField() default {};

	/**
	 * 是否记录方法参数
	 */
	boolean logArgs() default true;
	/**
	 * 是否记录方法结果的数据
	 */
	boolean logResult() default true;

	/**
	 * 记录日志范围
	 * @return
	 */
	LogScopeEnum  logScope() default LogScopeEnum.ALL;


}
