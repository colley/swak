
package com.swak.jdbc.toolkit;

import com.swak.common.dto.PageInfo;
import com.swak.jdbc.common.ConfigProperties;
import com.swak.jdbc.segments.SqlSegment;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.enums.SqlLikeMode;
import com.swak.jdbc.segments.*;

import java.util.Collection;
import java.util.List;

public final class JdbcRestrictions {
    private JdbcRestrictions() {
        //cannot be instantiated
    }
    

    public static SqlSegment leftFunction(String funcName, String property, Object value){
        return new SingleFuncSegment(funcName, property, value, true);
    }
    
    public static SqlSegment rightFunction(String funcName,String property,Object value){
        return new SingleFuncSegment(funcName, property, value, false);
    }
    
    public static SqlSegment addCondition(String propertyName, SqlKeyword sqlKeyword,Object value) {
        return new SqlKeywordSegment(propertyName, sqlKeyword,value);
    }

    public static SqlSegment likeValue(String propertyName, SqlKeyword sqlKeyword,Object value,SqlLikeMode sqlLike) {
        return new LikeSegment(propertyName, value, sqlKeyword,sqlLike);
    }
    
    public static SimpleSegment eq(String propertyName, Object value) {
        return new SimpleSegment(propertyName, value, SqlKeyword.EQ);
    }
    
    /**
     * (case when then end)=value
     * eq
     */
    public static CaseSegment eq(CaseSegment.WhenExpression[] when, Object value) {
        return new CaseSegment(when, value, SqlKeyword.EQ);
    }
    
    /**
     * (case when then end)=value
     * eq
     */
    public static CaseSegment eq(List<CaseSegment.WhenExpression> when, Object value) {
        return new CaseSegment(when.toArray(new CaseSegment.WhenExpression[when.size()]), value, SqlKeyword.EQ);
    }
   
    /**
     * 函数表达式
     * eq
     */
    public static FunctionSqlSegment eq(FunctionSqlSegment.FunctionBody function, Object value) {
        return new FunctionSqlSegment(function, value, SqlKeyword.EQ);
    }
    
    /**
     * 函数表达式
     * eq
     */
    public static FunctionSqlSegment eq(FunctionSqlSegment.FunctionBody oneFunc, FunctionSqlSegment.FunctionBody senFunc) {
        return new FunctionSqlSegment(oneFunc, null, senFunc, SqlKeyword.EQ);
    }

    /**
      * Apply a "not equal" constraint to the named property
      */
    public static SimpleSegment ne(String propertyName, Object value) {
        return new SimpleSegment(propertyName, value, SqlKeyword.NE);
    }

    /**
      * Apply a "like" constraint to the named property
      */
    public static SqlSegment like(String propertyName, Object value) {
        return likeValue(propertyName,SqlKeyword.LIKE,value,SqlLikeMode.DEFAULT);
    }

    /**
      * Apply a "like" constraint to the named property
      */
    public static SqlSegment like(String propertyName, Object value, SqlLikeMode matchMode) {
        return likeValue(propertyName,SqlKeyword.LIKE,value,matchMode);
    }
    
    public static SqlSegment notLike(String propertyName, String value, SqlLikeMode matchMode) {
        return likeValue(propertyName,SqlKeyword.NOT_LIKE,value,matchMode);
    }

    /**
      * A case-insensitive "like", similar to Postgres <tt>likeIgnoreCase</tt>
      * operator
      */
    public static SqlSegment likeIgnoreCase(String propertyName, String value, SqlLikeMode matchMode) {
        return new LikeSegment(propertyName, value, true, matchMode);
    }

    /**
      * A case-insensitive "like", similar to Postgres <tt>likeIgnoreCase</tt>
      * operator
      */
    public static SqlSegment likeIgnoreCase(String propertyName, Object value) {
        return new LikeSegment(propertyName, value, true, SqlLikeMode.DEFAULT);
    }

    /**
      * Apply a "greater than" constraint to the named property
      */
    public static SimpleSegment gt(String propertyName, Object value) {
        return new SimpleSegment(propertyName, value, SqlKeyword.GT);
    }

    /**
      * Apply a "less than" constraint to the named property
      */
    public static SimpleSegment lt(String propertyName, Object value) {
        return new SimpleSegment(propertyName, value, SqlKeyword.LT);
    }

    /**
      * Apply a "less than or equal" constraint to the named property
      */
    public static SimpleSegment le(String propertyName, Object value) {
        return new SimpleSegment(propertyName, value, SqlKeyword.LE);
    }

    /**
      * Apply a "greater than or equal" constraint to the named property
      */
    public static SimpleSegment ge(String propertyName, Object value) {
        return new SimpleSegment(propertyName, value, SqlKeyword.GE);
    }

    /**
      * Apply a "between" constraint to the named property
      */
    public static SqlSegment between(String propertyName, Object lo, Object hi) {
        return new BetweenSegment(propertyName, lo, hi);
    }

    public static SqlSegment apply(String ... applySql) {
        ApplySqlSegment applySqlSegment = new ApplySqlSegment();
        return applySqlSegment.apply(applySql);
    }

    public static SqlSegment apply(SqlSegment ... sqlSegments) {
        ApplySqlSegment applySqlSegment = new ApplySqlSegment();
        return applySqlSegment.apply(sqlSegments);
    }

    public static SqlSegment notBetween(String propertyName, Object lo, Object hi) {
        return new BetweenSegment(propertyName, lo, hi, SqlKeyword.NOT_BETWEEN);
    }

    /**
      * Apply an "in" constraint to the named property
      */
    public static SqlSegment in(String propertyName, Object[] values) {
        return new InSqlSegment(propertyName, SqlKeyword.IN,values);
    }
    
    
    /**
     * 子查询 a in (select Id from XXX)
     * in
     */
    public static SqlSegment in(String propertyName, SelectSegment selectSegment) {
        return new SubSelectSegment(propertyName, selectSegment, SqlKeyword.IN);
    }
    
    public static SqlSegment notIn(String propertyName, SelectSegment selectSegment) {
        return new SubSelectSegment(propertyName, selectSegment, SqlKeyword.NOT_IN);
    }
    

    /**
      * Apply an "in" constraint to the named property
      */
    @SuppressWarnings("rawtypes")
    public static SqlSegment in(String propertyName, Collection values) {
        return new InSqlSegment(propertyName,  SqlKeyword.IN,values.toArray());
    }
    
    /**
     * Apply an "not In" constraint to the named property
     */
   public static SqlSegment notIn(String propertyName, Object[] values) {
       return new InSqlSegment(propertyName,SqlKeyword.NOT_IN, values);
   }

   /**
     * Apply an "not in" constraint to the named property
     */
   @SuppressWarnings("rawtypes")
   public static SqlSegment notIn(String propertyName, Collection values) {
       return new InSqlSegment(propertyName,  SqlKeyword.NOT_IN,values.toArray());
   }

    /**
      * Apply an "is null" constraint to the named property
      * @return Criterion
      */
    public static SqlSegment isNull(String propertyName) {
        return new NullSegment(propertyName, SqlKeyword.IS_NULL);
    }

    /**
      * Apply an "is not null" constraint to the named property
      * @return Criterion
      */
    public static SqlSegment isNotNull(String propertyName) {
        return new NullSegment(propertyName, SqlKeyword.IS_NOT_NULL);
    }
    
    
    /**
	 * Apply an "equal" constraint to two properties
	 */
	public static PropertySegment eqProperty(String propertyName, String otherPropertyName) {
		return new PropertySegment(propertyName, otherPropertyName, SqlKeyword.EQ);
	}
    
    public static PropertySegment eqProperty(String propertyName, AliasColumnSegment otherProperty) {
        AliasColumnSegment firstProperty = AliasColumnSegment.as(propertyName);
        return new PropertySegment(firstProperty, otherProperty, SqlKeyword.EQ);
    }

	/**
	 * Apply a "not equal" constraint to two properties
	 */
	public static PropertySegment neProperty(String propertyName, String otherPropertyName) {
		return new PropertySegment(propertyName, otherPropertyName, SqlKeyword.NE);
	}
	/**
	 * Apply a "less than" constraint to two properties
	 */
	public static PropertySegment ltProperty(String propertyName, String otherPropertyName) {
		return new PropertySegment(propertyName, otherPropertyName, SqlKeyword.LT);
	}
	/**
	 * Apply a "less than or equal" constraint to two properties
	 */
	public static PropertySegment leProperty(String propertyName, String otherPropertyName) {
		return new PropertySegment(propertyName, otherPropertyName, SqlKeyword.LE);
	}
	/**
	 * Apply a "greater than" constraint to two properties
	 */
	public static PropertySegment gtProperty(String propertyName, String otherPropertyName) {
		return new PropertySegment(propertyName, otherPropertyName, SqlKeyword.GT);
	}
	/**
	 * Apply a "greater than or equal" constraint to two properties
	 */
	public static PropertySegment geProperty(String propertyName, String otherPropertyName) {
		return new PropertySegment(propertyName, otherPropertyName, SqlKeyword.GE);
	}
	
	public static PropertySegment betweenProperty(String propertyName, String firstPropertyName, String secondPropertyName) {
		return new PropertySegment(propertyName, firstPropertyName,secondPropertyName, SqlKeyword.BETWEEN);
	}
	
	
	
	public static SqlSegment asConst(String constValue) {
		return new ConstSegment(constValue);
	}
	
	/**
	 * Group expressions together in a single conjunction (A and B and C...)
	 */
	public static JunctionSqlSegment and() {
		return new JunctionSqlSegment(SqlKeyword.AND);
	}

	/**
	 * Group expressions together in a single disjunction (A or B or C...)
	 */
	public static JunctionSqlSegment or() {
		return new JunctionSqlSegment(SqlKeyword.OR);
	}
	
	
	public static SqlSegment not(SqlSegment expression) {
		return new NotSegment(expression);
	}
	
	public static PageSegment limit(int startPos, int pageSize) {
		return new PageSegment(startPos, pageSize, SqlKeyword.LIMIT);
	}

    public static PageSegment limit(PageInfo pageInfo) {
        return new PageSegment(pageInfo.getOffset(), pageInfo.getPageSize(), SqlKeyword.LIMIT);
    }
}
