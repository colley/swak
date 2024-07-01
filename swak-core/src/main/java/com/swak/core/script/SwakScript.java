package com.swak.core.script;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.scripting.ScriptSource;
import org.springframework.util.Assert;

public interface SwakScript<T> {

     ScriptSource getScriptSource();

    @Nullable
    Class<T> getResultType();

    String getScriptAsString();

    String getUniqueCode();

    default boolean returnsRawValue() {
        return this.getResultType() == null;
    }

    static <T> SwakScript<T> of(String script) {
        return new DefaultSwakScript(script);
    }

    static <T> SwakScript<T> of(String script, Class<T> resultType) {
        Assert.notNull(script, "Script must not be null!");
        Assert.notNull(resultType, "ResultType must not be null!");
        return new DefaultSwakScript(script, resultType);
    }

    static <T> SwakScript<T> of(Resource resource) {
        Assert.notNull(resource, "Resource must not be null!");
        DefaultSwakScript<T> script = new DefaultSwakScript();
        script.setLocation(resource);
        return script;
    }

    static <T> SwakScript<T> of(Resource resource, Class<T> resultType) {
        Assert.notNull(resource, "Resource must not be null!");
        Assert.notNull(resultType, "ResultType must not be null!");
        DefaultSwakScript<T> script = new DefaultSwakScript();
        script.setResultType(resultType);
        script.setLocation(resource);
        return script;
    }
}
