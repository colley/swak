package com.swak.formula.transform;

import com.swak.formula.plugin.MathFunctionPlugin;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.runtime.metaclass.MetaMethodIndex;

/**
 * SwaMetaMethod.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class SwaMetaMethod extends MetaMethod {

    @Override
    public int getModifiers() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Class getReturnType() {
        return null;
    }

    @Override
    public CachedClass getDeclaringClass() {
        return null;
    }

    @Override
    public Object invoke(Object o, Object[] objects) {
        MetaClass metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(MathFunctionPlugin.class);
        return null;
    }
}
