package com.swak.formula.plugin;

import com.swak.formula.spi.FormulaPlugin;

/**
 * The type Circular function plugin.
 *
 * @author colley.ma
 * @since 2.4.0
 **/
public class CircularFunctionPlugin implements FormulaPlugin {
    /**
     * 求sin sin(30.0)=> 0.98803162409286183
     *
     * @param value the value
     * @return double
     * @Param [value]
     */
    public double sin(Number value) {
        return Math.sin(value.doubleValue());
    }


    /**
     * Asin double.
     *
     * @param value the value
     * @return the double
     */
    public double asin(Number value) {
        return Math.asin(value.doubleValue());
    }

    /**
     * Cos double.
     *
     * @param value the value
     * @return the double
     */
    public double cos(Number value) {
        return Math.cos(value.doubleValue());
    }

    /**
     * Acos double.
     *
     * @param value the value
     * @return the double
     */
    public double acos(Number value) {
        return Math.acos(value.doubleValue());
    }

    /**
     * Tan double.
     *
     * @param value the value
     * @return the double
     */
    public double tan(Number value) {
        return Math.tan(value.doubleValue());
    }

    /**
     * Atan double.
     *
     * @param value the value
     * @return the double
     */
    public double atan(Number value) {
        return Math.atan(value.doubleValue());
    }


    /**
     * Cot double.
     *
     * @param value the value
     * @return the double
     */
    public double cot(Number value) {
        return 1.0 / Math.tan(value.doubleValue());
    }


    /**
     * Acot double.
     *
     * @param value the value
     * @return the double
     */
    public double acot(Number value) {
        return Math.atan(1.0 / value.doubleValue());
    }

    /**
     * 角度转弧度
     *
     * @param degree the degree
     * @return double
     * @Param [degree]
     */
    public double degToRad(Number degree) {
        return Math.toRadians(degree.doubleValue());
    }

    /**
     * 弧度转角度
     *
     * @param radian the radian
     * @return double
     * @Param [radian]
     */
    public  double radToDeg(Number radian){
        return Math.toDegrees(radian.doubleValue());
    }
}
