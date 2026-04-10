
package com.swak.core.extension.executor;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Sets;
import com.swak.core.extension.repository.ExtensionRepository;
import com.swak.common.dto.base.BizScenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 扩展点默认实现
 *  DefaultExtensionExecutor.java
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public class DefaultExtensionExecutor implements ExtensionExecutor {

	@Resource
	private ExtensionRepository extensionRepository;

	@Override
	public <C> C locateComponent(Class<C> targetClz, BizScenario bizScenario) {
		C extension = locateExtension(targetClz, bizScenario);
		log.debug("[swak-Extension] - [Located Extension]: " + extension.getClass().getName());
		return extension;
	}

	/**
	 * 根据BizScenario获取扩展点
	 * @param <Ext>
	 * @param targetClz
	 * @param bizScenario
	 * @return Ext
	 */
	protected <Ext> Ext locateExtension(Class<Ext> targetClz, BizScenario bizScenario) {
		Assert.notNull(bizScenario, "[swak-Extension] - BizScenario can not be null for extension");
		Ext extension = null;
		// 排序去重,减少非必要的namespace检索
		Set<String> uniqueIdentitySet = getUniqueIdentitySet(bizScenario);
		log.debug("[swak-Extension] - BizScenario uniqueIdentitySet is:{}", JSON.toJSONString(uniqueIdentitySet));
		for (String uniqueIdentity : uniqueIdentitySet) {
			extension = locate(targetClz.getName(), uniqueIdentity);
			if (extension != null) {
				log.debug("[swak-Extension] - found ExtensionPoint [{}] by [{}]", extension.getClass().getName(), uniqueIdentity);
				return extension;
			}
		}
		throw new RuntimeException("[swak-Extension] - Can not find extension with ExtensionPoint: " + targetClz + " BizScenario:"
				+ bizScenario.getUniqueIdentity());
	}

	/**
	 * 检索排序和去重
	 * 
	 * @param bizScenario
	 * @return
	 */
	private Set<String> getUniqueIdentitySet(BizScenario bizScenario) {
		Set<String> uniqueSet = Sets.newLinkedHashSetWithExpectedSize(4);
		uniqueSet.add(bizScenario.getUniqueIdentity());
		uniqueSet.add(bizScenario.getIdentityWithDefaultScenario());
		uniqueSet.add(bizScenario.getIdentityWithDefaultUseCase());
		uniqueSet.add(bizScenario.getDefault());
		return uniqueSet;
	}

	/**
	 * first try with full namespace example: biz1.useCase1.scenario1
	 */
	protected <Ext> Ext firstTry(Class<Ext> targetClz, BizScenario bizScenario) {
		log.debug("[swak-Extension] - First trying with " + bizScenario.getUniqueIdentity());
		Ext ext = locate(targetClz.getName(), bizScenario.getUniqueIdentity());
		log.debug("[swak-Extension] - firstTry#bizScenario:{} [Located Extension]: {}", bizScenario.getUniqueIdentity(),
				ext == null ? null : ext.getClass().getName());
		return ext;
	}

	/**
	 * second try with default scenario
	 *
	 * example: biz1.useCase1.#defaultScenario#
	 */
	protected <Ext> Ext secondTry(Class<Ext> targetClz, BizScenario bizScenario) {
		log.debug("[swak-Extension] - Second trying with " + bizScenario.getIdentityWithDefaultScenario());
		Ext ext = locate(targetClz.getName(), bizScenario.getIdentityWithDefaultScenario());
		log.debug("[swak-Extension] - secondTry#bizScenario:{} [Located Extension]: {}", bizScenario.getIdentityWithDefaultScenario(),
				ext == null ? null : ext.getClass().getName());
		return ext;
	}

	/**
	 * third try with default use case + default scenario
	 *
	 * example: biz1.#defaultUseCase#.#defaultScenario#
	 */
	protected <Ext> Ext defaultUseCaseTry(Class<Ext> targetClz, BizScenario bizScenario) {
		log.debug("[swak-Extension] - Third trying with " + bizScenario.getIdentityWithDefaultUseCase());
		Ext ext = locate(targetClz.getName(), bizScenario.getIdentityWithDefaultUseCase());
		log.debug("[swak-Extension] - defaultUseCaseTry#bizScenario:{} [Located Extension]: {}",
				bizScenario.getIdentityWithDefaultUseCase(), ext == null ? null : ext.getClass().getName());
		return ext;
	}

	protected <Ext> Ext defaultTry(Class<Ext> targetClz, BizScenario bizScenario) {
		log.debug("[swak-Extension] - default trying with " + bizScenario.getDefault());
		Ext ext = locate(targetClz.getName(), bizScenario.getDefault());
		log.debug("[swak-Extension] - defaultTry#bizScenario:{} [Located Extension]: {}", bizScenario.getDefault(),
				ext == null ? null : ext.getClass().getName());
		return ext;
	}

	@SuppressWarnings("unchecked")
	private <Ext> Ext locate(String name, String uniqueIdentity) {
		return (Ext) extensionRepository.getExtension(new ExtensionCoordinate(name, uniqueIdentity));
	}
}
