package com.swak.test.mybatis.fixture;

import com.swak.jdbc.annotation.SColumn;
import com.swak.jdbc.annotation.STable;

/**
 * 测试用从表实体：用于 join 场景。
 */
@STable("t_dept")
public class DeptEntity {

    @SColumn("id")
    private Long id;

    @SColumn("dept_name")
    private String deptName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}
