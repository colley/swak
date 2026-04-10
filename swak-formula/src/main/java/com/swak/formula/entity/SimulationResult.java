package com.swak.formula.entity;

import lombok.Data;

import java.util.Map;

@Data
public class SimulationResult implements java.io.Serializable {

    private Object result;

    private String script;

    private Map<Object, Object> paramValues;
}
