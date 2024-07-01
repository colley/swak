
package com.swak.core.extension.repository;

import com.swak.core.extension.ExtensionPoint;
import com.swak.core.extension.annotation.ExtensionOperation;
import com.swak.core.extension.executor.ExtensionCoordinate;

import java.util.Map;

/**
 * ExtensionRepository
 */

public interface ExtensionRepository {
	
	ExtensionPoint doRegistration(ExtensionOperation extensionOperation);

	ExtensionPoint getExtension(ExtensionCoordinate extensionCoordinate);
	
	
	Map<ExtensionCoordinate, ExtensionPoint> getAllExtension();

}
