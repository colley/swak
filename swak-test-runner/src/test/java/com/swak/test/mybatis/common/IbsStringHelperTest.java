package com.swak.test.mybatis.common;

import com.swak.jdbc.common.IbsStringHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * {@link IbsStringHelper} 回归：都是无副作用的静态字符串处理，重点覆盖边界分支。
 */
@DisplayName("IbsStringHelper 字符串工具")
class IbsStringHelperTest {

    @Test
    @DisplayName("replace 去掉点号与括号，空值返回空串")
    void replaceBasic() {
        assertThat(IbsStringHelper.replace("a.b[c](d)")).isEqualTo("abcd");
        assertThat(IbsStringHelper.replace("")).isEmpty();
        assertThat(IbsStringHelper.replace(null)).isEmpty();
    }

    @Test
    @DisplayName("repeatParamFormat 生成 #{name} 占位符，无参返回问号")
    void repeatParamFormatBasic() {
        assertThat(IbsStringHelper.repeatParamFormat("a.b")).isEqualTo("#{ab}");
        assertThat(IbsStringHelper.repeatParamFormat()).isEqualTo("?");
    }

    @Test
    @DisplayName("repeatParamFormat 多参数版本按次数拼接且末项不带分隔符")
    void repeatParamFormatMulti() {
        // repChar 同时作为前缀和后缀，chain 追加在后缀之后。
        String format = IbsStringHelper.repeatParamFormat("item", "#{", ",", 2);
        assertThat(format).isEqualTo("#{item[0]#{,#{item[1]#{");
    }

    @Test
    @DisplayName("lastIndexOfLetter 返回连续字母的最后一个下标")
    void lastIndexOfLetterBasic() {
        assertThat(IbsStringHelper.lastIndexOfLetter("abc123")).isEqualTo(2);
        assertThat(IbsStringHelper.lastIndexOfLetter("abc")).isEqualTo(2);
        assertThat(IbsStringHelper.lastIndexOfLetter("1abc")).isEqualTo(-1);
    }

    @Test
    @DisplayName("join 支持数组与迭代器，空数组返回空串")
    void joinBasic() {
        assertThat(IbsStringHelper.join(",", new String[]{"a", "b"})).isEqualTo("a,b");
        assertThat(IbsStringHelper.join(",", new String[0])).isEmpty();
        assertThat(IbsStringHelper.join("-", Arrays.asList("a", "b", "c").iterator())).isEqualTo("a-b-c");
        assertThat(IbsStringHelper.join("-", Collections.emptyIterator())).isEmpty();
    }

    @Test
    @DisplayName("add 按位拼接两个数组，repeat 重复字符串")
    void addAndRepeat() {
        assertThat(IbsStringHelper.add(new String[]{"a", "b"}, "=", new String[]{"1", "2"}))
                .containsExactly("a=1", "b=2");
        assertThat(IbsStringHelper.repeat("ab", 0)).isEmpty();
        assertThat(IbsStringHelper.repeat("ab", 3)).isEqualTo("ababab");
    }

    @Test
    @DisplayName("replace 支持整词模式与数组批量替换")
    void replaceVariants() {
        assertThat(IbsStringHelper.replace("a.b.c", ".", "_")).isEqualTo("a_b_c");
        assertThat(IbsStringHelper.replace((String) null, ".", "_")).isNull();
        assertThat(IbsStringHelper.replace("no-match", ".", "_")).isEqualTo("no-match");

        // wholeWords=true 时，占位符后紧跟标识符字符则不替换。
        assertThat(IbsStringHelper.replace("cat category", "cat", "dog", true)).isEqualTo("dog category");
        assertThat(IbsStringHelper.replace("cat category", "cat", "dog", false)).isEqualTo("dog dogegory");

        assertThat(IbsStringHelper.replace(new String[]{"a.b", "c.d"}, ".", "_"))
                .containsExactly("a_b", "c_d");

        assertThat(IbsStringHelper.replaceOnce("a.b.c", ".", "_")).isEqualTo("a_b.c");
        assertThat(IbsStringHelper.replaceOnce(null, ".", "_")).isNull();
        assertThat(IbsStringHelper.replaceOnce("abc", ".", "_")).isEqualTo("abc");
    }

    @Test
    @DisplayName("split 按分隔符切分，include 时保留分隔符")
    void splitBasic() {
        assertThat(IbsStringHelper.split(",", "a,b,c")).containsExactly("a", "b", "c");
        assertThat(IbsStringHelper.split(",", "a,b,c", true)).containsExactly("a", ",", "b", ",", "c");
    }

    @Test
    @DisplayName("qualify/unqualify/root/unroot 解析限定名")
    void qualifiedNames() {
        assertThat(IbsStringHelper.unqualify("a.b.c")).isEqualTo("c");
        assertThat(IbsStringHelper.unqualify("c")).isEqualTo("c");
        assertThat(IbsStringHelper.qualifier("a.b.c")).isEqualTo("a.b");
        assertThat(IbsStringHelper.qualifier("c")).isEmpty();
        assertThat(IbsStringHelper.root("a.b.c")).isEqualTo("a");
        assertThat(IbsStringHelper.root("a")).isEqualTo("a");
        assertThat(IbsStringHelper.unroot("a.b.c")).isEqualTo("b.c");
        assertThat(IbsStringHelper.unroot("a")).isEqualTo("a");

        assertThat(IbsStringHelper.qualify("t", "c")).isEqualTo("t.c");
        assertThat(IbsStringHelper.qualify("t", new String[]{"a", "b"})).containsExactly("t.a", "t.b");
        assertThat(IbsStringHelper.qualify(null, new String[]{"a"})).containsExactly("a");
        assertThatNullPointerException().isThrownBy(() -> IbsStringHelper.qualify(null, "c"));
        assertThatNullPointerException().isThrownBy(() -> IbsStringHelper.qualify("t", (String) null));
    }

    @Test
    @DisplayName("suffix 追加后缀，null 后缀原样返回")
    void suffixBasic() {
        String[] columns = {"a", "b"};
        assertThat(IbsStringHelper.suffix(columns, "_x")).containsExactly("a_x", "b_x");
        assertThat(IbsStringHelper.suffix(columns, null)).isSameAs(columns);
    }

    @Test
    @DisplayName("booleanValue 只认 true/t")
    void booleanValueBasic() {
        assertThat(IbsStringHelper.booleanValue("TRUE")).isTrue();
        assertThat(IbsStringHelper.booleanValue(" t ")).isTrue();
        assertThat(IbsStringHelper.booleanValue("yes")).isFalse();
    }

    @Test
    @DisplayName("toString 拼接数组，空数组返回空串")
    void toStringBasic() {
        assertThat(IbsStringHelper.toString(new Object[]{1, 2, 3})).isEqualTo("1, 2, 3");
        assertThat(IbsStringHelper.toString(new Object[0])).isEmpty();
        assertThat(IbsStringHelper.toString(new Object[]{"a"})).isEqualTo("a");
    }

    @Test
    @DisplayName("multiply 对多个占位符做笛卡尔展开")
    void multiplyBasic() {
        String[] result = IbsStringHelper.multiply("a-b",
                List.of("a", "b").iterator(),
                List.<Object>of(new String[]{"1", "2"}, new String[]{"X"}).iterator());

        assertThat(result).containsExactly("1-X", "2-X");
    }

    @Test
    @DisplayName("countUnquoted 忽略引号内的分隔符，禁止统计单引号")
    void countUnquotedBasic() {
        assertThat(IbsStringHelper.countUnquoted("a,b,'c,d'", ',')).isEqualTo(2);
        assertThat(IbsStringHelper.countUnquoted((String) null, ',')).isZero();
        assertThatIllegalArgumentException().isThrownBy(() -> IbsStringHelper.countUnquoted("abc", '\''));
    }

    @Test
    @DisplayName("isEmpty/isNotEmpty 处理 null")
    void emptiness() {
        assertThat(IbsStringHelper.isEmpty(null)).isTrue();
        assertThat(IbsStringHelper.isEmpty("")).isTrue();
        assertThat(IbsStringHelper.isEmpty(" ")).isFalse();
        assertThat(IbsStringHelper.isNotEmpty("x")).isTrue();
        assertThat(IbsStringHelper.isNotEmpty(null)).isFalse();
    }

    @Test
    @DisplayName("firstIndexOfChar 返回最早出现的分隔符位置")
    void firstIndexOfCharBasic() {
        assertThat(IbsStringHelper.firstIndexOfChar("a,b;c", ",;", 0)).isEqualTo(1);
        assertThat(IbsStringHelper.firstIndexOfChar("a,b;c", ";,", 0)).isEqualTo(1);
        assertThat(IbsStringHelper.firstIndexOfChar("abc", ",;", 0)).isEqualTo(-1);
        assertThat(IbsStringHelper.firstIndexOfChar("a,b", ",", 2)).isEqualTo(-1);
    }

    @Test
    @DisplayName("truncate 超长才截断")
    void truncateBasic() {
        assertThat(IbsStringHelper.truncate("abcdef", 3)).isEqualTo("abc");
        assertThat(IbsStringHelper.truncate("abc", 3)).isEqualTo("abc");
        assertThat(IbsStringHelper.truncate("abc", 10)).isEqualTo("abc");
    }

    @Test
    @DisplayName("generateAlias 生成小写别名，数字结尾补 x")
    void generateAliasBasic() {
        assertThat(IbsStringHelper.generateAlias("com.example.Order")).isEqualTo("order_");
        assertThat(IbsStringHelper.generateAlias("com.example.Order", 2)).isEqualTo("order2_");
        // 前缀非字母时被裁掉。
        assertThat(IbsStringHelper.generateAlias("_Order")).isEqualTo("order_");
        // 超过 10 个字符被截断。
        assertThat(IbsStringHelper.generateAlias("VeryLongEntityName")).isEqualTo("verylongen_");
        // 截断后以数字结尾时补 x。
        assertThat(IbsStringHelper.generateAlias("Order1")).isEqualTo("order1x_");
    }

    @Test
    @DisplayName("unqualifyEntityName 去掉包名，带斜杠的角色名按斜杠前缀截取")
    void unqualifyEntityNameBasic() {
        assertThat(IbsStringHelper.unqualifyEntityName("com.example.Order")).isEqualTo("Order");
        assertThat(IbsStringHelper.unqualifyEntityName("Order")).isEqualTo("Order");
        // 现状：斜杠前一位被吃掉（"ab/CD" → "a"），见短板记录。
        assertThat(IbsStringHelper.unqualifyEntityName("ab/CD")).isEqualTo("a");
    }

    @Test
    @DisplayName("大小写转换对 null 安全")
    void caseConversion() {
        assertThat(IbsStringHelper.toUpperCase("abc")).isEqualTo("ABC");
        assertThat(IbsStringHelper.toLowerCase("ABC")).isEqualTo("abc");
        assertThat(IbsStringHelper.toUpperCase(null)).isNull();
        assertThat(IbsStringHelper.toLowerCase(null)).isNull();
    }

    @Test
    @DisplayName("moveAndToBeginning 把 and 断句搬回开头")
    void moveAndToBeginningBasic() {
        // 只截掉 4 个字符的前缀 " and"，保留其后的空格。
        assertThat(IbsStringHelper.moveAndToBeginning(" and a = 1")).isEqualTo(" a = 1 and ");
        assertThat(IbsStringHelper.moveAndToBeginning("a = 1")).isEqualTo("a = 1 and ");
        assertThat(IbsStringHelper.moveAndToBeginning("   ")).isEqualTo("   ");
    }

    @Test
    @DisplayName("msTimeformat2String 按天/时/分/秒/毫秒拼装")
    void msTimeformat2StringBasic() {
        long day = 24L * 3600 * 1000;
        String formatted = IbsStringHelper.msTimeformat2String(day + 3_723_456L);
        assertThat(formatted).startsWith("1天").contains("小时").contains("分").contains("秒").contains("毫秒");
        assertThat(IbsStringHelper.msTimeformat2String(0L)).isEmpty();
    }
}
