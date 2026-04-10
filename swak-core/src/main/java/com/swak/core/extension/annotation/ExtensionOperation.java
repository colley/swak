
package com.swak.core.extension.annotation;

import com.swak.core.extension.ExtensionPoint;
import com.swak.common.dto.base.BaseOperation;

/**
 * The type Extension operation.
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class ExtensionOperation implements BaseOperation {
	private static final long serialVersionUID = -4720640499045739269L;

	/**
	 * The Extension class.
	 */
	public Class<?> extensionClass;

	/**
	 * 扩展业务编码
	 */
	private String bizId;

	/**
	 * 扩展case
	 */
	private String useCase;

	/**
	 * 扩展场景
	 */
	private String scenario;

	/**
	 * 扩展描述
	 */
	private String desc;

	/**
	 * The Extension.
	 */
	public ExtensionPoint extension;

	/**
	 * New operation extension operation.
	 *
	 * @param extensionClass the extension class
	 * @param extension      the extension
	 * @return the extension operation
	 */
	public static ExtensionOperation newOperation(Class<?> extensionClass,Extension extension) {
		ExtensionOperation operation = new ExtensionOperation();
		operation.setExtensionClass(extensionClass);
		operation.setBizId(extension.bizId());
		operation.setUseCase(extension.useCase());
		operation.setScenario(extension.scenario());
		operation.setDesc(extension.desc());
		return operation;
	}

	/**
	 * Gets biz id.
	 *
	 * @return the biz id
	 */
	public String getBizId() {
		return bizId;
	}

	/**
	 * Sets biz id.
	 *
	 * @param bizId the biz id
	 */
	public void setBizId(String bizId) {
		this.bizId = bizId;
	}

	/**
	 * Gets use case.
	 *
	 * @return the use case
	 */
	public String getUseCase() {
		return useCase;
	}

	/**
	 * Sets use case.
	 *
	 * @param useCase the use case
	 */
	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}

	/**
	 * Gets scenario.
	 *
	 * @return the scenario
	 */
	public String getScenario() {
		return scenario;
	}

	/**
	 * Sets scenario.
	 *
	 * @param scenario the scenario
	 */
	public void setScenario(String scenario) {
		this.scenario = scenario;
	}

	/**
	 * Gets desc.
	 *
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Sets desc.
	 *
	 * @param desc the desc
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * Gets extension class.
	 *
	 * @return the extension class
	 */
	public Class<?> getExtensionClass() {
		return extensionClass;
	}

	/**
	 * Sets extension class.
	 *
	 * @param extensionClass the extension class
	 */
	public void setExtensionClass(Class<?> extensionClass) {
		this.extensionClass = extensionClass;
	}

	/**
	 * Gets extension.
	 *
	 * @return the extension
	 */
	public ExtensionPoint getExtension() {
		return extension;
	}

	/**
	 * Sets extension.
	 *
	 * @param extension the extension
	 */
	public void setExtension(ExtensionPoint extension) {
		this.extension = extension;
	}

}
