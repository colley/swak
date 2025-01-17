package com.swak.formula.entity;

import lombok.Data;

import java.util.Map;

@Data
public class VirtualSimulationCmd implements java.io.Serializable{

    private String logicFormula;

    private Map<String,String> mockData;

}
