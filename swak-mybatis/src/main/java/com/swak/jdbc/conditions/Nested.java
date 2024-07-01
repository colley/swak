
package  com.swak.jdbc.conditions;

import com.swak.jdbc.segments.SqlSegment;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * 查询条件封装
 * <p>嵌套</p>
 * <li>泛型 Param 是具体需要运行函数的类(也是 wrapper 的子类)</li>
 */
public interface Nested<Param, Children> extends Serializable {

    /**
     * ignore
     */
    default Children and(Consumer<Param> consumer) {
        return and(true, consumer);
    }

    Children addCondition(SqlSegment sqlSegment);
    /**
     * AND 嵌套
     * <p>
     * 例: and(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return children
     */
    Children and(boolean condition, Consumer<Param> consumer);

    /**
     * ignore
     */
    default Children or(Consumer<Param> consumer) {
        return or(true, consumer);
    }

    /**
     * OR 嵌套
     * <p>
     * 例: or(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return children
     */
    Children or(boolean condition, Consumer<Param> consumer);

    /**
     * ignore
     */
    default Children nested(Consumer<Param> consumer) {
        return nested(true, consumer);
    }

    /**
     * 正常嵌套 不带 AND 或者 OR
     * <p>
     * 例: nested(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return children
     */
    Children nested(boolean condition, Consumer<Param> consumer);

    /**
     * ignore
     */
    default Children not(Consumer<Param> consumer) {
        return not(true, consumer);
    }

    /**
     * not嵌套
     * <p>
     * 例: not(i -&gt; i.eq("name", "李白").ne("status", "活着"))
     * </p>
     *
     * @param condition 执行条件
     * @param consumer  消费函数
     * @return children
     */
    Children not(boolean condition, Consumer<Param> consumer);
}
