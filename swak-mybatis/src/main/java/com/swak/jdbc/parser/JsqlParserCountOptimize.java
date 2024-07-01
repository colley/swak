package com.swak.jdbc.parser;


import com.swak.common.util.StringPool;
import com.swak.jdbc.metadata.SqlInfo;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 16:21
 */
public class JsqlParserCountOptimize implements ISqlParser {
    private static final List<SelectItem> COUNT_SELECT_ITEM = Collections.singletonList(defaultCountSelectItem());
    private final Log logger = LogFactory.getLog(JsqlParserCountOptimize.class);

    private boolean optimizeJoin = false;

    /**
     * 获取jsqlparser中count的SelectItem
     */
    private static SelectItem defaultCountSelectItem() {
        Function function = new Function();
        ExpressionList expressionList = new ExpressionList(Collections.singletonList(new LongValue(1)));
        function.setName("COUNT");
        function.setParameters(expressionList);
        return new SelectExpressionItem(function);
    }

    @Override
    public SqlInfo parser(String sql) {
        if (logger.isDebugEnabled()) {
            logger.debug("JsqlParserCountOptimize sql=" + sql);
        }
        SqlInfo sqlInfo = SqlInfo.newInstance();
        try {
            Select selectStatement = (Select) CCJSqlParserUtil.parse(sql);
            PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
            Distinct distinct = plainSelect.getDistinct();
            GroupByElement groupBy = plainSelect.getGroupBy();
            List<OrderByElement> orderBy = plainSelect.getOrderByElements();

            // 添加包含groupBy 不去除orderBy
            if (null == groupBy && CollectionUtils.isNotEmpty(orderBy)) {
                plainSelect.setOrderByElements(null);
                sqlInfo.setOrderBy(false);
            }
            //#95 Github, selectItems contains #{} ${}, which will be translated to ?, and it may be in a function: power(#{myInt},2)
            for (SelectItem item : plainSelect.getSelectItems()) {
                if (item.toString().contains(StringPool.QUESTION_MARK)) {
                    return sqlInfo.setSql(SqlParserUtils.getOriginalCountSql(selectStatement.toString()));
                }
            }
            // 包含 distinct、groupBy不优化
            if (distinct != null || null != groupBy) {
                return sqlInfo.setSql(SqlParserUtils.getOriginalCountSql(selectStatement.toString()));
            }
            // 包含 join 连表,进行判断是否移除 join 连表
            List<Join> joins = plainSelect.getJoins();
            if (optimizeJoin && CollectionUtils.isNotEmpty(joins)) {
                boolean canRemoveJoin = true;
                String whereS = Optional.ofNullable(plainSelect.getWhere()).map(Expression::toString).orElse(StringPool.EMPTY);
                // 不区分大小写
                whereS = whereS.toLowerCase();
                for (Join join : joins) {
                    if (!join.isLeft()) {
                        canRemoveJoin = false;
                        break;
                    }
                    Table table = (Table) join.getRightItem();
                    String str = Optional.ofNullable(table.getAlias()).map(Alias::getName).orElse(table.getName()) + StringPool.DOT;
                    // 不区分大小写
                    str = str.toLowerCase();
                    String onExpressionS = join.getOnExpression().toString();
                    /* 如果 join 里包含 ?(代表有入参) 或者 where 条件里包含使用 join 的表的字段作条件,就不移除 join */
                    if (onExpressionS.contains(StringPool.QUESTION_MARK) || whereS.contains(str)) {
                        canRemoveJoin = false;
                        break;
                    }
                }
                if (canRemoveJoin) {
                    plainSelect.setJoins(null);
                }
            }
            // 优化 SQL
            plainSelect.setSelectItems(COUNT_SELECT_ITEM);
            return sqlInfo.setSql(selectStatement.toString());
        } catch (Throwable e) {
            // 无法优化使用原 SQL
            return sqlInfo.setSql(SqlParserUtils.getOriginalCountSql(sql));
        }
    }

    public static void main(String[] args) {
        ISqlParser sqlParser = new JsqlParserCountOptimize();
        String sql = "select * from user where name =? and age>? order by name desc";
        System.out.println(sqlParser.parser(sql));
    }
}
