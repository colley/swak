package com.swak.core.extension.repository;

import com.google.common.collect.Lists;
import com.swak.common.key.ClassKey;
import com.swak.core.extension.ExtensionFinder;
import com.swak.core.extension.ExtensionPoint;
import com.swak.core.extension.annotation.Extension;
import com.swak.core.interceptor.SwakAnnotationUtils;
import com.swak.core.extension.annotation.ExtensionOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AnnotationExtensionFinder implements ExtensionFinder, ApplicationContextAware {

	private static final Set<Class<? extends Annotation>> EXTENSION_ANNOTATIONS = new LinkedHashSet<>(1);
	static {
		EXTENSION_ANNOTATIONS.add(Extension.class);
	}

	private static final Collection<ExtensionOperation> NULL_ATTRIBUTE = Collections.emptyList();

	private final Map<Object, Collection<ExtensionOperation>> attributeCache = new ConcurrentHashMap<>(1024);

	private ApplicationContext applicationContext;

	@Override
	public Collection<ExtensionOperation> find(Class<?> targetClass) {
		Object cacheKey = getCacheKey(targetClass);
		Collection<ExtensionOperation> cached = this.attributeCache.get(cacheKey);
		if (cached != null) {
			return (cached != NULL_ATTRIBUTE ? cached : null);
		} else {
			Collection<ExtensionOperation> operations = computeOperations(targetClass);
			if (CollectionUtils.isNotEmpty(operations)) {
				log.trace("[swak-extension] - Adding @Extension class '" + targetClass.getSimpleName()
						+ "' with attribute: " + operations);
				this.attributeCache.put(cacheKey, operations);
			} else {
				this.attributeCache.put(cacheKey, NULL_ATTRIBUTE);
			}
			return operations;
		}
	}

	protected Collection<ExtensionOperation> computeOperations(Class<?> targetClass) {
		Collection<? extends Annotation> anns = SwakAnnotationUtils.parseAnnotations(targetClass,
				EXTENSION_ANNOTATIONS);
		if (CollectionUtils.isNotEmpty(anns)) {
			// 处理@Extension
			final Collection<ExtensionOperation> operation = new ArrayList<>(1);
			anns.stream().filter(ann -> (ann instanceof Extension))
					.forEach(ann -> operation.add(parseExtensionAnnotation(targetClass, ann)));
			return operation;
		}
		return Collections.emptyList();
	}

	protected ExtensionOperation parseExtensionAnnotation(Class<?> targetClass, Annotation ann) {
		Extension extension = (Extension) ann;
		return ExtensionOperation.newOperation(targetClass, extension);
	}

	@Override
	public List<ExtensionOperation> find() {
		Map<String, ExtensionPoint> extensionInfos = applicationContext.getBeansOfType(ExtensionPoint.class);
		if (MapUtils.isEmpty(extensionInfos)) {
			log.debug("[swak-extension] - No extensions found");
			return Collections.emptyList();
		}
		List<ExtensionOperation> opets = Lists.newArrayListWithExpectedSize(extensionInfos.size());
		for (Map.Entry<String, ExtensionPoint> entry : extensionInfos.entrySet()) {
			Class<?> extensionClz = AopProxyUtils.ultimateTargetClass(entry.getValue());
			Collection<ExtensionOperation> extensionOperations = find(extensionClz);
			if (CollectionUtils.isNotEmpty(extensionOperations)) {
				ExtensionOperation extensionOperation = extensionOperations.iterator().next();
				extensionOperation.setExtension(entry.getValue());
				opets.add(extensionOperation);
			} else {
				log.warn("[swak-extension] - Found {} extensions for extension,but not mark @Extension",
						entry.getValue().getClass().getName());
			}
		}
		log.debug("[swak-extension] - Found {} extensions for @Extension", opets.size());
		return opets;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	protected Object getCacheKey(Class<?> targetClass) {
		return new ClassKey(targetClass);
	}

}
