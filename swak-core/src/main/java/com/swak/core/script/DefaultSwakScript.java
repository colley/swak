package com.swak.core.script;

import com.swak.common.exception.SwakException;
import com.swak.common.util.DigestUtils;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.scripting.support.StaticScriptSource;

import java.io.IOException;

public class DefaultSwakScript<T> implements SwakScript<T>{
    @Nullable
    private ScriptSource scriptSource;
    @Nullable
    private Class<T> resultType;

    private String uniqueCode;

    public DefaultSwakScript(){
    }

    public DefaultSwakScript(String script) {
        this(script, (Class)null);
    }

    public DefaultSwakScript(String script, @Nullable Class<T> resultType) {
        this.setScriptText(script);
        this.resultType = resultType;
    }

    @Override
    public ScriptSource getScriptSource() {
        return scriptSource;
    }

    @Override
    @Nullable
    public Class<T> getResultType() {
        return this.resultType;
    }

    @Override
    public String getScriptAsString() {
        try {
            return this.scriptSource.getScriptAsString();
        } catch (IOException var2) {
            throw new SwakException("Error reading script text", var2);
        }
    }

    @Override
    public String getUniqueCode() {
        return this.uniqueCode;
    }

    public void setResultType(@Nullable Class<T> resultType) {
        this.resultType = resultType;
    }

    public void setScriptText(String scriptText) {
        this.scriptSource = new StaticScriptSource(scriptText);
        this.uniqueCode = DigestUtils.md5(getScriptAsString());
    }

    public void setLocation(Resource scriptLocation) {
        this.scriptSource = new ResourceScriptSource(scriptLocation);
        this.uniqueCode = DigestUtils.md5(getScriptAsString());
    }

    public void setScriptSource(ScriptSource scriptSource) {
        this.scriptSource = scriptSource;
        this.uniqueCode = DigestUtils.md5(getScriptAsString());
    }
}
