
package com.swak.operatelog.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OperateLog.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
@Documented
@Target({ java.lang.annotation.ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateLog {

	/**
	 * 模块
	 */
	  String module();

	/**
	 * 操作类型
	 */
	String operateType();

	/**
	 * 指定日志内容
	 */
	String content() default "";

	/**
	 * 过滤的字段
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
	 */
	LogScopeEnum  logScope() default LogScopeEnum.ALL;


}
