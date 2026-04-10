package com.swak.formula.dag;

import java.io.Serializable;
import java.util.Objects;

/**
 * 公式数据
 *
 * @author colley
 * @since 1.0
 */
public final class FormulaData implements Serializable {

    /**
     * 唯一名称
     */
    private String name;

    /**
     * 索引
     */
    private int index;

    /**
     * 公式
     */
    private String formula;

    /**
     * 数据
     */
    private Object value;

    /**
     * Instantiates a new Formula data.
     *
     * @param name    the name
     * @param index   the index
     * @param formula the formula
     * @param value   the value
     */
    public FormulaData(String name, int index, String formula, Object value) {
        this.name = name;
        this.index = index;
        this.formula = formula;
        this.value = value;
    }

    /**
     * Instantiates a new Formula data.
     *
     * @param name    the name
     * @param formula the formula
     * @param value   the value
     */
    public FormulaData(String name, String formula, Object value) {
        this(name, -1, formula, value);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets index.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets index.
     *
     * @param index the index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Gets formula.
     *
     * @return the formula
     */
    public String getFormula() {
        return formula;
    }

    /**
     * Sets formula.
     *
     * @param formula the formula
     */
    public void setFormula(String formula) {
        this.formula = formula;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FormulaData that = (FormulaData) o;
        return index == that.index && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index);
    }
}
