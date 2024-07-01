
package com.swak.core.extension.repository;

import com.swak.core.extension.ExtensionPoint;
import com.swak.core.extension.executor.ExtensionCoordinate;
import com.swak.common.dto.base.BizScenario;
import com.swak.common.exception.SwakException;
import com.swak.core.extension.annotation.ExtensionOperation;
import org.springframework.aop.framework.AopProxyUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultExtensionRepository implements ExtensionRepository {

	private final static String EXTENSION_EXTPT_NAMING = "ExtPt";

	private Map<ExtensionCoordinate, ExtensionPoint> extPtRepository = new ConcurrentHashMap<>();

	@Override
	public ExtensionPoint getExtension(ExtensionCoordinate extensionCoordinate) {
		return extPtRepository.get(extensionCoordinate);
	}

	@Override
	public ExtensionPoint doRegistration(ExtensionOperation extensionOperation) {
		BizScenario bizScenario = BizScenario.valueOf(extensionOperation.getBizId(), extensionOperation.getUseCase(),
				extensionOperation.getScenario());
		ExtensionCoordinate extensionCoordinate = new ExtensionCoordinate(calculateExtensionPoint(extensionOperation.getExtensionClass()),
				bizScenario.getUniqueIdentity());
		ExtensionPoint preVal = extPtRepository.put(extensionCoordinate, extensionOperation.getExtension());
		if (preVal != null) {
			throw new SwakException("Duplicate registration is not allowed for :" + extensionCoordinate);
		}
		return null;
	}

	/**
	 * @param targetClz
	 * @return
	 */
	private String calculateExtensionPoint(Class<?> targetClz) {
	  AopProxyUtils.ultimateTargetClass(targetClz);
		Class<?>[] interfaces = targetClz.getInterfaces();
		if (interfaces == null || interfaces.length == 0) {
			throw new SwakException("Please assign a extension point interface for " + targetClz);
		}
		for (Class<?> intf : interfaces) {
			String extensionPoint = intf.getSimpleName();
			if (extensionPoint.contains(EXTENSION_EXTPT_NAMING)) {
				return intf.getName();
			}
		}
		throw new SwakException("Your name of ExtensionPoint for " + targetClz + " is not valid, must be end of "
				+ EXTENSION_EXTPT_NAMING);
	}

	@Override
	public Map<ExtensionCoordinate, ExtensionPoint> getAllExtension() {
		return extPtRepository;
	}

	
}
