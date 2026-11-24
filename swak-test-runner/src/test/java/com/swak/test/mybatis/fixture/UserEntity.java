package com.swak.test.mybatis.fixture;

import com.swak.jdbc.annotation.SColumn;
import com.swak.jdbc.annotation.STable;

/**
 * 测试用实体：覆盖普通列、非表字段、驼峰转下划线与主键。
 */
@STable("t_user")
public class UserEntity {

    @SColumn("id")
    private Long id;

    private String userName;

    @SColumn("age")
    private Integer age;

    @SColumn(exist = false)
    private String ignoreMe;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getIgnoreMe() {
        return ignoreMe;
    }

    public void setIgnoreMe(String ignoreMe) {
        this.ignoreMe = ignoreMe;
    }
}
