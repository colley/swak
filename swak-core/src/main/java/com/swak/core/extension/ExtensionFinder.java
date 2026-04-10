
package com.swak.core.extension;

import com.swak.core.extension.annotation.ExtensionOperation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface ExtensionFinder {

	/**
	 * find all ExtensionPoint
	 */
	default List<ExtensionOperation> find() {
		return Collections.emptyList();
	}

	/**
	 * find ExtensionPoint by targetClass
	 * 
	 * @param targetClass
	 */
	default Collection<ExtensionOperation> find(Class<?> targetClass) {
		return Collections.emptyList();
	}
}
