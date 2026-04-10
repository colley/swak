package com.swak.formula.plugin;


import com.swak.common.util.GetterUtil;
import com.swak.formula.spi.FormulaPlugin;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.StatUtils;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;

/**
 * Math函数
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
public class MathFunctionPlugin implements FormulaPlugin {

    /**
     * 求abs abs(-2.35)=2.35
     *
     * @param value the value
     * @return double
     */
    public double abs(Number value) {
        return Math.abs(value.doubleValue());
    }

    /**
     * 求平均数 avg(1,2,3)=2.0
     *
     * @param values the values
     * @return double
     */
    public double avg(Number... values) {
        double[] arrays = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            arrays[i] = values[i].doubleValue();
        }
        return StatUtils.mean(arrays);
    }

    /**
     * 求max max(1,2,3)=3.0
     *
     * @param values the values
     * @return double
     */
    public double max(Number... values) {
        double[] arrays = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            arrays[i] = values[i].doubleValue();
        }
        return StatUtils.max(arrays);
    }

    /**
     * Min double.
     *
     * @param values the values
     * @return the double
     */
    public double min(Number... values) {
        double[] arrays = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            arrays[i] = values[i].doubleValue();
        }
        return StatUtils.min(arrays);
    }

    /**
     * 求余数mod mod(10.0,3.0)=1.0
     *
     * @param value1 the value 1
     * @param value2 the value 2
     * @return double
     */
    public double mod(Number value1, Number value2) {
        return value1.doubleValue() % value2.doubleValue();
    }


    /**
     * 取乘方跟
     *
     * @param value the value
     * @return double
     */
    public double sqrt(Number value) {
        return Math.sqrt(value.doubleValue());
    }

    /**
     * 四舍五入
     *
     * @param value the value
     * @return double
     */
    public double round(Number value) {
        return  Math.round(value.doubleValue());
    }


    /**
     * Prod double.
     *
     * @param values the values
     * @return the double
     */
    public double prod(Number ... values) {
        double[] arrays = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            arrays[i] = values[i].doubleValue();
        }
        return  StatUtils.product(arrays);
    }

    /**
     * Log double.
     *
     * @param value the value
     * @return the double
     */
    public double log(Number value) {
        return  Math.log(value.doubleValue());
    }


    /**
     * Pow double.
     *
     * @param value1 the value 1
     * @param value2 the value 2
     * @return the double
     */
    public double pow(Number value1,Number value2) {
        return  Math.pow(value1.doubleValue(), value2.doubleValue());
    }

    /**
     * Exp double.
     *
     * @param value the value
     * @return the double
     */
    public double exp(Number value) {
        return  Math.exp(value.doubleValue());
    }

    /**
     * Cast to int.
     *
     * @param value the value
     * @return the int
     */
    public int castToInt(Number value) {
        return value.intValue();
    }

    /**
     * Cast to long.
     *
     * @param value the value
     * @return the int
     */
    public long castToLong(Number value) {
        return value.longValue();
    }

    /**
     * Is numeric boolean.
     *
     * @param value the value
     * @return the boolean
     */
    public boolean isNumeric(String value) {
        return NumberUtils.isCreatable(value);
    }

    public Number convertHexToDec(String  value) {
        if(value.startsWith("0x")) {
            value = value.substring(2);
        }
        BigInteger bigInteger = new BigInteger(value, 16);
        return bigInteger.intValue();
    }


    public Integer convertStrToNum(String str) {
        return GetterUtil.getInteger(str,null);
    }

    public String convertNumToStr(Number number) {
        if(Objects.nonNull(number)){
            return number.toString();
        }
        return null;
    }

    public String convertToUpper(String value) {
        if(Objects.nonNull(value)){
            return value.toUpperCase();
        }
        return null;
    }

    public String convertToLower(String value) {
        if(Objects.nonNull(value)){
            return value.toLowerCase();
        }
        return null;
    }

    /**
     * ceiling
     *
     * @param value1 the value 1
     * @return the Integer
     */
    public Number ceiling(Number value1) {
        if (value1 == null) {
            return null;
        }
        return Math.ceil(value1.doubleValue());
    }

    /**
     * floor
     *
     * @param value1 the value 1
     * @return the Integer
     */
    public Number floor(Number value1) {
        if (value1 == null) {
            return null;
        }
        return Math.floor(value1.doubleValue());
    }

    /**
     * Log10
     *
     * @param value the value
     * @return the double
     */
    public Number log10(Number value) {
        if (value == null) {
            return null;
        }
        return  Math.log10(value.doubleValue());
    }

    /**
     * neg
     *
     * @param value the value
     * @return the double
     */
    public Number neg(Number value) {
        if (value == null) {
            return null;
        }
        return  -value.doubleValue();
    }

    /**
     * rnd
     *
     * @param value the value
     * @return the double
     */
    public Number rnd(Number value) {
        if (value == null) {
            return null;
        }
        Random random = new Random();
        return random.nextInt(value.intValue());
    }
}
